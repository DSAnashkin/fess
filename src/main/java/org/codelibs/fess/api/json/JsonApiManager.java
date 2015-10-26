/*
 * Copyright 2009-2015 the CodeLibs Project and the Others.
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

package org.codelibs.fess.api.json;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.codelibs.core.CoreLibConstants;
import org.codelibs.core.lang.StringUtil;
import org.codelibs.core.misc.DynamicProperties;
import org.codelibs.fess.Constants;
import org.codelibs.fess.api.BaseApiManager;
import org.codelibs.fess.app.service.FavoriteLogService;
import org.codelibs.fess.app.service.SearchService;
import org.codelibs.fess.entity.FacetInfo;
import org.codelibs.fess.entity.GeoInfo;
import org.codelibs.fess.entity.PingResponse;
import org.codelibs.fess.entity.SearchRenderData;
import org.codelibs.fess.entity.SearchRequestParams;
import org.codelibs.fess.es.client.FessEsClient;
import org.codelibs.fess.exception.WebApiException;
import org.codelibs.fess.helper.FieldHelper;
import org.codelibs.fess.helper.HotSearchWordHelper;
import org.codelibs.fess.helper.HotSearchWordHelper.Range;
import org.codelibs.fess.helper.LabelTypeHelper;
import org.codelibs.fess.helper.QueryHelper;
import org.codelibs.fess.helper.UserInfoHelper;
import org.codelibs.fess.util.ComponentUtil;
import org.codelibs.fess.util.DocumentUtil;
import org.codelibs.fess.util.FacetResponse;
import org.codelibs.fess.util.FacetResponse.Field;
import org.lastaflute.web.util.LaRequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonApiManager extends BaseApiManager {

    private static final Logger logger = LoggerFactory.getLogger(JsonApiManager.class);

    @Resource
    protected DynamicProperties crawlerProperties;

    public JsonApiManager() {
        setPathPrefix("/json");
    }

    @Override
    public boolean matches(final HttpServletRequest request) {
        if (Constants.FALSE.equals(ComponentUtil.getCrawlerProperties().getProperty(Constants.WEB_API_JSON_PROPERTY, Constants.TRUE))) {
            return false;
        }

        final String servletPath = request.getServletPath();
        return servletPath.startsWith(pathPrefix);
    }

    @Override
    public void process(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws IOException,
            ServletException {
        final String formatType = request.getParameter("type");
        switch (getFormatType(formatType)) {
        case SEARCH:
            processSearchRequest(request, response, chain);
            break;
        case LABEL:
            processLabelRequest(request, response, chain);
            break;
        case HOTSEARCHWORD:
            processHotSearchWordRequest(request, response, chain);
            break;
        case FAVORITE:
            processFavoriteRequest(request, response, chain);
            break;
        case FAVORITES:
            processFavoritesRequest(request, response, chain);
            break;
        case PING:
            processPingRequest(request, response, chain);
            break;
        default:
            writeJsonResponse(99, StringUtil.EMPTY, "Not found.");
            break;
        }
    }

    protected void processPingRequest(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) {
        final FessEsClient fessEsClient = ComponentUtil.getElasticsearchClient();
        int status;
        String errMsg = null;
        try {
            final PingResponse pingResponse = fessEsClient.ping();
            status = pingResponse.getStatus();
        } catch (final Exception e) {
            status = 9;
            errMsg = e.getMessage();
            if (errMsg == null) {
                errMsg = e.getClass().getName();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to process a ping request.", e);
            }
        }

        writeJsonResponse(status, null, errMsg);
    }

    protected void processSearchRequest(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) {
        final SearchService searchService = ComponentUtil.getComponent(SearchService.class);

        int status = 0;
        String errMsg = StringUtil.EMPTY;
        String query = null;
        final StringBuilder buf = new StringBuilder(1000);
        request.setAttribute(Constants.SEARCH_LOG_ACCESS_TYPE, Constants.SEARCH_LOG_ACCESS_TYPE_JSON);
        try {
            final SearchRenderData data = new SearchRenderData();
            final SearchApiRequestParams params = new SearchApiRequestParams(request);
            searchService.search(request, params, data);
            query = params.getQuery();
            final String execTime = data.getExecTime();
            final String queryTime = Long.toString(data.getQueryTime());
            final String pageSize = Integer.toString(data.getPageSize());
            final String currentPageNumber = Integer.toString(data.getCurrentPageNumber());
            final String allRecordCount = Long.toString(data.getAllRecordCount());
            final String allPageCount = Integer.toString(data.getAllPageCount());
            final List<Map<String, Object>> documentItems = data.getDocumentItems();
            final FacetResponse facetResponse = data.getFacetResponse();

            buf.append("\"query\":");
            buf.append(escapeJson(query));
            buf.append(",\"execTime\":");
            buf.append(execTime);
            buf.append(",\"queryTime\":");
            buf.append(queryTime);
            buf.append(',');
            buf.append("\"pageSize\":");
            buf.append(pageSize);
            buf.append(',');
            buf.append("\"pageNumber\":");
            buf.append(currentPageNumber);
            buf.append(',');
            buf.append("\"recordCount\":");
            buf.append(allRecordCount);
            buf.append(',');
            buf.append("\"pageCount\":");
            buf.append(allPageCount);
            if (!documentItems.isEmpty()) {
                buf.append(',');
                buf.append("\"result\":[");
                boolean first1 = true;
                for (final Map<String, Object> document : documentItems) {
                    if (!first1) {
                        buf.append(',');
                    } else {
                        first1 = false;
                    }
                    buf.append('{');
                    boolean first2 = true;
                    for (final Map.Entry<String, Object> entry : document.entrySet()) {
                        final String name = entry.getKey();
                        if (StringUtil.isNotBlank(name) && entry.getValue() != null
                                && ComponentUtil.getQueryHelper().isApiResponseField(name)) {
                            if (!first2) {
                                buf.append(',');
                            } else {
                                first2 = false;
                            }
                            buf.append(escapeJson(name));
                            buf.append(':');
                            buf.append(escapeJson(entry.getValue()));
                        }
                    }
                    buf.append('}');
                }
                buf.append(']');
            }
            if (facetResponse != null && facetResponse.hasFacetResponse()) {
                // facet field
                if (facetResponse.getFieldList() != null) {
                    buf.append(',');
                    buf.append("\"facetField\":[");
                    boolean first1 = true;
                    for (final Field field : facetResponse.getFieldList()) {
                        if (!first1) {
                            buf.append(',');
                        } else {
                            first1 = false;
                        }
                        buf.append("{\"name\":");
                        buf.append(escapeJson(field.getName()));
                        buf.append(",\"result\":[");
                        boolean first2 = true;
                        for (final Map.Entry<String, Long> entry : field.getValueCountMap().entrySet()) {
                            if (!first2) {
                                buf.append(',');
                            } else {
                                first2 = false;
                            }
                            buf.append("{\"value\":");
                            buf.append(escapeJson(entry.getKey()));
                            buf.append(",\"count\":");
                            buf.append(entry.getValue());
                            buf.append('}');
                        }
                        buf.append(']');
                        buf.append('}');
                    }
                    buf.append(']');
                }
                // facet query
                if (facetResponse.getQueryCountMap() != null) {
                    buf.append(',');
                    buf.append("\"facetQuery\":[");
                    boolean first1 = true;
                    for (final Map.Entry<String, Long> entry : facetResponse.getQueryCountMap().entrySet()) {
                        if (!first1) {
                            buf.append(',');
                        } else {
                            first1 = false;
                        }
                        buf.append("{\"value\":");
                        buf.append(escapeJson(entry.getKey()));
                        buf.append(",\"count\":");
                        buf.append(entry.getValue());
                        buf.append('}');
                    }
                    buf.append(']');
                }
            }
        } catch (final Exception e) {
            status = 1;
            errMsg = e.getMessage();
            if (errMsg == null) {
                errMsg = e.getClass().getName();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to process a search request.", e);
            }
        }

        writeJsonResponse(status, buf.toString(), errMsg);

    }

    protected void processLabelRequest(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) {
        final LabelTypeHelper labelTypeHelper = ComponentUtil.getLabelTypeHelper();

        int status = 0;
        String errMsg = StringUtil.EMPTY;
        final StringBuilder buf = new StringBuilder(255);
        try {
            final List<Map<String, String>> labelTypeItems = labelTypeHelper.getLabelTypeItemList();
            buf.append("\"recordCount\":");
            buf.append(labelTypeItems.size());
            if (!labelTypeItems.isEmpty()) {
                buf.append(',');
                buf.append("\"result\":[");
                boolean first1 = true;
                for (final Map<String, String> labelMap : labelTypeItems) {
                    if (!first1) {
                        buf.append(',');
                    } else {
                        first1 = false;
                    }
                    buf.append("{\"label\":");
                    buf.append(escapeJson(labelMap.get(Constants.ITEM_LABEL)));
                    buf.append(", \"value\":");
                    buf.append(escapeJson(labelMap.get(Constants.ITEM_VALUE)));
                    buf.append('}');
                }
                buf.append(']');
            }
        } catch (final Exception e) {
            status = 1;
            errMsg = e.getMessage();
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to process a label request.", e);
            }
        }

        writeJsonResponse(status, buf.toString(), errMsg);

    }

    protected void processHotSearchWordRequest(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) {
        if (Constants.FALSE.equals(crawlerProperties.getProperty(Constants.WEB_API_HOT_SEARCH_WORD_PROPERTY, Constants.TRUE))) {
            writeJsonResponse(9, null, "Unsupported operation.");
            return;
        }

        final HotSearchWordHelper hotSearchWordHelper = ComponentUtil.getHotSearchWordHelper();

        int status = 0;
        String errMsg = StringUtil.EMPTY;
        final StringBuilder buf = new StringBuilder(255);
        try {
            final List<String> hotSearchWordList =
                    hotSearchWordHelper.getHotSearchWordList(Range.parseRange(request.getParameter("range")));

            buf.append("\"result\":[");
            boolean first1 = true;
            for (final String word : hotSearchWordList) {
                if (!first1) {
                    buf.append(',');
                } else {
                    first1 = false;
                }
                buf.append(escapeJson(word));
            }
            buf.append(']');
        } catch (final Exception e) {
            if (e instanceof WebApiException) {
                status = ((WebApiException) e).getStatusCode();
            } else {
                status = 1;
            }
            errMsg = e.getMessage();
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to process a hotSearchWord request.", e);
            }
        }

        writeJsonResponse(status, buf.toString(), errMsg);

    }

    protected void processFavoriteRequest(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) {
        if (Constants.FALSE.equals(crawlerProperties.getProperty(Constants.USER_FAVORITE_PROPERTY, Constants.FALSE))) {
            writeJsonResponse(9, null, "Unsupported operation.");
            return;
        }

        final UserInfoHelper userInfoHelper = ComponentUtil.getUserInfoHelper();
        final FieldHelper fieldHelper = ComponentUtil.getFieldHelper();
        final SearchService searchService = ComponentUtil.getComponent(SearchService.class);
        final FavoriteLogService favoriteLogService = ComponentUtil.getComponent(FavoriteLogService.class);

        try {
            final String docId = request.getParameter("docId");
            final String queryId = request.getParameter("queryId");

            final String[] docIds = userInfoHelper.getResultDocIds(URLDecoder.decode(queryId, Constants.UTF_8));
            if (docIds == null) {
                throw new WebApiException(6, "No searched urls.");
            }

            searchService
                    .getDocumentByDocId(docId, new String[] { fieldHelper.idField, fieldHelper.urlField, fieldHelper.favoriteCountField })
                    .ifPresent(doc -> {
                        final String favoriteUrl = doc == null ? null : DocumentUtil.getValue(doc, fieldHelper.urlField, String.class);
                        final String userCode = userInfoHelper.getUserCode();

                        if (StringUtil.isBlank(userCode)) {
                            throw new WebApiException(2, "No user session.");
                        } else if (StringUtil.isBlank(favoriteUrl)) {
                            throw new WebApiException(2, "URL is null.");
                        }

                        boolean found = false;
                        for (final String id : docIds) {
                            if (docId.equals(id)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            throw new WebApiException(5, "Not found: " + favoriteUrl);
                        }

                        if (!favoriteLogService.addUrl(userCode, favoriteUrl)) {
                            throw new WebApiException(4, "Failed to add url: " + favoriteUrl);
                        }

                        final String id = DocumentUtil.getValue(doc, fieldHelper.idField, String.class);
                        final Long count = DocumentUtil.getValue(doc, fieldHelper.favoriteCountField, Long.class);
                        if (count != null) {
                            searchService.update(id, fieldHelper.favoriteCountField, count.longValue() + 1);
                        } else {
                            throw new WebApiException(7, "Failed to update count: " + favoriteUrl);
                        }

                        writeJsonResponse(0, "\"result\":\"ok\"", null);

                    }).orElse(() -> {
                        throw new WebApiException(6, "Not found: " + docId);
                    });

        } catch (final Exception e) {
            int status;
            if (e instanceof WebApiException) {
                status = ((WebApiException) e).getStatusCode();
            } else {
                status = 1;
            }
            writeJsonResponse(status, null, e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to process a favorite request.", e);
            }
        }

    }

    protected void processFavoritesRequest(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) {
        if (Constants.FALSE.equals(crawlerProperties.getProperty(Constants.USER_FAVORITE_PROPERTY, Constants.FALSE))) {
            writeJsonResponse(9, null, "Unsupported operation.");
            return;
        }

        final UserInfoHelper userInfoHelper = ComponentUtil.getUserInfoHelper();
        final FieldHelper fieldHelper = ComponentUtil.getFieldHelper();
        final SearchService searchService = ComponentUtil.getComponent(SearchService.class);
        final FavoriteLogService favoriteLogService = ComponentUtil.getComponent(FavoriteLogService.class);

        int status = 0;
        String body = null;
        String errMsg = null;

        try {
            final String queryId = request.getParameter("queryId");
            final String userCode = userInfoHelper.getUserCode();

            if (StringUtil.isBlank(userCode)) {
                throw new WebApiException(2, "No user session.");
            } else if (StringUtil.isBlank(queryId)) {
                throw new WebApiException(3, "Query ID is null.");
            }

            final String[] docIds = userInfoHelper.getResultDocIds(queryId);
            final List<Map<String, Object>> docList =
                    searchService.getDocumentListByDocIds(docIds, new String[] { fieldHelper.urlField, fieldHelper.docIdField,
                            fieldHelper.favoriteCountField });
            List<String> urlList = new ArrayList<>(docList.size());
            for (final Map<String, Object> doc : docList) {
                final String urlObj = DocumentUtil.getValue(doc, fieldHelper.urlField, String.class);
                if (urlObj != null) {
                    urlList.add(urlObj.toString());
                }
            }
            urlList = favoriteLogService.getUrlList(userCode, urlList);
            final List<String> docIdList = new ArrayList<>(urlList.size());
            for (final Map<String, Object> doc : docList) {
                final String urlObj = DocumentUtil.getValue(doc, fieldHelper.urlField, String.class);
                if (urlObj != null && urlList.contains(urlObj)) {
                    final String docIdObj = DocumentUtil.getValue(doc, fieldHelper.docIdField, String.class);
                    if (docIdObj != null) {
                        docIdList.add(docIdObj);
                    }
                }
            }

            final StringBuilder buf = new StringBuilder();
            buf.append("\"num\":").append(docIdList.size());
            if (!docIdList.isEmpty()) {
                buf.append(", \"docIds\":[");
                for (int i = 0; i < docIdList.size(); i++) {
                    if (i > 0) {
                        buf.append(',');
                    }
                    buf.append(escapeJson(docIdList.get(i)));
                }
                buf.append(']');
            }
            body = buf.toString();
        } catch (final Exception e) {
            if (e instanceof WebApiException) {
                status = ((WebApiException) e).getStatusCode();
            } else {
                status = 1;
            }
            errMsg = e.getMessage();
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to process a favorites request.", e);
            }
        }

        writeJsonResponse(status, body, errMsg);

    }

    protected void writeJsonResponse(final int status, final String body, final String errMsg) {
        final String callback = LaRequestUtil.getRequest().getParameter("callback");
        final boolean isJsonp = StringUtil.isNotBlank(callback);

        final StringBuilder buf = new StringBuilder(1000);
        if (isJsonp) {
            buf.append(escapeCallbackName(callback));
            buf.append('(');
        }
        buf.append("{\"response\":");
        buf.append("{\"version\":");
        buf.append(Constants.WEB_API_VERSION);
        buf.append(',');
        buf.append("\"status\":");
        buf.append(status);
        if (status == 0) {
            if (StringUtil.isNotBlank(body)) {
                buf.append(',');
                buf.append(body);
            }
        } else {
            buf.append(',');
            buf.append("\"message\":");
            buf.append(escapeJson(errMsg));
        }
        buf.append('}');
        buf.append('}');
        if (isJsonp) {
            buf.append(')');
        }
        write(buf.toString(), "text/javascript+json", Constants.UTF_8);

    }

    protected String escapeCallbackName(final String callbackName) {
        return "/**/" + callbackName.replaceAll("[^0-9a-zA-Z_\\$\\.]", StringUtil.EMPTY);
    }

    protected String escapeJson(final Object obj) {
        if (obj == null) {
            return "null";
        }

        final StringBuilder buf = new StringBuilder(255);
        if (obj instanceof List<?>) {
            buf.append('[');
            boolean first = true;
            for (final Object child : (List<?>) obj) {
                if (first) {
                    first = false;
                } else {
                    buf.append(',');
                }
                buf.append(escapeJson(child));
            }
            buf.append(']');
        } else if (obj instanceof Map<?, ?>) {
            buf.append('{');
            boolean first = true;
            for (final Map.Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet()) {
                if (first) {
                    first = false;
                } else {
                    buf.append(',');
                }
                buf.append(escapeJson(entry.getKey())).append(':').append(escapeJson(entry.getValue()));
            }
            buf.append('}');
        } else if (obj instanceof Integer || obj instanceof Long || obj instanceof Float || obj instanceof Double || obj instanceof Short) {
            buf.append(obj);
        } else if (obj instanceof Date) {
            final SimpleDateFormat sdf = new SimpleDateFormat(CoreLibConstants.DATE_FORMAT_ISO_8601_EXTEND);
            buf.append('\"').append(StringEscapeUtils.escapeXml(sdf.format(obj))).append('\"');
        } else {
            buf.append('\"').append(escapeJsonString(obj.toString())).append('\"');
        }
        return buf.toString();
    }

    protected String escapeJsonString(final String str) {

        final StringWriter out = new StringWriter(str.length() * 2);
        int sz;
        sz = str.length();
        for (int i = 0; i < sz; i++) {
            final char ch = str.charAt(i);

            // handle unicode
            if (ch > 0xfff) {
                out.write("\\u");
                out.write(hex(ch));
            } else if (ch > 0xff) {
                out.write("\\u0");
                out.write(hex(ch));
            } else if (ch > 0x7f) {
                out.write("\\u00");
                out.write(hex(ch));
            } else if (ch < 32) {
                switch (ch) {
                case '\b':
                    out.write('\\');
                    out.write('b');
                    break;
                case '\n':
                    out.write('\\');
                    out.write('n');
                    break;
                case '\t':
                    out.write('\\');
                    out.write('t');
                    break;
                case '\f':
                    out.write('\\');
                    out.write('f');
                    break;
                case '\r':
                    out.write('\\');
                    out.write('r');
                    break;
                default:
                    if (ch > 0xf) {
                        out.write("\\u00");
                        out.write(hex(ch));
                    } else {
                        out.write("\\u000");
                        out.write(hex(ch));
                    }
                    break;
                }
            } else {
                switch (ch) {
                case '"':
                    out.write("\\u0022");
                    break;
                case '\\':
                    out.write("\\u005C");
                    break;
                case '/':
                    out.write("\\u002F");
                    break;
                default:
                    out.write(ch);
                    break;
                }
            }
        }
        return out.toString();
    }

    private String hex(final char ch) {
        return Integer.toHexString(ch).toUpperCase();
    }

    protected static class SearchApiRequestParams implements SearchRequestParams {

        private final HttpServletRequest request;

        private int startPosition = -1;

        private int pageSize = -1;

        protected SearchApiRequestParams(final HttpServletRequest request) {
            this.request = request;
        }

        @Override
        public String getQuery() {
            return request.getParameter("query");
        }

        @Override
        public String getOperator() {
            return request.getParameter("op");
        }

        @Override
        public String[] getAdditional() {
            return request.getParameterValues("additional");
        }

        @Override
        public Map<String, String[]> getFields() {
            // TODO Auto-generated method stub
            return new HashMap<>();
        }

        @Override
        public String[] getLanguages() {
            return request.getParameterValues("lang");
        }

        @Override
        public GeoInfo getGeoInfo() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public FacetInfo getFacetInfo() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getSort() {
            return request.getParameter("sort");
        }

        @Override
        public int getStartPosition() {
            if (startPosition != -1) {
                return startPosition;
            }

            final String start = request.getParameter("start");
            final QueryHelper queryHelper = ComponentUtil.getQueryHelper();
            if (StringUtil.isBlank(start)) {
                startPosition = queryHelper.getDefaultStart();
            } else {
                try {
                    startPosition = Integer.parseInt(start);
                } catch (final NumberFormatException e) {
                    startPosition = queryHelper.getDefaultStart();
                }
            }
            return startPosition;
        }

        @Override
        public int getPageSize() {
            if (pageSize != -1) {
                return pageSize;
            }

            final String num = request.getParameter("num");
            final QueryHelper queryHelper = ComponentUtil.getQueryHelper();
            if (StringUtil.isBlank(num)) {
                pageSize = queryHelper.getDefaultPageSize();
            } else {
                try {
                    pageSize = Integer.parseInt(num);
                    if (pageSize > queryHelper.getMaxPageSize() || pageSize <= 0) {
                        pageSize = queryHelper.getMaxPageSize();
                    }
                } catch (final NumberFormatException e) {
                    pageSize = queryHelper.getDefaultPageSize();
                }
            }
            return pageSize;
        }

    }
}