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
package org.codelibs.fess.es.config.cbean.cq.bs;

import java.time.LocalDateTime;
import java.util.Collection;

import org.codelibs.fess.es.config.allcommon.EsAbstractConditionQuery;
import org.codelibs.fess.es.config.cbean.cq.ElevateWordToLabelCQ;
import org.dbflute.cbean.ckey.ConditionKey;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FuzzyQueryBuilder;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.PrefixQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;

/**
 * @author ESFlute (using FreeGen)
 */
public abstract class BsElevateWordToLabelCQ extends EsAbstractConditionQuery {

    protected static final Class<?> suppressUnusedImportLocalDateTime = LocalDateTime.class;

    // ===================================================================================
    //                                                                       Name Override
    //                                                                       =============
    @Override
    public String asTableDbName() {
        return "elevate_word_to_label";
    }

    @Override
    public String xgetAliasName() {
        return "elevate_word_to_label";
    }

    // ===================================================================================
    //                                                                       Query Control
    //                                                                       =============
    public void filtered(FilteredCall<ElevateWordToLabelCQ, ElevateWordToLabelCQ> filteredLambda) {
        filtered(filteredLambda, null);
    }

    public void filtered(FilteredCall<ElevateWordToLabelCQ, ElevateWordToLabelCQ> filteredLambda,
            ConditionOptionCall<BoolQueryBuilder> opLambda) {
        bool((must, should, mustNot, filter) -> {
            filteredLambda.callback(must, filter);
        }, opLambda);
    }

    public void not(OperatorCall<ElevateWordToLabelCQ> notLambda) {
        not(notLambda, null);
    }

    public void not(final OperatorCall<ElevateWordToLabelCQ> notLambda, final ConditionOptionCall<BoolQueryBuilder> opLambda) {
        bool((must, should, mustNot, filter) -> notLambda.callback(mustNot), opLambda);
    }

    public void bool(BoolCall<ElevateWordToLabelCQ> boolLambda) {
        bool(boolLambda, null);
    }

    public void bool(BoolCall<ElevateWordToLabelCQ> boolLambda, ConditionOptionCall<BoolQueryBuilder> opLambda) {
        ElevateWordToLabelCQ mustQuery = new ElevateWordToLabelCQ();
        ElevateWordToLabelCQ shouldQuery = new ElevateWordToLabelCQ();
        ElevateWordToLabelCQ mustNotQuery = new ElevateWordToLabelCQ();
        ElevateWordToLabelCQ filterQuery = new ElevateWordToLabelCQ();
        boolLambda.callback(mustQuery, shouldQuery, mustNotQuery, filterQuery);
        if (mustQuery.hasQueries() || shouldQuery.hasQueries() || mustNotQuery.hasQueries() || filterQuery.hasQueries()) {
            BoolQueryBuilder builder =
                    regBoolCQ(mustQuery.getQueryBuilderList(), shouldQuery.getQueryBuilderList(), mustNotQuery.getQueryBuilderList(),
                            filterQuery.getQueryBuilderList());
            if (opLambda != null) {
                opLambda.callback(builder);
            }
        }
    }

    // ===================================================================================
    //                                                                           Query Set
    //                                                                           =========
    public void setId_Equal(String id) {
        setId_Term(id, null);
    }

    public void setId_Equal(String id, ConditionOptionCall<TermQueryBuilder> opLambda) {
        setId_Term(id, opLambda);
    }

    public void setId_Term(String id) {
        setId_Term(id, null);
    }

    public void setId_Term(String id, ConditionOptionCall<TermQueryBuilder> opLambda) {
        TermQueryBuilder builder = regTermQ("_id", id);
        if (opLambda != null) {
            opLambda.callback(builder);
        }
    }

    public void setId_NotEqual(String id) {
        setId_NotTerm(id, null);
    }

    public void setId_NotTerm(String id) {
        setId_NotTerm(id, null);
    }

    public void setId_NotEqual(String id, ConditionOptionCall<BoolQueryBuilder> opLambda) {
        setId_NotTerm(id, opLambda);
    }

    public void setId_NotTerm(String id, ConditionOptionCall<BoolQueryBuilder> opLambda) {
        not(not -> not.setId_Term(id), opLambda);
    }

    public void setId_Terms(Collection<String> idList) {
        setId_Terms(idList, null);
    }

    public void setId_Terms(Collection<String> idList, ConditionOptionCall<IdsQueryBuilder> opLambda) {
        IdsQueryBuilder builder = regIdsQ(idList);
        if (opLambda != null) {
            opLambda.callback(builder);
        }
    }

    public void setId_InScope(Collection<String> idList) {
        setId_Terms(idList, null);
    }

