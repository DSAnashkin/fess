/*
 * Copyright 2012-2016 CodeLibs Project and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.codelibs.fess.ds.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codelibs.core.lang.StringUtil;
import org.codelibs.fess.app.service.FailureUrlService;
import org.codelibs.fess.crawler.exception.CrawlingAccessException;
import org.codelibs.fess.crawler.exception.MultipleCrawlingAccessException;
import org.codelibs.fess.ds.IndexUpdateCallback;
import org.codelibs.fess.es.config.exentity.DataConfig;
import org.codelibs.fess.exception.DataStoreCrawlingException;
import org.codelibs.fess.exception.DataStoreException;
import org.codelibs.fess.exception.FessSystemException;
import org.codelibs.fess.util.ComponentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseDataStoreImpl extends AbstractDataStoreImpl {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseDataStoreImpl.class);

    private static final String SQL_PARAM = "sql";

    private static final String URL_PARAM = "url";

    private static final String PASSWORD_PARAM = "password";

    private static final String USERNAME_PARAM = "username";

    private static final String DRIVER_PARAM = "driver";

    protected String getDriverClass(final Map<String, String> paramMap) {
        final String driverName = paramMap.get(DRIVER_PARAM);
        if (StringUtil.isBlank(driverName)) {
            throw new DataStoreException("JDBC driver is null");
        }
        return driverName;
    }

    protected String getUsername(final Map<String, String> paramMap) {
        return paramMap.get(USERNAME_PARAM);
    }

    protected String getPassword(final Map<String, String> paramMap) {
        return paramMap.get(PASSWORD_PARAM);
    }

    protected String getUrl(final Map<String, String> paramMap) {
        return paramMap.get(URL_PARAM);
    }

    protected String getSql(final Map<String, String> paramMap) {
        final String sql = paramMap.get(SQL_PARAM);
        if (StringUtil.isBlank(sql)) {
            throw new DataStoreException("sql is null");
        }
        return sql;
    }

    @Override
    protected void storeData(final DataConfig config, final IndexUpdateCallback callback, final Map<String, String> paramMap,
            final Map<String, String> scriptMap, final Map<String, Object> defaultDataMap) {

        final long readInterval = getReadInterval(paramMap);

        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName(getDriverClass(paramMap));

            final String jdbcUrl = getUrl(paramMap);
            final String username = getUsername(paramMap);
            final String password = getPassword(paramMap);
            if (StringUtil.isNotEmpty(username)) {
                con = DriverManager.getConnection(jdbcUrl, username, password);
            } else {
                con = DriverManager.getConnection(jdbcUrl);
            }

            final String sql = getSql(paramMap);
            stmt = con.createStatement();
            rs = stmt.executeQuery(sql); // SQL generated by an administrator
            boolean loop = true;
            while (rs.next() && loop && alive) {
                final Map<String, Object> dataMap = new HashMap<>();
                dataMap.putAll(defaultDataMap);
                for (final Map.Entry<String, String> entry : scriptMap.entrySet()) {
                    final Object convertValue = convertValue(entry.getValue(), rs, paramMap);
                    if (convertValue != null) {
                        dataMap.put(entry.getKey(), convertValue);
                    }
                }

                try {
                    callback.store(paramMap, dataMap);
                } catch (final CrawlingAccessException e) {
                    logger.warn("Crawling Access Exception at : " + dataMap, e);

                    Throwable target = e;
                    if (target instanceof MultipleCrawlingAccessException) {
                        final Throwable[] causes = ((MultipleCrawlingAccessException) target).getCauses();
                        if (causes.length > 0) {
                            target = causes[causes.length - 1];
                        }
                    }

                    String errorName;
                    final Throwable cause = target.getCause();
                    if (cause != null) {
                        errorName = cause.getClass().getCanonicalName();
                    } else {
                        errorName = target.getClass().getCanonicalName();
                    }

                    String url;
                    if (target instanceof DataStoreCrawlingException) {
                        final DataStoreCrawlingException dce = (DataStoreCrawlingException) target;
                        url = dce.getUrl();
                        if (dce.aborted()) {
                            loop = false;
                        }
                    } else {
                        url = sql + ":" + rs.getRow();
                    }
                    final FailureUrlService failureUrlService = ComponentUtil.getComponent(FailureUrlService.class);
                    failureUrlService.store(config, errorName, url, target);
                } catch (final Throwable t) {
                    logger.warn("Crawling Access Exception at : " + dataMap, t);
                    final String url = sql + ":" + rs.getRow();
                    final FailureUrlService failureUrlService = ComponentUtil.getComponent(FailureUrlService.class);
                    failureUrlService.store(config, t.getClass().getCanonicalName(), url, t);
                }

                if (readInterval > 0) {
                    sleep(readInterval);
                }
            }
        } catch (final Exception e) {
            throw new DataStoreException("Failed to crawl data in DB.", e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (final SQLException e) {
                logger.warn("Failed to close a result set.", e);
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (final SQLException e) {
                    logger.warn("Failed to close a statement.", e);
                } finally {
                    try {
                        if (con != null) {
                            con.close();
                        }
                    } catch (final SQLException e) {
                        logger.warn("Failed to close a db connection.", e);
                    }
                }
            }

        }
    }

    protected Object convertValue(final String template, final ResultSet rs, final Map<String, String> paramMap) {
        return convertValue(template, new ResultSetParamMap(rs, paramMap));
    }

    protected static class ResultSetParamMap implements Map<String, String> {
        private final Map<String, String> paramMap = new HashMap<>();

        public ResultSetParamMap(final ResultSet resultSet, final Map<String, String> paramMap) {
            this.paramMap.putAll(paramMap);

            try {
                final ResultSetMetaData metaData = resultSet.getMetaData();
                final int columnCount = metaData.getColumnCount();
                for (int i = 0; i < columnCount; i++) {
                    try {
                        final String label = metaData.getColumnLabel(i + 1);
                        final String value = resultSet.getString(i + 1);
                        this.paramMap.put(label, value);
                    } catch (final SQLException e) {
                        logger.warn("Failed to parse data in a result set. The column is " + (i + 1) + ".", e);
                    }
                }
            } catch (final Exception e) {
                throw new FessSystemException("Failed to access meta data.", e);
            }

        }

        @Override
        public void clear() {
            paramMap.clear();
        }

        @Override
        public boolean containsKey(final Object key) {
            return paramMap.containsKey(key);
        }

        @Override
        public boolean containsValue(final Object value) {
            return paramMap.containsValue(value);
        }

        @Override
        public Set<java.util.Map.Entry<String, String>> entrySet() {
            return paramMap.entrySet();
        }

        @Override
        public String get(final Object key) {
            return paramMap.get(key);
        }

        @Override
        public boolean isEmpty() {
            return paramMap.isEmpty();
        }

        @Override
        public Set<String> keySet() {
            return paramMap.keySet();
        }

        @Override
        public String put(final String key, final String value) {
            return paramMap.put(key, value);
        }

        @Override
        public void putAll(final Map<? extends String, ? extends String> m) {
            paramMap.putAll(m);
        }

        @Override
        public String remove(final Object key) {
            return paramMap.remove(key);
        }

        @Override
        public int size() {
            return paramMap.size();
        }

        @Override
        public Collection<String> values() {
            return paramMap.values();
        }

    }

}
