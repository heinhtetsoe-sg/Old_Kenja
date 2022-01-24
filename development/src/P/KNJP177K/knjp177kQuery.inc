<?php

require_once('for_php7.php');

class knjp177kQuery extends Query {
    function selectQuery(&$model) {
        //変数
        $sdate =  CTRL_YEAR      ."-04-01";
        $edate = (CTRL_YEAR + 1) ."-03-31";
        $rankNameCd = "G218";
        if ("2016" > CTRL_YEAR) {
            $rankNameCd = "G213";
        } else if ("2016" == CTRL_YEAR && $model->search["GRADE"] > "01") {
            $rankNameCd = "G213";
        } else if ("2017" == CTRL_YEAR && $model->search["GRADE"] == "03") {
            $rankNameCd = "G213";
        }
        //SQL
        $query .= " WITH REGD_MAX_SEM AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     MAX(SEMESTER) AS SEMESTER, ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO ";
        $query .= " ), REGD_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     REGD_MAX_SEM T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR ";
        if (isset($model->search["HR_CLASS"]) && $model->search["HR_CLASS"] != "00-000") {
            $query .= "   AND T1.GRADE || '-' || T1.HR_CLASS = '".$model->search["HR_CLASS"]."' ";
        }else if ($model->search["GRADE"]) {
            $query .= "   AND T1.GRADE = '".$model->search["GRADE"]."' ";
        } else {
            $query .= "   AND T1.GRADE = '' ";
        }
        $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ), LAST_REGD_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     REGD_T T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->mstLastYear."' ";
        $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ), KNJP170SQL AS ( ";
        $query .= " SELECT ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.NAME_SHOW, ";
        $query .= "   H1.CURRICULUM_YEAR, ";
        $query .= "   T1.GRADE, ";
        $query .= "   T1.HR_CLASS, ";
        $query .= "   T1.HR_NAMEABBV, ";
        $query .= "   T1.ATTENDNO, ";
        $query .= "   T2.GUARD_NAME, ";
        $query .= "   T2.PREF, ";
        $query .= "   T2.PREFECTURESCD AS PREF_CD, ";
        $query .= "   T3.PREFECTURESCD, ";
        $query .= "   T3.PREF AS PREF2, ";
        $query .= "   T3.REDUCTION_SEQ_1, ";
        $query .= "   T3.REDUC_INCOME_1, ";
        $query .= "   N1.NAME1 AS REDUC_RANK_1, ";
        $query .= "   T3.REDUCTIONMONEY_1, ";
        $query .= "   T3.REDUC_DEC_FLG_1, ";
        $query .= "   T3.REDUCTION_SEQ_2, ";
        $query .= "   T3.REDUC_INCOME_2, ";
        $query .= "   N2.NAME1 AS REDUC_RANK_2, ";
        $query .= "   T3.REDUCTIONMONEY_2, ";
        $query .= "   T3.REDUC_DEC_FLG_2, ";
        $query .= "   T3.REDUC_RARE_CASE_CD, ";
        $query .= "   G211.NAMESPARE1 AS REDUC_RARE_SPARE, ";
        $query .= "   CASE WHEN T3.REDUC_REMARK IS NOT NULL ";
        $query .= "        THEN T3.REDUC_REMARK ";
        $query .= "        ELSE SUB_REMARK.REDUC_REMARK ";
        $query .= "   END AS REDUC_REMARK, ";
        $query .= "   T3.TOTAL_MONEY, ";
        $query .= "   MAX_GRANTCD1.GRANTCD AS GRANTCD1, ";
        $query .= "   GRA1.ABBV1 AS GRANT_NAME1, ";
        $query .= "   MAX_GRANTCD2.GRANTCD AS GRANTCD2, ";
        $query .= "   GRA2.ABBV1 AS GRANT_NAME2, ";
        $query .= "   MAX_GRANTCD3.GRANTCD AS GRANTCD3, ";
        $query .= "   GRA3.ABBV1 AS GRANT_NAME3, ";
        $query .= "   RCD.REDUC_INCOME_1 AS COPY_INCOME_1, ";
        $query .= "   RCD.REDUC_INCOME_2 AS COPY_INCOME_2, ";
        $query .= "   value(T6.COUNTTRANSFER,0) + value(G6.COUNTTRANSFER,0) + value(E6.COUNTTRANSFER,0) AS COUNTTRANSFER ";
        $query .= " FROM ";

        $query .= "   (SELECT ";
        $query .= "     ST2.YEAR, ";
        $query .= "     ST2.SEMESTER, ";
        $query .= "     ST1.SCHREGNO, ";
        $query .= "     ST1.NAME_SHOW, ";
        $query .= "     ST2.GRADE, ";
        $query .= "     ST2.HR_CLASS, ";
        $query .= "     ST2.ATTENDNO, ";
        $query .= "     ST3.HR_NAMEABBV ";
        $query .= "   FROM ";
        $query .= "     SCHREG_BASE_MST ST1, ";
        $query .= "     SCHREG_REGD_DAT ST2 LEFT OUTER JOIN SCHREG_REGD_HDAT ST3  ";
        $query .= "       ON ST2.YEAR = ST3.YEAR AND  ";
        $query .= "       ST2.SEMESTER = ST3.SEMESTER AND  ";
        $query .= "       ST2.GRADE = ST3.GRADE AND  ";
        $query .= "       ST2.HR_CLASS = ST3.HR_CLASS ";
        $query .= "   WHERE ";
        $query .= "     ST1.SCHREGNO = ST2.SCHREGNO ";
        $query .= "   ) T1 ";

        $query .= "       LEFT OUTER JOIN (SELECT DISTINCT ";
        $query .= "                             ST1.SCHREGNO, ";
        $query .= "                             ST1.GUARANTOR_NAME AS GUARD_NAME, ";
        $query .= "                             SUBSTR(ST2.CITYCD,1,2) AS PREFECTURESCD, ";
        $query .= "                             MEISYOU_GET(SUBSTR(ST2.CITYCD,1,2),'G202',1) AS PREF ";
        $query .= "                         FROM ";
        $query .= "                             GUARDIAN_DAT ST1 LEFT OUTER JOIN ZIPCD_MST ST2  ";
        $query .= "                             ON ST1.GUARANTOR_ZIPCD = ST2.NEW_ZIPCD ";
        $query .= "                         ) T2 ON T1.SCHREGNO = T2.SCHREGNO  ";

        $query .= "       LEFT OUTER JOIN (SELECT ";
        $query .= "                            ST1.YEAR, ";
        $query .= "                            ST1.SCHREGNO, ";
        $query .= "                            ST1.PREFECTURESCD, ";
        $query .= "                            MEISYOU_GET(ST1.PREFECTURESCD, 'G202', 1) AS PREF, ";
        $query .= "                            ST1.REDUCTION_SEQ_1, ";
        $query .= "                            ST1.REDUC_INCOME_1, ";
        $query .= "                            ST1.REDUC_RANK_1, ";
        $query .= "                            ST1.REDUCTIONMONEY_1, ";
        $query .= "                            ST1.REDUC_DEC_FLG_1, ";
        $query .= "                            ST1.REDUCTION_SEQ_2, ";
        $query .= "                            ST1.REDUC_INCOME_2, ";
        $query .= "                            ST1.REDUC_RANK_2, ";
        $query .= "                            ST1.REDUCTIONMONEY_2, ";
        $query .= "                            ST1.REDUC_DEC_FLG_2, ";
        $query .= "                            ST1.REDUC_RARE_CASE_CD, ";
        $query .= "                            ST1.REDUC_REMARK, ";
        $query .= "                            VALUE(ST1.REDUCTIONMONEY_1, 0) + VALUE(ST1.REDUCTIONMONEY_2, 0) AS TOTAL_MONEY ";
        $query .= "                        FROM ";
        $query .= "                            REDUCTION_DAT ST1 ";
        $query .= "                        ) T3 ON  T1.YEAR     = T3.YEAR ";
        $query .= "                             AND T1.SCHREGNO = T3.SCHREGNO ";
        $query .= "       LEFT JOIN REDUCTION_COUNTRY_DAT SUB_REMARK ON T1.YEAR = SUB_REMARK.YEAR ";
        $query .= "            AND T1.SCHREGNO = SUB_REMARK.SCHREGNO ";
        $query .= "       LEFT OUTER JOIN (SELECT MAX(GRANTCD) AS GRANTCD, ";
        $query .= "                            YEAR, ";
        $query .= "                            SCHREGNO  ";
        $query .= "                        FROM  ";
        $query .= "                            SCHREG_GRANT_DAT ST1  ";
        $query .= "                        WHERE  ";
        $query .= "                            ST1.GRANTCD = '01' ";
        $query .= "                        GROUP BY ";
        $query .= "                            YEAR, ";
        $query .= "                            SCHREGNO ";
        $query .= "                        ) MAX_GRANTCD1 ON  T1.YEAR = MAX_GRANTCD1.YEAR  ";
        $query .= "                             AND T1.SCHREGNO = MAX_GRANTCD1.SCHREGNO ";
        $query .= "       LEFT JOIN NAME_MST GRA1 ON GRA1.NAMECD1 = 'G212' ";
        $query .= "                    AND GRA1.NAMECD2 = MAX_GRANTCD1.GRANTCD ";
        $query .= "       LEFT OUTER JOIN (SELECT MAX(GRANTCD) AS GRANTCD, ";
        $query .= "                            YEAR, ";
        $query .= "                            SCHREGNO  ";
        $query .= "                        FROM  ";
        $query .= "                            SCHREG_GRANT_DAT ST1  ";
        $query .= "                        WHERE  ";
        $query .= "                            ST1.GRANTCD = '02' ";
        $query .= "                        GROUP BY ";
        $query .= "                            YEAR, ";
        $query .= "                            SCHREGNO ";
        $query .= "                        ) MAX_GRANTCD2 ON  T1.YEAR = MAX_GRANTCD2.YEAR  ";
        $query .= "                             AND T1.SCHREGNO = MAX_GRANTCD2.SCHREGNO ";
        $query .= "       LEFT JOIN NAME_MST GRA2 ON GRA2.NAMECD1 = 'G212' ";
        $query .= "                    AND GRA2.NAMECD2 = MAX_GRANTCD2.GRANTCD ";
        $query .= "       LEFT OUTER JOIN (SELECT MAX(GRANTCD) AS GRANTCD, ";
        $query .= "                            YEAR, ";
        $query .= "                            SCHREGNO  ";
        $query .= "                        FROM  ";
        $query .= "                            SCHREG_GRANT_DAT ST1  ";
        $query .= "                        WHERE  ";
        $query .= "                            ST1.GRANTCD = '03' ";
        $query .= "                        GROUP BY ";
        $query .= "                            YEAR, ";
        $query .= "                            SCHREGNO ";
        $query .= "                        ) MAX_GRANTCD3 ON  T1.YEAR = MAX_GRANTCD3.YEAR  ";
        $query .= "                             AND T1.SCHREGNO = MAX_GRANTCD3.SCHREGNO ";
        $query .= "       LEFT JOIN NAME_MST GRA3 ON GRA3.NAMECD1 = 'G212' ";
        $query .= "                    AND GRA3.NAMECD2 = MAX_GRANTCD3.GRANTCD ";
        $query .= "       LEFT OUTER JOIN (SELECT ";
        $query .= "                            COUNT(*) AS COUNTTRANSFER, ";
        $query .= "                            SCHREGNO ";
        $query .= "                        FROM ";
        $query .= "                            SCHREG_TRANSFER_DAT ";
        $query .= "                        WHERE ";
        $query .= "                            TRANSFERCD IN ('1','2') ";
        $query .= "                            AND ((TRANSFER_SDATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}')) ";
        $query .= "                             OR  (TRANSFER_EDATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}'))) ";
        $query .= "                        GROUP BY ";
        $query .= "                            SCHREGNO ";
        $query .= "                       ) T6 ON T1.SCHREGNO = T6.SCHREGNO ";
        $query .= "       LEFT OUTER JOIN (SELECT ";
        $query .= "                            COUNT(*) AS COUNTTRANSFER, ";
        $query .= "                            SCHREGNO ";
        $query .= "                        FROM ";
        $query .= "                            SCHREG_BASE_MST ";
        $query .= "                        WHERE ";
        $query .= "                            GRD_DIV > '1' ";
        $query .= "                            AND GRD_DATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}') ";
        $query .= "                        GROUP BY ";
        $query .= "                            SCHREGNO ";
        $query .= "                       ) G6 ON T1.SCHREGNO = G6.SCHREGNO ";
        $query .= "       LEFT OUTER JOIN (SELECT ";
        $query .= "                            COUNT(*) AS COUNTTRANSFER, ";
        $query .= "                            SCHREGNO ";
        $query .= "                        FROM ";
        $query .= "                            SCHREG_BASE_MST ";
        $query .= "                        WHERE ";
        $query .= "                            ENT_DIV > '3' ";
        $query .= "                            AND ENT_DATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}') ";
        $query .= "                        GROUP BY ";
        $query .= "                            SCHREGNO ";
        $query .= "                       ) E6 ON T1.SCHREGNO = E6.SCHREGNO ";
        $query .= "       LEFT JOIN SCHREG_REGD_GDAT G1 ON G1.YEAR = '".CTRL_YEAR."' ";
        $query .= "            AND G1.GRADE = T1.GRADE ";
        $query .= "       LEFT JOIN SCHREG_ENT_GRD_HIST_DAT H1 ON H1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                    AND H1.SCHOOL_KIND = G1.SCHOOL_KIND ";
        $query .= "       LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = '{$rankNameCd}' ";
        $query .= "                    AND N1.NAMECD2 = T3.REDUC_RANK_1 ";
        $query .= "       LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = '{$rankNameCd}' ";
        $query .= "                    AND N2.NAMECD2 = T3.REDUC_RANK_2 ";
        $query .= "       LEFT JOIN REDUCTION_COUNTRY_DAT RCD ON RCD.YEAR = T1.YEAR ";
        $query .= "                    AND RCD.SCHREGNO = T1.SCHREGNO ";
        $query .= "       LEFT JOIN NAME_MST G211 ON N1.NAMECD1 = 'G211' ";
        $query .= "                    AND N1.NAMECD2 = T3.REDUC_RARE_CASE_CD ";

        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "   T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        if (isset($model->search["PREF"]) && $model->search["RAD_PREF"] == 1 && $model->search["PREF"] != "00") {
            $query .= "   T2.PREFECTURESCD = '".$model->search["PREF"]."' AND ";
        }else if (isset($model->search["PREF"]) && $model->search["RAD_PREF"] == 2 && $model->search["PREF"] != "00") {
            $query .= "   T3.PREFECTURESCD = '".$model->search["PREF"]."' AND ";
        }
        if (isset($model->search["HR_CLASS"]) && $model->search["HR_CLASS"] != "00-000") {
            $query .= "   T1.GRADE || '-' || T1.HR_CLASS = '".$model->search["HR_CLASS"]."' ";
        }else if ($model->search["GRADE"]) {
            $query .= "   T1.GRADE = '".$model->search["GRADE"]."' ";
        } else {
            $query .= "   T1.GRADE = '' ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.NAME_SHOW, ";
        $query .= "   T1.GRADE, ";
        $query .= "   T1.HR_CLASS, ";
        $query .= "   T1.HR_NAMEABBV, ";
        $query .= "   T1.ATTENDNO, ";
        $query .= "   T2.GUARD_NAME, ";
        $query .= "   T2.PREF, ";
        $query .= "   T2.PREFECTURESCD AS PREF_CD, ";
        $query .= "   L4.PASSNO, ";
        $query .= "   REDUC_C.OFFSET_FLG, ";
        $query .= "   RARE.NAMECD2 || ':' || RARE.NAMESPARE1 AS REDUC_RARE_CASE_CD_1, ";
        $query .= "   RARE.NAMESPARE1 AS TEXT_DISP1, ";
        $query .= "   REDUC_C.REDUCTION_SEQ_176_1, ";
        $query .= "   REDUC_C.REDUC_INCOME_1, ";
        $query .= "   CASE WHEN RARE.NAMESPARE1 like '1:%' ";
        $query .= "        THEN REDUC_C.REDUCTIONMONEY_1 ";
        $query .= "        ELSE L1.REDUCTIONMONEY1 ";
        $query .= "   END AS BASE_MONEY_1, ";
        $query .= "   REDUC_C.REDUCTIONMONEY_1, ";
        $query .= "   REDUC_C.REDUC_DEC_FLG_1, ";
        $query .= "   CASE WHEN RARE.NAMESPARE1 like '1:%' ";
        $query .= "        THEN REDUC_C.REDUCTION_ADD_MONEY_1 ";
        $query .= "        ELSE L1.REDUCTION_ADD_MONEY1 ";
        $query .= "   END AS REDUCTION_ADD_MONEY_1, ";
        $query .= "   REDUC_C.REDUC_ADD_FLG_1, ";
        $query .= "   RARE2.NAMECD2 || ':' || RARE2.NAMESPARE1 AS REDUC_RARE_CASE_CD_2, ";
        $query .= "   RARE2.NAMESPARE1 AS TEXT_DISP2, ";
        $query .= "   REDUC_C.REDUCTION_SEQ_176_2, ";
        $query .= "   REDUC_C.REDUC_INCOME_2, ";
        $query .= "   CASE WHEN RARE2.NAMESPARE1 like '1:%' ";
        $query .= "        THEN REDUC_C.REDUCTIONMONEY_2 ";
        $query .= "        ELSE L2.REDUCTIONMONEY1 ";
        $query .= "   END AS BASE_MONEY_2, ";
        $query .= "   REDUC_C.REDUCTIONMONEY_2, ";
        $query .= "   REDUC_C.REDUC_DEC_FLG_2, ";
        $query .= "   CASE WHEN RARE2.NAMESPARE1 like '1:%' ";
        $query .= "        THEN REDUC_C.REDUCTION_ADD_MONEY_2 ";
        $query .= "        ELSE L2.REDUCTION_ADD_MONEY1 ";
        $query .= "   END AS REDUCTION_ADD_MONEY_2, ";
        $query .= "   REDUC_C.REDUC_ADD_FLG_2, ";
        $query .= "   CASE WHEN REDUC_C.REDUC_REMARK IS NOT NULL ";
        $query .= "        THEN REDUC_C.REDUC_REMARK ";
        $query .= "        ELSE SUB_REMARK.REDUC_REMARK ";
        $query .= "   END AS REDUC_REMARK, ";
        $query .= "   REDUC_C.TOTAL_MONEY, ";
        $query .= "   MAX_GRANTCD1.GRANTCD AS GRANTCD1, ";
        $query .= "   GRA1.ABBV1 AS GRANT_NAME1, ";
        $query .= "   MAX_GRANTCD2.GRANTCD AS GRANTCD2, ";
        $query .= "   GRA2.ABBV1 AS GRANT_NAME2, ";
        $query .= "   MAX_GRANTCD3.GRANTCD AS GRANTCD3, ";
        $query .= "   GRA3.ABBV1 AS GRANT_NAME3, ";
        $query .= "   value(T6.COUNTTRANSFER,0) + value(G6.COUNTTRANSFER,0) + value(E6.COUNTTRANSFER,0) AS COUNTTRANSFER, ";
        $query .= "   KNJP170SQL.PREF2, ";
        $query .= "   KNJP170SQL.REDUC_RARE_CASE_CD, ";
        $query .= "   KNJP170SQL.REDUC_RARE_SPARE, ";
        $query .= "   KNJP170SQL.REDUC_RANK_1, ";
        $query .= "   KNJP170SQL.REDUC_RANK_2, ";
        $query .= "   KNJP170SQL.REDUCTIONMONEY_1 AS REDUCTIONMONEY_1_170, ";
        $query .= "   KNJP170SQL.REDUCTIONMONEY_2 AS REDUCTIONMONEY_2_170, ";
        $query .= "   KNJP170SQL.REDUC_DEC_FLG_1 AS REDUC_DEC_FLG_1_170, ";
        $query .= "   KNJP170SQL.REDUC_DEC_FLG_2 AS REDUC_DEC_FLG_2_170, ";
        $query .= "   KNJP170SQL.TOTAL_MONEY AS TOTAL_MONEY_170, ";
        $query .= "   L4.BEGIN_YEARMONTH, ";
        $query .= "   L4.REMAIN_SUP_LIMIT_MONTH, ";
        $query .= "   L3.ZENKI_CNT, ";
        $query .= "   L3.KOUKI_CNT, ";
        $query .= "   L3.ZENKI_ADD_CNT, ";
        $query .= "   L3.KOUKI_ADD_CNT ";
        $query .= " FROM ";

        $query .= "   (SELECT ";
        $query .= "     ST2.YEAR, ";
        $query .= "     ST2.SEMESTER, ";
        $query .= "     ST1.SCHREGNO, ";
        $query .= "     ST1.NAME_SHOW, ";
        $query .= "     ST2.GRADE, ";
        $query .= "     LREG.GRADE AS LAST_GRADE, ";
        $query .= "     ST2.HR_CLASS, ";
        $query .= "     ST2.ATTENDNO, ";
        $query .= "     ST3.HR_NAMEABBV ";
        $query .= "   FROM ";
        $query .= "     SCHREG_BASE_MST ST1, ";
        $query .= "     REGD_T ST2 ";
        $query .= "     LEFT OUTER JOIN SCHREG_REGD_HDAT ST3  ";
        $query .= "       ON ST2.YEAR = ST3.YEAR AND  ";
        $query .= "       ST2.SEMESTER = ST3.SEMESTER AND  ";
        $query .= "       ST2.GRADE = ST3.GRADE AND  ";
        $query .= "       ST2.HR_CLASS = ST3.HR_CLASS ";
        $query .= "     LEFT JOIN LAST_REGD_T LREG  ";
        $query .= "       ON ST2.SCHREGNO = LREG.SCHREGNO ";
        $query .= "   WHERE ";
        $query .= "     ST1.SCHREGNO = ST2.SCHREGNO ";
        $query .= "   ) T1 ";

        $query .= "       LEFT OUTER JOIN (SELECT DISTINCT ";
        $query .= "                             ST1.SCHREGNO, ";
        $query .= "                             ST1.GUARANTOR_NAME AS GUARD_NAME, ";
        $query .= "                             SUBSTR(ST2.CITYCD,1,2) AS PREFECTURESCD, ";
        $query .= "                             MEISYOU_GET(SUBSTR(ST2.CITYCD,1,2),'G202',1) AS PREF ";
        $query .= "                         FROM ";
        $query .= "                             GUARDIAN_DAT ST1 LEFT OUTER JOIN ZIPCD_MST ST2  ";
        $query .= "                             ON ST1.GUARANTOR_ZIPCD = ST2.NEW_ZIPCD ";
        $query .= "                         ) T2 ON T1.SCHREGNO = T2.SCHREGNO  ";

        $query .= "       LEFT OUTER JOIN (SELECT ";
        $query .= "                            ST1.YEAR, ";
        $query .= "                            ST1.SCHREGNO, ";
        $query .= "                            ST1.OFFSET_FLG, ";
        $query .= "                            ST1.REDUC_RARE_CASE_CD_1, ";
        $query .= "                            ST1.REDUCTION_SEQ_1 AS REDUCTION_SEQ_176_1, ";
        $query .= "                            ST1.REDUC_INCOME_1, ";
        $query .= "                            ST1.REDUCTIONMONEY_1, ";
        $query .= "                            ST1.REDUC_DEC_FLG_1, ";
        $query .= "                            ST1.REDUCTION_ADD_MONEY_1, ";
        $query .= "                            ST1.REDUC_ADD_FLG_1, ";
        $query .= "                            ST1.REDUC_RARE_CASE_CD_2, ";
        $query .= "                            ST1.REDUCTION_SEQ_2 AS REDUCTION_SEQ_176_2, ";
        $query .= "                            ST1.REDUC_INCOME_2, ";
        $query .= "                            ST1.REDUCTIONMONEY_2, ";
        $query .= "                            ST1.REDUC_DEC_FLG_2, ";
        $query .= "                            ST1.REDUCTION_ADD_MONEY_2, ";
        $query .= "                            ST1.REDUC_ADD_FLG_2, ";
        $query .= "                            ST1.REDUC_REMARK, ";
        $query .= "                            VALUE(ST1.REDUCTIONMONEY_1, 0) + VALUE(ST1.REDUCTIONMONEY_2, 0) AS TOTAL_MONEY ";
        $query .= "                        FROM ";
        $query .= "                            REDUCTION_COUNTRY_DAT ST1 ";
        $query .= "                        ) REDUC_C ON  T1.YEAR     = REDUC_C.YEAR ";
        $query .= "                             AND T1.SCHREGNO = REDUC_C.SCHREGNO ";
        $query .= "       LEFT JOIN NAME_MST RARE ON RARE.NAMECD1 = 'G216' ";
        $query .= "            AND RARE.NAMECD2 = REDUC_C.REDUC_RARE_CASE_CD_1 ";
        $query .= "       LEFT JOIN NAME_MST RARE2 ON RARE2.NAMECD1 = 'G216' ";
        $query .= "            AND RARE2.NAMECD2 = REDUC_C.REDUC_RARE_CASE_CD_2 ";
        $query .= "       LEFT JOIN REDUCTION_DAT SUB_REMARK ON T1.YEAR = SUB_REMARK.YEAR ";
        $query .= "            AND T1.SCHREGNO = SUB_REMARK.SCHREGNO ";
        $query .= "       LEFT OUTER JOIN (SELECT MAX(GRANTCD) AS GRANTCD, ";
        $query .= "                            YEAR, ";
        $query .= "                            SCHREGNO  ";
        $query .= "                        FROM  ";
        $query .= "                            SCHREG_GRANT_DAT ST1  ";
        $query .= "                        WHERE  ";
        $query .= "                            ST1.GRANTCD = '01' ";
        $query .= "                        GROUP BY ";
        $query .= "                            YEAR, ";
        $query .= "                            SCHREGNO ";
        $query .= "                        ) MAX_GRANTCD1 ON  T1.YEAR = MAX_GRANTCD1.YEAR  ";
        $query .= "                             AND T1.SCHREGNO = MAX_GRANTCD1.SCHREGNO ";
        $query .= "       LEFT JOIN NAME_MST GRA1 ON GRA1.NAMECD1 = 'G212' ";
        $query .= "                    AND GRA1.NAMECD2 = MAX_GRANTCD1.GRANTCD ";
        $query .= "       LEFT OUTER JOIN (SELECT MAX(GRANTCD) AS GRANTCD, ";
        $query .= "                            YEAR, ";
        $query .= "                            SCHREGNO  ";
        $query .= "                        FROM  ";
        $query .= "                            SCHREG_GRANT_DAT ST1  ";
        $query .= "                        WHERE  ";
        $query .= "                            ST1.GRANTCD = '02' ";
        $query .= "                        GROUP BY ";
        $query .= "                            YEAR, ";
        $query .= "                            SCHREGNO ";
        $query .= "                        ) MAX_GRANTCD2 ON  T1.YEAR = MAX_GRANTCD2.YEAR  ";
        $query .= "                             AND T1.SCHREGNO = MAX_GRANTCD2.SCHREGNO ";
        $query .= "       LEFT JOIN NAME_MST GRA2 ON GRA2.NAMECD1 = 'G212' ";
        $query .= "                    AND GRA2.NAMECD2 = MAX_GRANTCD2.GRANTCD ";
        $query .= "       LEFT OUTER JOIN (SELECT MAX(GRANTCD) AS GRANTCD, ";
        $query .= "                            YEAR, ";
        $query .= "                            SCHREGNO  ";
        $query .= "                        FROM  ";
        $query .= "                            SCHREG_GRANT_DAT ST1  ";
        $query .= "                        WHERE  ";
        $query .= "                            ST1.GRANTCD = '03' ";
        $query .= "                        GROUP BY ";
        $query .= "                            YEAR, ";
        $query .= "                            SCHREGNO ";
        $query .= "                        ) MAX_GRANTCD3 ON  T1.YEAR = MAX_GRANTCD3.YEAR  ";
        $query .= "                             AND T1.SCHREGNO = MAX_GRANTCD3.SCHREGNO ";
        $query .= "       LEFT JOIN NAME_MST GRA3 ON GRA3.NAMECD1 = 'G212' ";
        $query .= "                    AND GRA3.NAMECD2 = MAX_GRANTCD3.GRANTCD ";
        $query .= "       LEFT OUTER JOIN (SELECT ";
        $query .= "                            COUNT(*) AS COUNTTRANSFER, ";
        $query .= "                            SCHREGNO ";
        $query .= "                        FROM ";
        $query .= "                            SCHREG_TRANSFER_DAT ";
        $query .= "                        WHERE ";
        $query .= "                            TRANSFERCD IN ('1','2') ";
        $query .= "                            AND ((TRANSFER_SDATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}')) ";
        $query .= "                             OR  (TRANSFER_EDATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}'))) ";
        $query .= "                        GROUP BY ";
        $query .= "                            SCHREGNO ";
        $query .= "                       ) T6 ON T1.SCHREGNO = T6.SCHREGNO ";
        $query .= "       LEFT OUTER JOIN (SELECT ";
        $query .= "                            COUNT(*) AS COUNTTRANSFER, ";
        $query .= "                            SCHREGNO ";
        $query .= "                        FROM ";
        $query .= "                            SCHREG_BASE_MST ";
        $query .= "                        WHERE ";
        $query .= "                            GRD_DIV > '1' ";
        $query .= "                            AND GRD_DATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}') ";
        $query .= "                        GROUP BY ";
        $query .= "                            SCHREGNO ";
        $query .= "                       ) G6 ON T1.SCHREGNO = G6.SCHREGNO ";
        $query .= "       LEFT OUTER JOIN (SELECT ";
        $query .= "                            COUNT(*) AS COUNTTRANSFER, ";
        $query .= "                            SCHREGNO ";
        $query .= "                        FROM ";
        $query .= "                            SCHREG_BASE_MST ";
        $query .= "                        WHERE ";
        $query .= "                            ENT_DIV > '3' ";
        $query .= "                            AND ENT_DATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}') ";
        $query .= "                        GROUP BY ";
        $query .= "                            SCHREGNO ";
        $query .= "                       ) E6 ON T1.SCHREGNO = E6.SCHREGNO ";
        $query .= "       LEFT JOIN REDUCTION_COUNTRY_MST L1 ON L1.YEAR = '".CTRL_YEAR."' ";
        $query .= "            AND T1.LAST_GRADE = L1.GRADE ";
        $query .= "            AND REDUC_C.REDUC_INCOME_1 BETWEEN L1.INCOME_LOW1 AND L1.INCOME_HIGH1 ";
        $query .= "       LEFT JOIN REDUCTION_COUNTRY_MST L2 ON L2.YEAR = '".CTRL_YEAR."' ";
        $query .= "            AND T1.GRADE = L2.GRADE ";
        $query .= "            AND REDUC_C.REDUC_INCOME_2 BETWEEN L2.INCOME_LOW2 AND L2.INCOME_HIGH2 ";
        $query .= "       LEFT JOIN (SELECT ";
        $query .= "                    SCHREGNO, ";
        $query .= "                    SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH < '".CTRL_YEAR."07' AND PLAN_MONEY IS NOT NULL ";
        $query .= "                             THEN 1 ";
        $query .= "                             ELSE 0 ";
        $query .= "                        END ";
        $query .= "                    ) AS ZENKI_CNT, ";
        $query .= "                    SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH >= '".CTRL_YEAR."07' AND PLAN_MONEY IS NOT NULL ";
        $query .= "                             THEN 1 ";
        $query .= "                             ELSE 0 ";
        $query .= "                        END ";
        $query .= "                    ) AS KOUKI_CNT, ";
        $query .= "                    SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH < '".CTRL_YEAR."07' AND ADD_PLAN_MONEY IS NOT NULL ";
        $query .= "                             THEN 1 ";
        $query .= "                             ELSE 0 ";
        $query .= "                        END ";
        $query .= "                    ) AS ZENKI_ADD_CNT, ";
        $query .= "                    SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH >= '".CTRL_YEAR."07' AND ADD_PLAN_MONEY IS NOT NULL ";
        $query .= "                             THEN 1 ";
        $query .= "                             ELSE 0 ";
        $query .= "                        END ";
        $query .= "                    ) AS KOUKI_ADD_CNT ";
        $query .= "                  FROM ";
        $query .= "                      REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= "                  WHERE ";
        $query .= "                      YEAR = '".CTRL_YEAR."' ";
        $query .= "                  GROUP BY ";
        $query .= "                      SCHREGNO ";
        $query .= "                 ) L3 ON T1.SCHREGNO = L3.SCHREGNO ";
        $query .= "       LEFT JOIN REDUCTION_AUTHORIZE_DAT L4 ON T1.SCHREGNO = L4.SCHREGNO ";
        $query .= "            AND L4.DATA_DIV = '1' ";
        $query .= "            AND L4.DATA_DIV_SUB = '1' ";

        if (isset($model->search["PREF"]) && $model->search["PREF"] != "00") {
            $query .= "       INNER JOIN KNJP170SQL ON T1.SCHREGNO = KNJP170SQL.SCHREGNO ";
        } else {
            $query .= "       LEFT JOIN KNJP170SQL ON T1.SCHREGNO = KNJP170SQL.SCHREGNO ";
        }

        $query .= " ORDER BY ";
        $query .= "   GRADE, ";
        $query .= "   HR_CLASS, ";
        $query .= "   ATTENDNO ";

        return $query;
    }

    //年学年
    function selectQueryGrade(&$model) {
        $query = "SELECT ";
        $query .= "  T1.GRADE, ";
        $query .= "  T1.HR_CLASS, ";
        $query .= "  T1.HR_NAME, ";
        $query .= "  T1.HR_NAMEABBV, ";
        $query .= "  T2.STAFFCD, ";
        $query .= "  T2.STAFFNAME_SHOW ";
        $query .= "FROM ";
        $query .= "  SCHREG_REGD_HDAT T1,";
        $query .= "  STAFF_MST T2 ";
        $query .= "WHERE ";
        if ($model->usr_auth == DEF_UPDATE_RESTRICT) {
            $query .= " T1.TR_CD1 = '". STAFFCD ."' AND";
        }
        $query .= "  T1.TR_CD1 = T2.STAFFCD AND ";
        $query .= "  T1.YEAR = '" .CTRL_YEAR ."' AND ";
        $query .= "  T1.SEMESTER = '" .CTRL_SEMESTER ."' ";
        $query .= " ORDER BY ";
        $query .= "  T1.GRADE,T1.HR_CLASS ";

        return $query;
    }

    //都道府県
    function selectQueryPref(&$model) {
        $query = " SELECT ";
        $query .= "   NAMECD2, ";
        $query .= "   NAME1, ";
        $query .= "   PREF_GRADE.USE_RANK, ";
        $query .= "   PREF_GRADE.ZENKI_KAISI_YEAR, ";
        $query .= "   PREF_GRADE.KOUKI_KAISI_YEAR ";
        $query .= " FROM ";
        $query .= "   NAME_MST T1 ";
        $query .= "   LEFT JOIN REDUCTION_PREF_GRADE_MST PREF_GRADE ON PREF_GRADE.YEAR = '".CTRL_YEAR."' ";
        $query .= "        AND T1.NAMECD2 = PREF_GRADE.PREFECTURESCD ";
        $query .= "        AND PREF_GRADE.GRADE = '{$model->search["GRADE"]}' ";
        $query .= " WHERE ";
        $query .= "   NAMECD1 = 'G202' ";
        $query .= " ORDER BY ";
        $query .= "   NAMECD2 ";
        return $query;
    }

    //軽減額取得
    function selectQueryReductionMonty(&$model) {
        $setSoeji = "1";
        if ($model->income["OBJ_NAME"] == 'REDUC_INCOME_1[]') {
            $setSoeji = "1";
        } else {
            $setSoeji = "2";
        }

        $query  = " SELECT ";
        $query .= "     T1.REDUCTIONMONEY{$setSoeji} AS REDUCTIONMONEY ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_MST T1, ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "         ST1.GRADE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ST1 ";
        $query .= "     WHERE ";
        $query .= "         ST1.YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "         ST1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "         ST1.SCHREGNO = '".$model->income["SCHREGNO"]."' ";
        $query .= "     ) T2 ";
        $query .= " WHERE ";
        $query .= "     T1.GRADE = T2.GRADE ";
        $query .= "     AND T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND ".$model->income["REDUC_INCOME_{$setSoeji}"]." BETWEEN T1.INCOME_LOW{$setSoeji} AND T1.INCOME_HIGH{$setSoeji} ";

        $query .= " ORDER BY ";
        $query .= "     REDUCTIONMONEY, ";
        $query .= "     REDUCTION_SEQ ";
        $query .= " FETCH FIRST 1 ROWS ONLY ";

        return $query;
    }

    //軽減額取得
    function selectQueryReductionAddMonty(&$model) {
        $setSoeji = "1";
        if ($model->income["OBJ_NAME"] == 'REDUC_INCOME_1[]') {
            $setSoeji = "1";
        } else {
            $setSoeji = "2";
        }

        $query  = " SELECT ";
        $query .= "     T1.REDUCTION_SEQ, ";
        $query .= "     T1.REDUCTION_ADD_MONEY{$setSoeji} AS REDUCTION_ADD_MONEY ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_MST T1, ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "         ST1.GRADE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ST1 ";
        $query .= "     WHERE ";
        $query .= "         ST1.YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "         ST1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "         ST1.SCHREGNO = '".$model->income["SCHREGNO"]."' ";
        $query .= "     ) T2 ";
        $query .= " WHERE ";
        $query .= "     T1.GRADE = T2.GRADE ";
        $query .= "     AND T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND ".$model->income["REDUC_INCOME_{$setSoeji}"]." BETWEEN T1.INCOME_LOW{$setSoeji} AND T1.INCOME_HIGH{$setSoeji} ";

        $query .= " ORDER BY ";
        $query .= "     REDUCTION_ADD_MONEY, ";
        $query .= "     REDUCTION_SEQ ";
        $query .= " FETCH FIRST 1 ROWS ONLY ";

        return $query;
    }

    //都道府県コード
    function selectQueryReductionPref(&$model) {
        $query = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1 = 'G202' AND ";
        if (is_numeric($model->income["PREFECTURESCD"])) {     //軽減対象都道府県設定値
            $query .= "     T1.NAMECD2 = '".$model->income["PREFECTURESCD"]."' ";
        } else {                                                //保護者都道府県
            $query .= "     T1.NAMECD2 = (SELECT ";
            $query .= "                       SUBSTR(MIN(ST1.CITYCD),1,2) ";
            $query .= "                   FROM ";
            $query .= "                       ZIPCD_MST ST1, ";
            $query .= "                       GUARDIAN_DAT ST2 ";
            $query .= "                   WHERE ";
            $query .= "                       ST1.NEW_ZIPCD = ST2.GUARANTOR_ZIPCD AND ";
            $query .= "                       ST2.SCHREGNO = '".$model->income["SCHREGNO"]."' ";
            $query .= "                   ) ";
        }
        return $query;
    }

    function getPlanCnt($schregno) {
        $query  = " SELECT ";
        $query .= "   COUNT(*) AS ALLCNT, ";
        $query .= "   SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH < '".CTRL_YEAR."07' AND PLAN_MONEY IS NOT NULL ";
        $query .= "            THEN 1 ";
        $query .= "            ELSE 0 ";
        $query .= "       END ";
        $query .= "   ) AS ZENKI_CNT, ";
        $query .= "   SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH >= '".CTRL_YEAR."07' AND PLAN_MONEY IS NOT NULL ";
        $query .= "            THEN 1 ";
        $query .= "            ELSE 0 ";
        $query .= "       END ";
        $query .= "   ) AS KOUKI_CNT, ";
        $query .= "   SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH < '".CTRL_YEAR."07' AND ADD_PLAN_MONEY IS NOT NULL ";
        $query .= "            THEN 1 ";
        $query .= "            ELSE 0 ";
        $query .= "       END ";
        $query .= "   ) AS ZENKI_ADD_CNT, ";
        $query .= "   SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH >= '".CTRL_YEAR."07' AND ADD_PLAN_MONEY IS NOT NULL ";
        $query .= "            THEN 1 ";
        $query .= "            ELSE 0 ";
        $query .= "       END ";
        $query .= "   ) AS KOUKI_ADD_CNT ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO = '".$schregno."' ";

        return $query;
    }

    function getReducMoney($schregno) {
        $query  = " SELECT ";
        $query .= "   SCHREGNO, ";
        $query .= "   REDUCTIONMONEY_1, ";
        $query .= "   REDUC_DEC_FLG_1, ";
        $query .= "   REDUCTIONMONEY_2, ";
        $query .= "   REDUC_DEC_FLG_2, ";
        $query .= "   REDUCTION_ADD_MONEY_1, ";
        $query .= "   REDUC_ADD_FLG_1, ";
        $query .= "   REDUCTION_ADD_MONEY_2, ";
        $query .= "   REDUC_ADD_FLG_2 ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //移動情報取得
    function selectQueryTransfer(&$model) {
        //変数
        $sdate =  CTRL_YEAR      ."-04-01";
        $edate = (CTRL_YEAR + 1) ."-03-31";
        //SQL
        $query = "
            WITH TRANS_T AS (
            SELECT
                T1.TRANSFERCD,T1.TRANSFER_SDATE,T1.TRANSFER_EDATE,T2.NAME1
            FROM
                SCHREG_TRANSFER_DAT T1 LEFT OUTER JOIN NAME_MST T2
                                    ON T1.TRANSFERCD = T2.NAMECD2
                                    AND T2.NAMECD1 =  'A004'
            WHERE
                SCHREGNO = '".$model->grantTransfer["SCHREGNO"]."'
                AND T1.TRANSFERCD IN ('1','2')
                AND ((T1.TRANSFER_SDATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}'))
                 OR  (T1.TRANSFER_EDATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}')))
            ), GRD_T AS (
            SELECT
                T1.GRD_DIV AS TRANSFERCD,T1.GRD_DATE AS TRANSFER_SDATE,CAST(NULL AS DATE) AS TRANSFER_EDATE,T2.NAME1
            FROM
                SCHREG_BASE_MST T1 LEFT OUTER JOIN NAME_MST T2
                                    ON T1.GRD_DIV = T2.NAMECD2
                                    AND T2.NAMECD1 =  'A003'
            WHERE
                SCHREGNO = '".$model->grantTransfer["SCHREGNO"]."'
                AND T1.GRD_DIV > '1'
                AND T1.GRD_DATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}')
            ), ENT_T AS (
            SELECT
                T1.ENT_DIV AS TRANSFERCD,T1.ENT_DATE AS TRANSFER_SDATE,CAST(NULL AS DATE) AS TRANSFER_EDATE,T2.NAME1
            FROM
                SCHREG_BASE_MST T1 LEFT OUTER JOIN NAME_MST T2
                                    ON T1.ENT_DIV = T2.NAMECD2
                                    AND T2.NAMECD1 =  'A002'
            WHERE
                SCHREGNO = '".$model->grantTransfer["SCHREGNO"]."'
                AND T1.ENT_DIV > '3'
                AND T1.ENT_DATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}')
            )
            SELECT
                *
            FROM
                ENT_T
            UNION ALL
            SELECT
                *
            FROM
                TRANS_T
            UNION ALL
            SELECT
                *
            FROM
                GRD_T
        ";
        return $query;
    }

    //奨学金情報取得
    function selectQueryGrant(&$model) {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRANTSDATE, ";
        $query .= "     T1.GRANTEDATE, ";
        $query .= "     T1.GRANT_MONEY, ";
        $query .= "     T1.GRANTCD, ";
        $query .= "     T2.NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_GRANT_DAT T1 ";
        $query .= " LEFT JOIN NAME_MST T2 ON  T1.GRANTCD = T2.NAMECD2 ";
        $query .= "                       AND T2.NAMECD1 =  'G212' ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->grantTransfer["SCHREGNO"]}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRANTCD ";
        return $query;
    }

    //軽減特殊コード取得
    function selectQuerySpecailCode() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS LABEL, ";
        $query .= "     NAMECD2 || ':' || NAMESPARE1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'G216' AND ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getSpecailCode($reduc_rare_case_cd) {
        $query  = " SELECT ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'G216' AND ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     NAMECD2 = '{$reduc_rare_case_cd}' ";

        return $query;
    }
}
?>