    public void setId_InScope(Collection<String> idList, ConditionOptionCall<IdsQueryBuilder> opLambda) {
        setId_Terms(idList, opLambda);
    }

    public BsElevateWordToLabelCQ addOrderBy_Id_Asc() {
        regOBA("_id");
        return this;
    }

    public BsElevateWordToLabelCQ addOrderBy_Id_Desc() {
        regOBD("_id");
        return this;
    }

    public void setElevateWordId_Equal(String elevateWordId) {
        setElevateWordId_Term(elevateWordId, null);
    }

    public void setElevateWordId_Equal(String elevateWordId, ConditionOptionCall<TermQueryBuilder> opLambda) {
        setElevateWordId_Term(elevateWordId, opLambda);
    }

    public void setElevateWordId_Term(String elevateWordId) {
        setElevateWordId_Term(elevateWordId, null);
    }

    public void setElevateWordId_Term(String elevateWordId, ConditionOptionCall<TermQueryBuilder> opLambda) {
        TermQueryBuilder builder = regTermQ("elevateWordId", elevateWordId);
        if (opLambda != null) {
            opLambda.callback(builder);
        }
    }

    public void setElevateWordId_NotEqual(String elevateWordId) {
        setElevateWordId_NotTerm(elevateWordId, null);
    }

    public void setElevateWordId_NotTerm(String elevateWordId) {
        setElevateWordId_NotTerm(elevateWordId, null);
    }

    public void setElevateWordId_NotEqual(String elevateWordId, ConditionOptionCall<BoolQueryBuilder> opLambda) {
        setElevateWordId_NotTerm(elevateWordId, opLambda);
    }

    public void setElevateWordId_NotTerm(String elevateWordId, ConditionOptionCall<BoolQueryBuilder> opLambda) {
        not(not -> not.setElevateWordId_Term(elevateWordId), opLambda);
    }

    public void setElevateWordId_Terms(Collection<String> elevateWordIdList) {
        setElevateWordId_Terms(elevateWordIdList, null);
    }

    public void setElevateWordId_Terms(Collection<String> elevateWordIdList, ConditionOptionCall<TermsQueryBuilder> opLambda) {
        TermsQueryBuilder builder = regTermsQ("elevateWordId", elevateWordIdList);
        if (opLambda != null) {
            opLambda.callback(builder);
        }
    }

    public void setElevateWordId_InScope(Collection<String> elevateWordIdList) {
        setElevateWordId_Terms(elevateWordIdList, null);
    }

    public void setElevateWordId_InScope(Collection<String> elevateWordIdList, ConditionOptionCall<TermsQueryBuilder> opLambda) {
        setElevateWordId_Terms(elevateWordIdList, opLambda);
    }

    public void setElevateWordId_Match(String elevateWordId) {
        setElevateWordId_Match(elevateWordId, null);
    }

    public void setElevateWordId_Match(String elevateWordId, ConditionOptionCall<MatchQueryBuilder> opLambda) {
        MatchQueryBuilder builder = regMatchQ("elevateWordId", elevateWordId);
        if (opLambda != null) {
            opLambda.callback(builder);
        }
    }

    public void setElevateWordId_MatchPhrase(String elevateWordId) {
        setElevateWordId_MatchPhrase(elevateWordId, null);
    }

    public void setElevateWordId_MatchPhrase(String elevateWordId, ConditionOptionCall<MatchQueryBuilder> opLambda) {
        MatchQueryBuilder builder = regMatchPhraseQ("elevateWordId", elevateWordId);
        if (opLambda != null) {
            opLambda.callback(builder);
        }
    }

    public void setElevateWordId_MatchPhrasePrefix(String elevateWordId) {
        setElevateWordId_MatchPhrasePrefix(elevateWordId, null);
    }

    public void setElevateWordId_MatchPhrasePrefix(String elevateWordId, ConditionOptionCall<MatchQueryBuilder> opLambda) {
        MatchQueryBuilder builder = regMatchPhrasePrefixQ("elevateWordId", elevateWordId);
        if (opLambda != null) {
            opLambda.callback(builder);
        }
    }

    public void setElevateWordId_Fuzzy(String elevateWordId) {
        setElevateWordId_Fuzzy(elevateWordId, null);
    }

    public void setElevateWordId_Fuzzy(String elevateWordId, ConditionOptionCall<FuzzyQueryBuilder> opLambda) {
        FuzzyQueryBuilder builder = regFuzzyQ("elevateWordId", elevateWordId);
        if (opLambda != null) {
            opLambda.callback(builder);
        }
    }

    public void setElevateWordId_Prefix(String elevateWordId) {
        setElevateWordId_Prefix(elevateWordId, null);
    }

