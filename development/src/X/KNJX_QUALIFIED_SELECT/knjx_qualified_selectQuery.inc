<?php

require_once('for_php7.php');

class knjx_qualified_selectQuery extends Query {
    //学籍資格データよりデータを取得
    function getAward($model, $db) {
        $query  = "";
        if ($model->send_prgid == 'KNJE020') {
            $query .= " WITH T_YEAR AS ( ";
            $query .= "     SELECT DISTINCT ";
            $query .= "         R1.YEAR ";
            $query .= "     FROM ";
            $query .= "         SCHREG_REGD_DAT T1 ";
            $query .= "         INNER JOIN NAME_MST T2 ON T2.NAMECD1 = 'A023' AND T1.GRADE BETWEEN T2.NAME2 AND T2.NAME3, ";
            $query .= "         SCHREG_REGD_DAT R1 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR     = '".$model->exp_year."' AND ";
            $query .= "         T1.SEMESTER = '".$model->exp_semester."' AND ";
            $query .= "         T1.SCHREGNO = '".$model->schregno."' AND ";
            $query .= "         T1.SCHREGNO = R1.SCHREGNO AND ";
            $query .= "         R1.GRADE BETWEEN T2.NAME2 AND T2.NAME3 ";
            $query .= " ) ";
        }
        if ($model->Properties["useQualifiedMst"] == '1') {
            $query .= " SELECT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.SEQ, ";
            $query .= "     T1.REGDDATE, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
            } else {
                $query .= "     T1.SUBCLASSCD, ";
            }
            $query .= "     CASE L1.CONDITION_DIV ";
            $query .= "          WHEN '1' ";
            $query .= "          THEN '国家資格' ";
            $query .= "          WHEN '2' ";
            $query .= "          THEN '公的資格' ";
            $query .= "          WHEN '3' ";
            $query .= "          THEN '民間資格' ";
            $query .= "          ELSE 'その他' ";
            $query .= "     END AS CONDITION_DIV, ";
            $query .= "     T1.QUALIFIED_CD, ";
            $query .= "     L1.QUALIFIED_NAME AS QUALIFIED_SHOW, ";
            $query .= "     L1.QUALIFIED_ABBV, ";
            $query .= "     L1.PROMOTER, ";
            $query .= "     T1.CONTENTS, ";
            $query .= "     L2.NAME1 AS RANK, ";
            $query .= "     T1.SCORE, ";
            $query .= "     T1.REMARK, ";
            $query .= "     T1.CREDITS ";
            $query .= " FROM ";
            $query .= "     SCHREG_QUALIFIED_HOBBY_DAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     QUALIFIED_MST L1 ON L1.QUALIFIED_CD = T1.QUALIFIED_CD ";
            $query .= " LEFT JOIN ";
            $query .= "     NAME_MST L2 ON  L2.NAMECD2 = T1.RANK ";
            $query .= "                 AND L2.NAMECD1 = 'H312' ";
            if (0 < $db->getOne(knjx_qualified_selectQuery::getSyscatColumns("QUALIFIED_RANK_DAT"))) {
                $query .= " LEFT JOIN ";
                $query .= "     QUALIFIED_RANK_DAT L3 ON L3.QUALIFIED_CD = T1.QUALIFIED_CD ";
                $query .= "                          AND L3.RANK = T1.RANK ";
            }
            $query .= " WHERE ";
            if ($model->send_prgid == 'KNJE020') {
                $query .= "     T1.YEAR IN (SELECT YEAR FROM T_YEAR) AND ";
            } else {
                $query .= "     T1.YEAR     = '{$model->exp_year}' AND ";
            }
            $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
            if (0 < $db->getOne(knjx_qualified_selectQuery::getSyscatColumns("QUALIFIED_RANK_DAT"))) {
                $query .= "    AND L3.NOT_PRINT IS NULL ";
            }
            if ($model->Properties["useSchregQualified"] == 'SUBCLASS_QUALIFIED_TEST_DAT') {
                $query .= " UNION ";
                $query .= " SELECT ";
                $query .= "     T1.YEAR, ";
                $query .= "     T1.SCHREGNO, ";
                $query .= "     0 AS SEQ, ";
                $query .= "     T1.TEST_DATE AS REGDDATE, ";
                $query .= "     '00-00-00-000000' AS SUBCLASSCD, ";
                $query .= "     CASE L1.CONDITION_DIV ";
                $query .= "          WHEN '1' ";
                $query .= "          THEN '国家資格' ";
                $query .= "          WHEN '2' ";
                $query .= "          THEN '公的資格' ";
                $query .= "          WHEN '3' ";
                $query .= "          THEN '民間資格' ";
                $query .= "          ELSE 'その他' ";
                $query .= "     END AS CONDITION_DIV, ";
                $query .= "     T1.QUALIFIED_CD, ";
                $query .= "     L1.QUALIFIED_NAME AS QUALIFIED_SHOW, ";
                $query .= "     L1.QUALIFIED_ABBV, ";
                $query .= "     L1.PROMOTER, ";
                $query .= "     '' AS CONTENTS, ";
                $query .= "     L2.TEST_NAME AS RANK, ";
                $query .= "     NULL AS SCORE, ";
                $query .= "     N1.NAME1 AS REMARK, ";
                $query .= "     0 AS CREDITS ";
                $query .= " FROM ";
                $query .= "     SCHREG_QUALIFIED_TEST_DAT T1 ";
                $query .= "     LEFT JOIN QUALIFIED_MST L1 ON L1.QUALIFIED_CD = T1.QUALIFIED_CD ";
                $query .= "     LEFT JOIN QUALIFIED_TEST_MST L2 ";
                $query .= "          ON L2.YEAR         = T1.YEAR ";
                $query .= "         AND L2.QUALIFIED_CD = T1.QUALIFIED_CD ";
                $query .= "         AND L2.TEST_CD      = T1.TEST_CD ";
                $query .= "     LEFT JOIN NAME_MST N1 ";
                $query .= "          ON N1.NAMECD1      = 'Z050' ";
                $query .= "         AND SUBSTR(T1.RESULT_CD, 4) = N1.NAMECD2 ";
                if (0 < $db->getOne(knjx_qualified_selectQuery::getSyscatColumns("QUALIFIED_RESULT_MST"))) {
                    $query .= " LEFT JOIN ";
                    $query .= "     QUALIFIED_RESULT_MST L3 ON L3.YEAR = T1.YEAR ";
                    $query .= "                            AND L3.QUALIFIED_CD = T1.QUALIFIED_CD ";
                    $query .= "                            AND L3.RESULT_CD = T1.RESULT_CD ";
                }
                $query .= " WHERE ";
                if ($model->send_prgid == 'KNJE020') {
                    $query .= "     T1.YEAR IN (SELECT YEAR FROM T_YEAR) AND ";
                } else {
                    $query .= "     T1.YEAR     = '{$model->exp_year}' AND ";
                }
                $query .= "     T1.SCHREGNO = '{$model->schregno}' AND ";
                $query .= "     T1.RESULT_CD NOT IN ('8888', '9999') "; //欠席・不合格を除く
                if (0 < $db->getOne(knjx_qualified_selectQuery::getSyscatColumns("QUALIFIED_RESULT_MST"))) {
                    $query .= "    AND L3.NOT_PRINT IS NULL ";
                }
            }
            $query .= " ORDER BY ";
            $query .= "     REGDDATE, ";
            $query .= "     SUBCLASSCD, ";
            $query .= "     SEQ ";
        } else {
            $query .= " SELECT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.REGDDATE, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
            } else {
                $query .= "     T1.SUBCLASSCD, ";
            }
            $query .= "     CASE WHEN T1.CONDITION_DIV = '1' ";
            $query .= "          THEN T1.CONDITION_DIV || ':資格' ";
            $query .= "          ELSE T1.CONDITION_DIV || ':その他' ";
            $query .= "          END AS CONDITION_DIV, ";
            $query .= "     T1.SEQ, ";
            $query .= "     T1.CONTENTS, ";
            $query .= "     T1.SCORE, ";
            $query .= "     T1.REMARK, ";
            $query .= "     T1.CREDITS, ";
            $query .= "     T5.CREDITS AS CREDIT ";
            $query .= " FROM ";
            $query .= "     SCHREG_QUALIFIED_HOBBY_DAT T1 ";
            $query .= "     LEFT JOIN SUBCLASS_MST T2 ON T1.SUBCLASSCD = T2.SUBCLASSCD ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                  AND T1.CLASSCD         = T2.CLASSCD ";
                $query .= "                                  AND T1.SCHOOL_KIND     = T2.SCHOOL_KIND ";
                $query .= "                                  AND T1.CURRICULUM_CD   = T2.CURRICULUM_CD ";
            }
            $query .= "       LEFT JOIN SCHREG_REGD_DAT T4 ON T4.SCHREGNO = T1.SCHREGNO ";
            $query .= "                                   AND T4.YEAR     = T1.YEAR ";
            $query .= "                                   AND T4.SEMESTER = '".$model->exp_semester."' ";
            $query .= "       LEFT JOIN CREDIT_MST T5 ON T5.YEAR       = T1.YEAR ";
            $query .= "                              AND T5.COURSECD   = T4.COURSECD ";
            $query .= "                              AND T5.MAJORCD    = T4.MAJORCD ";
            $query .= "                              AND T5.GRADE      = T4.GRADE ";
            $query .= "                              AND T5.COURSECODE = T4.COURSECODE ";
            $query .= "                              AND T5.CLASSCD    = SUBSTR(T1.SUBCLASSCD,1,2) ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                          AND T5.SCHOOL_KIND     = T1.SCHOOL_KIND ";
                $query .= "                          AND T5.CURRICULUM_CD   = T1.CURRICULUM_CD ";
            }
            $query .= "                              AND T5.SUBCLASSCD = T1.SUBCLASSCD ";
            if (0 < $db->getOne(knjx_qualified_selectQuery::getSyscatColumns("QUALIFIED_RANK_DAT"))) {
                $query .= " LEFT JOIN ";
                $query .= "     QUALIFIED_RANK_DAT L3 ON L3.QUALIFIED_CD = T1.QUALIFIED_CD ";
                $query .= "                          AND L3.RANK = T1.RANK ";
            }
            $query .= " WHERE ";
            if ($model->send_prgid == 'KNJE020') {
                $query .= "     T1.YEAR IN (SELECT YEAR FROM T_YEAR) AND ";
            } else {
                $query .= "     T1.YEAR     = '{$model->exp_year}' AND ";
            }
            $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
            if (0 < $db->getOne(knjx_qualified_selectQuery::getSyscatColumns("QUALIFIED_RANK_DAT"))) {
                $query .= "    AND L3.NOT_PRINT IS NULL ";
            }
            $query .= " ORDER BY ";
            $query .= "     T1.REGDDATE, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.SEQ ";
        }

        return $query;
    }

    //NOT_PRINTがテーブルにあるかどうか取得
    function getSyscatColumns($tableName) {
        $query  = " SELECT ";
        $query .= "     COUNT(COLNAME) ";
        $query .= " FROM ";
        $query .= "     SYSCAT.COLUMNS ";
        $query .= " WHERE ";
        $query .= "     TABNAME = '".$tableName."' ";
        $query .= "     AND COLNAME = 'NOT_PRINT' ";

        return $query;
    }
}
?>
