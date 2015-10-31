/*
 * Copyright 2012-2015 CodeLibs Project and the Others.
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
package org.codelibs.fess.es.log.bsentity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.codelibs.fess.es.log.allcommon.EsAbstractEntity;
import org.codelibs.fess.es.log.bsentity.dbmeta.SearchLogDbm;

/**
 * ${table.comment}
 * @author ESFlute (using FreeGen)
 */
public class BsSearchLog extends EsAbstractEntity {

    private static final long serialVersionUID = 1L;
    protected static final Class<?> suppressUnusedImportLocalDateTime = LocalDateTime.class;

    @Override
    public SearchLogDbm asDBMeta() {
        return SearchLogDbm.getInstance();
    }

    @Override
    public String asTableDbName() {
        return "search_log";
    }

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** accessType */
    protected String accessType;

    /** clientIp */
    protected String clientIp;

    /** hitCount */
    protected Long hitCount;

    /** queryOffset */
    protected Integer queryOffset;

    /** queryPageSize */
    protected Integer queryPageSize;

    /** referer */
    protected String referer;

    /** requestedTime */
    protected Long requestedTime;

    /** responseTime */
    protected Integer responseTime;

    /** searchWord */
    protected String searchWord;

    /** userAgent */
    protected String userAgent;

    /** userInfoId */
    protected String userInfoId;

    /** userSessionId */
    protected String userSessionId;

    // [Referrers] *comment only

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getAccessType() {
        checkSpecifiedProperty("accessType");
        return accessType;
    }

    public void setAccessType(String value) {
        registerModifiedProperty("accessType");
        this.accessType = value;
    }

    public String getClientIp() {
        checkSpecifiedProperty("clientIp");
        return clientIp;
    }

    public void setClientIp(String value) {
        registerModifiedProperty("clientIp");
        this.clientIp = value;
    }

    public Long getHitCount() {
        checkSpecifiedProperty("hitCount");
        return hitCount;
    }

    public void setHitCount(Long value) {
        registerModifiedProperty("hitCount");
        this.hitCount = value;
    }

    public String getId() {
        checkSpecifiedProperty("id");
        return asDocMeta().id();
    }

    public void setId(String value) {
        registerModifiedProperty("id");
        asDocMeta().id(value);
    }

    public Integer getQueryOffset() {
        checkSpecifiedProperty("queryOffset");
        return queryOffset;
    }

    public void setQueryOffset(Integer value) {
        registerModifiedProperty("queryOffset");
        this.queryOffset = value;
    }

    public Integer getQueryPageSize() {
        checkSpecifiedProperty("queryPageSize");
        return queryPageSize;
    }

    public void setQueryPageSize(Integer value) {
        registerModifiedProperty("queryPageSize");
        this.queryPageSize = value;
    }

    public String getReferer() {
        checkSpecifiedProperty("referer");
        return referer;
    }

    public void setReferer(String value) {
        registerModifiedProperty("referer");
        this.referer = value;
    }

    public Long getRequestedTime() {
        checkSpecifiedProperty("requestedTime");
        return requestedTime;
    }

    public void setRequestedTime(Long value) {
        registerModifiedProperty("requestedTime");
        this.requestedTime = value;
    }

    public Integer getResponseTime() {
        checkSpecifiedProperty("responseTime");
        return responseTime;
    }

    public void setResponseTime(Integer value) {
        registerModifiedProperty("responseTime");
        this.responseTime = value;
    }

    public String getSearchWord() {
        checkSpecifiedProperty("searchWord");
        return searchWord;
    }

    public void setSearchWord(String value) {
        registerModifiedProperty("searchWord");
        this.searchWord = value;
    }

    public String getUserAgent() {
        checkSpecifiedProperty("userAgent");
        return userAgent;
    }

    public void setUserAgent(String value) {
        registerModifiedProperty("userAgent");
        this.userAgent = value;
    }

    public String getUserInfoId() {
        checkSpecifiedProperty("userInfoId");
        return userInfoId;
    }

    public void setUserInfoId(String value) {
        registerModifiedProperty("userInfoId");
        this.userInfoId = value;
    }

    public String getUserSessionId() {
        checkSpecifiedProperty("userSessionId");
        return userSessionId;
    }

    public void setUserSessionId(String value) {
        registerModifiedProperty("userSessionId");
        this.userSessionId = value;
    }

    @Override
    public Map<String, Object> toSource() {
        Map<String, Object> sourceMap = new HashMap<>();
        if (accessType != null) {
            sourceMap.put("accessType", accessType);
        }
        if (clientIp != null) {
            sourceMap.put("clientIp", clientIp);
        }
        if (hitCount != null) {
            sourceMap.put("hitCount", hitCount);
        }
        if (asDocMeta().id() != null) {
            sourceMap.put("id", asDocMeta().id());
        }
        if (queryOffset != null) {
            sourceMap.put("queryOffset", queryOffset);
        }
        if (queryPageSize != null) {
            sourceMap.put("queryPageSize", queryPageSize);
        }
        if (referer != null) {
            sourceMap.put("referer", referer);
        }
        if (requestedTime != null) {
            sourceMap.put("requestedTime", requestedTime);
        }
        if (responseTime != null) {
            sourceMap.put("responseTime", responseTime);
        }
        if (searchWord != null) {
            sourceMap.put("searchWord", searchWord);
        }
        if (userAgent != null) {
            sourceMap.put("userAgent", userAgent);
        }
        if (userInfoId != null) {
            sourceMap.put("userInfoId", userInfoId);
        }
        if (userSessionId != null) {
            sourceMap.put("userSessionId", userSessionId);
        }
        return sourceMap;
    }
}