    public void setElevateWordId_Prefix(String elevateWordId, ConditionOptionCall<PrefixQueryBuilder> opLambda) {
        PrefixQueryBuilder builder = regPrefixQ("elevateWordId", elevateWordId);
        if (opLambda != null) {
            opLambda.callback(builder);
        }
    }

    public void setElevateWordId_GreaterThan(String elevateWordId) {
        setElevateWordId_GreaterThan(elevateWordId, null);
    }

    public void setElevateWordId_GreaterThan(String elevateWordId, ConditionOptionCall<RangeQueryBuilder> opLambda) {
        RangeQueryBuilder builder = regRangeQ("elevateWordId", ConditionKey.CK_GREATER_THAN, elevateWordId);
        if (opLambda != null) {
            opLambda.callback(builder);
        }
    }

    public void setElevateWordId_LessThan(String elevateWordId) {
        setElevateWordId_LessThan(elevateWordId, null);
    }

    public void setElevateWordId_LessThan(String elevateWordId, ConditionOptionCall<RangeQueryBuilder> opLambda) {
        RangeQueryBuilder builder = regRangeQ("elevateWordId", ConditionKey.CK_LESS_THAN, elevateWordId);
        if (opLambda != null) {
            opLambda.callback(builder);
        }
    }

    public void setElevateWordId_GreaterEqual(String elevateWordId) {
        setElevateWordId_GreaterEqual(elevateWordId, null);
    }

    public void setElevateWordId_GreaterEqual(String elevateWordId, ConditionOptionCall<RangeQueryBuilder> opLambda) {
        RangeQueryBuilder builder = regRangeQ("elevateWordId", ConditionKey.CK_GREATER_EQUAL, elevateWordId);
        if (opLambda != null) {
            opLambda.callback(builder);
        }
    }

    public void setElevateWordId_LessEqual(String elevateWordId) {
        setElevateWordId_LessEqual(elevateWordId, null);
    }

    public void setElevateWordId_LessEqual(String elevateWordId, ConditionOptionCall<RangeQueryBuilder> opLambda) {
        RangeQueryBuilder builder = regRangeQ("elevateWordId", ConditionKey.CK_LESS_EQUAL, elevateWordId);
        if (opLambda != null) {
            opLambda.callback(builder);
        }
    }

    public BsElevateWordToLabelCQ addOrderBy_ElevateWordId_Asc() {
        regOBA("elevateWordId");
        return this;
    }

    public BsElevateWordToLabelCQ addOrderBy_ElevateWordId_Desc() {
        regOBD("elevateWordId");
        return this;
    }

    public void setLabelTypeId_Equal(String labelTypeId) {
        setLabelTypeId_Term(labelTypeId, null);
    }

    public void setLabelTypeId_Equal(String labelTypeId, ConditionOptionCall<TermQueryBuilder> opLambda) {
        setLabelTypeId_Term(labelTypeId, opLambda);
    }

    public void setLabelTypeId_Term(String labelTypeId) {
        setLabelTypeId_Term(labelTypeId, null);
    }

    public void setLabelTypeId_Term(String labelTypeId, ConditionOptionCall<TermQueryBuilder> opLambda) {
        TermQueryBuilder builder = regTermQ("labelTypeId", labelTypeId);
        if (opLambda != null) {
            opLambda.callback(builder);
        }
    }

    public void setLabelTypeId_NotEqual(String labelTypeId) {
        setLabelTypeId_NotTerm(labelTypeId, null);
    }

    public void setLabelTypeId_NotTerm(String labelTypeId) {
        setLabelTypeId_NotTerm(labelTypeId, null);
    }

    public void setLabelTypeId_NotEqual(String labelTypeId, ConditionOptionCall<BoolQueryBuilder> opLambda) {
        setLabelTypeId_NotTerm(labelTypeId, opLambda);
    }

    public void setLabelTypeId_NotTerm(String labelTypeId, ConditionOptionCall<BoolQueryBuilder> opLambda) {
        not(not -> not.setLabelTypeId_Term(labelTypeId), opLambda);
    }

    public void setLabelTypeId_Terms(Collection<String> labelTypeIdList) {
        setLabelTypeId_Terms(labelTypeIdList, null);
    }

    public void setLabelTypeId_Terms(Collection<String> labelTypeIdList, ConditionOptionCall<TermsQueryBuilder> opLambda) {
        TermsQueryBuilder builder = regTermsQ("labelTypeId", labelTypeIdList);
        if (opLambda != null) {
            opLambda.callback(builder);
        }
    }

    public void setLabelTypeId_InScope(Collection<String> labelTypeIdList) {
        setLabelTypeId_Terms(labelTypeIdList, null);
    }

    public void setLabelTypeId_InScope(Collection<String> labelTypeIdList, ConditionOptionCall<TermsQueryBuilder> opLambda) {
        setLabelTypeId_Terms(labelTypeIdList, opLambda);
    }

    public void setLabelTypeId_Match(String labelTypeId) {
        setLabelTypeId_Match(labelTypeId, null);
    }

    public void setLabelTypeId_Match(String labelTypeId, ConditionOptionCall<MatchQueryBuilder> opLambda) {
        MatchQueryBuilder builder = regMatchQ("labelTypeId", labelTypeId);
        if (opLambda != null) {
            opLambda.callback(builder);
        }
    }

    public void setLabelTypeId_MatchPhrase(String labelTypeId) {
        setLabelTypeId_MatchPhrase(labelTypeId, null);
    }

    public void setLabelTypeId_MatchPhrase(String labelTypeId, ConditionOptionCall<MatchQueryBuilder> opLambda) {
        MatchQueryBuilder builder = regMatchPhraseQ("labelTypeId", labelTypeId);
        if (opLambda != null) {
            opLambda.callback(builder);
        }
    }

    public void setLabelTypeId_MatchPhrasePrefix(String labelTypeId) {
        setLabelTypeId_MatchPhrasePrefix(labelTypeId, null);
    }

    public void setLabelTypeId_MatchPhrasePrefix(String labelTypeId, ConditionOptionCall<MatchQueryBuilder> opLambda) {
        MatchQueryBuilder builder = regMatchPhrasePrefixQ("labelTypeId", labelTypeId);
        if (opLambda != null) {
            opLambda.callback(builder);
        }
    }

    public void setLabelTypeId_Fuzzy(String labelTypeId) {
        setLabelTypeId_Fuzzy(labelTypeId, null);
    }

    public void setLabelTypeId_Fuzzy(String labelTypeId, ConditionOptionCall<FuzzyQueryBuilder> opLambda) {
        FuzzyQueryBuilder builder = regFuzzyQ("labelTypeId", labelTypeId);
        if (opLambda != null) {
            opLambda.callback(builder);
        }
    }

    public void setLabelTypeId_Prefix(String labelTypeId) {
        setLabelTypeId_Prefix(labelTypeId, null);
    }

    public void setLabelTypeId_Prefix(String labelTypeId, ConditionOptionCall<PrefixQueryBuilder> opLambda) {
        PrefixQueryBuilder builder = regPrefixQ("labelTypeId", labelTypeId);
        if (opLambda != null) {
            opLambda.callback(builder);
        }
    }

    public void setLabelTypeId_GreaterThan(String labelTypeId) {
        setLabelTypeId_GreaterThan(labelTypeId, null);
    }

    public void setLabelTypeId_GreaterThan(String labelTypeId, ConditionOptionCall<RangeQueryBuilder> opLambda) {
        RangeQueryBuilder builder = regRangeQ("labelTypeId", ConditionKey.CK_GREATER_THAN, labelTypeId);
        if (opLambda != null) {
            opLambda.callback(builder);
        }
    }

    public void setLabelTypeId_LessThan(String labelTypeId) {
        setLabelTypeId_LessThan(labelTypeId, null);
    }

    public void setLabelTypeId_LessThan(String labelTypeId, ConditionOptionCall<RangeQueryBuilder> opLambda) {
        RangeQueryBuilder builder = regRangeQ("labelTypeId", ConditionKey.CK_LESS_THAN, labelTypeId);
        if (opLambda != null) {
            opLambda.callback(builder);
        }
    }

    public void setLabelTypeId_GreaterEqual(String labelTypeId) {
        setLabelTypeId_GreaterEqual(labelTypeId, null);
    }

    public void setLabelTypeId_GreaterEqual(String labelTypeId, ConditionOptionCall<RangeQueryBuilder> opLambda) {
        RangeQueryBuilder builder = regRangeQ("labelTypeId", ConditionKey.CK_GREATER_EQUAL, labelTypeId);
        if (opLambda != null) {
            opLambda.callback(builder);
        }
    }

    public void setLabelTypeId_LessEqual(String labelTypeId) {
        setLabelTypeId_LessEqual(labelTypeId, null);
    }

    public void setLabelTypeId_LessEqual(String labelTypeId, ConditionOptionCall<RangeQueryBuilder> opLambda) {
        RangeQueryBuilder builder = regRangeQ("labelTypeId", ConditionKey.CK_LESS_EQUAL, labelTypeId);
        if (opLambda != null) {
            opLambda.callback(builder);
        }
    }

    public BsElevateWordToLabelCQ addOrderBy_LabelTypeId_Asc() {
        regOBA("labelTypeId");
        return this;
    }

    public BsElevateWordToLabelCQ addOrderBy_LabelTypeId_Desc() {
        regOBD("labelTypeId");
        return this;
    }

}
