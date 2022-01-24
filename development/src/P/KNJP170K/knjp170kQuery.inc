<?php

require_once('for_php7.php');

class knjp170kQuery extends Query {
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
        $query  = " SELECT ";
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
        $query .= "   T3.INCOME_SIBLINGS1, ";
        $query .= "   PREF_GRADE.PREFECTURESCD AS PREF_GRADE, ";
        $query .= "   N1.NAME1 AS REDUC_RANK_1, ";
        $query .= "   T3.REDUCTIONMONEY_1, ";
        $query .= "   T3.REDUC_DEC_FLG_1, ";
        $query .= "   T3.REDUCTION_SEQ_2, ";
        $query .= "   T3.REDUC_INCOME_2, ";
        $query .= "   T3.INCOME_SIBLINGS2, ";
        $query .= "   N2.NAME1 AS REDUC_RANK_2, ";
        $query .= "   T3.REDUCTIONMONEY_2, ";
        $query .= "   T3.REDUC_DEC_FLG_2, ";
        $query .= "   CASE WHEN MAX_GRANTCD1.SCHREGNO IS NOT NULL OR MAX_GRANTCD2.SCHREGNO IS NOT NULL OR MAX_GRANTCD3.SCHREGNO IS NOT NULL ";
        $query .= "        THEN 'T' ";
        $query .= "        ELSE T3.REDUC_RARE_CASE_CD ";
        $query .= "   END AS REDUC_RARE_CASE_CD, ";
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
        $query .= "                            ST1.INCOME_SIBLINGS1, ";
        $query .= "                            ST1.REDUC_RANK_1, ";
        $query .= "                            ST1.REDUCTIONMONEY_1, ";
        $query .= "                            ST1.REDUC_DEC_FLG_1, ";
        $query .= "                            ST1.REDUCTION_SEQ_2, ";
        $query .= "                            ST1.REDUC_INCOME_2, ";
        $query .= "                            ST1.INCOME_SIBLINGS2, ";
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

        $query .= "       LEFT OUTER JOIN (SELECT ";
        $query .= "                          PREF_GRADE.* ";
        $query .= "                        FROM ";
        $query .= "                          REDUCTION_PREF_GRADE_MST PREF_GRADE ";
        $query .= "                          INNER JOIN NAME_MST N1 ON N1.NAMECD1 = 'G202' ";
        $query .= "                                AND N1.NAMECD2 = PREF_GRADE.PREFECTURESCD ";
        $query .= "                        WHERE ";
        $query .= "                          PREF_GRADE.YEAR = '".CTRL_YEAR."' ";
        $query .= "                          AND PREF_GRADE.GRADE = '{$model->search["GRADE"]}' ";
        $query .= "                         ) PREF_GRADE ON T1.GRADE = PREF_GRADE.GRADE AND T3.PREFECTURESCD = PREF_GRADE.PREFECTURESCD  ";

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
        $query .= " ORDER BY ";
        $query .= "   GRADE, ";
        $query .= "   HR_CLASS, ";
        $query .= "   ATTENDNO ";

        return $query;
    }

    //軽減額取得
    function getRankDef(&$model, $dataDiv, $reducIncome, $siblings, $row) {
        $rankNameCd = "G218";
        if ("2016" > CTRL_YEAR) {
            $rankNameCd = "G213";
        } else if ("2016" == CTRL_YEAR && $model->income["GRADE2"] > "01") {
            $rankNameCd = "G213";
        } else if ("2017" == CTRL_YEAR && $model->search["GRADE2"] == "03") {
            $rankNameCd = "G213";
        }
        $query  = " SELECT ";
        $query .= "     T1.PREFECTURESCD, ";
        $query .= "     L2.NAME1 ";
        $query .= " FROM ";
        $query .= "     REDUCTION_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'G202' ";
        $query .= "          AND T1.PREFECTURESCD = L1.NAMECD2 ";
        $query .= "     LEFT JOIN REDUCTION_PREF_GRADE_MST PREF_GRADE ON PREF_GRADE.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND T1.PREFECTURESCD = PREF_GRADE.PREFECTURESCD ";
        $query .= "          AND PREF_GRADE.GRADE = '{$model->search["GRADE"]}' ";
        $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = '{$rankNameCd}' ";
        $query .= "          AND L2.NAMECD2 = T1.INCOME_RANK{$dataDiv}, ";
        $query .= "     (SELECT ";
        $query .= "         ST1.GRADE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ST1 ";
        $query .= "     WHERE ";
        $query .= "         ST1.YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "         ST1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "         ST1.SCHREGNO = '".$row["SCHREGNO"]."' ";
        $query .= "     ) T3 ";
        $query .= " WHERE ";
        $query .= "     T1.PREFECTURESCD = (SELECT ";
        $query .= "                             SUBSTR(MIN(ST1.CITYCD),1,2) ";
        $query .= "                         FROM ";
        $query .= "                             ZIPCD_MST ST1, ";
        $query .= "                             GUARDIAN_DAT ST2 ";
        $query .= "                         WHERE ";
        $query .= "                             ST1.NEW_ZIPCD = ST2.GUARANTOR_ZIPCD AND ";
        $query .= "                             ST2.SCHREGNO = '".$row["SCHREGNO"]."' ";
        $query .= "                         ) AND ";
        $query .= "     T1.GRADE = T3.GRADE AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     {$reducIncome} BETWEEN T1.INCOME_LOW{$dataDiv} AND ";
        $query .= "     T1.INCOME_HIGH{$dataDiv} AND ";
        if (strlen($siblings) > 0) {
            $query .= "     {$siblings} = VALUE(T1.INCOME_SIBLINGS{$dataDiv}, 0) ";
        } else {
            $query .= "     0 = VALUE(T1.INCOME_SIBLINGS{$dataDiv}, 0) ";
        }

        $query .= " ORDER BY ";
        $query .= "     INCOME_RANK{$dataDiv} DESC ";
        $query .= " FETCH FIRST 1 ROWS ONLY ";

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

    //ランク取得
    function getNameMst($model, $nameCd1) {
        $query = " SELECT ";
        $query .= "   NAMECD2 AS VALUE, ";
        $query .= "   NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "   NAME_MST ";
        $query .= " WHERE ";
        $query .= "   NAMECD1 = '{$nameCd1}' ";
        $query .= " ORDER BY ";
        $query .= "   VALUE ";
        return $query;
    }

    //入学年度取得
    function getCurriculumYear($model) {
        $query  = " SELECT ";
        $query .= "   T1.CURRICULUM_YEAR ";
        $query .= " FROM ";
        $query .= "   SCHREG_ENT_GRD_HIST_DAT T1 ";
        $query .= " WHERE ";
        $query .= "   T1.SCHREGNO = '".$model->income["SCHREGNO"]."' ";
        $query .= "   AND T1.SCHOOL_KIND IN (SELECT ";
        $query .= "                             I1.SCHOOL_KIND ";
        $query .= "                          FROM ";
        $query .= "                             SCHREG_REGD_GDAT I1 ";
        $query .= "                          WHERE ";
        $query .= "                             I1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                             AND I1.GRADE = '".$model->income["GRADE2"]."' ) ";

        return $query;
    }

    //軽減額取得
    function selectQueryReductionMonty(&$model) {
        $setFieldNum = "";
        if ($model->income["OBJ_NAME"] == 'REDUC_INCOME_1[]' || $model->income["OBJ_NAME"] == 'INCOME_SIBLINGS1[]' || $model->income["OBJ_NAME"] == 'REDUC_RANK_1[]') {
            $setFieldNum = "1";
        } else {
            $setFieldNum = "2";
        }

        $rankNameCd = "G218";
        if ("2016" > CTRL_YEAR) {
            $rankNameCd = "G213";
        } else if ("2016" == CTRL_YEAR && $model->income["GRADE2"] > "01") {
            $rankNameCd = "G213";
        } else if ("2017" == CTRL_YEAR && $model->search["GRADE2"] == "03") {
            $rankNameCd = "G213";
        }
        $query  = " SELECT ";
        $query .= "     T1.PREFECTURESCD, ";
        $query .= "     T1.REDUCTION_SEQ, ";
        $query .= "     T1.REDUCTIONMONEY_1, ";
        $query .= "     T1.REDUCTIONMONEY_2, ";
        $query .= "     T1.INCOME_SIBLINGS1, ";
        $query .= "     T1.INCOME_SIBLINGS2, ";
        $query .= "     L1.NAME1 AS PREF ";
        $query .= " FROM ";
        $query .= "     REDUCTION_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'G202' ";
        $query .= "          AND T1.PREFECTURESCD = L1.NAMECD2 ";
        $query .= "     LEFT JOIN REDUCTION_PREF_GRADE_MST PREF_GRADE ON PREF_GRADE.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND T1.PREFECTURESCD = PREF_GRADE.PREFECTURESCD ";
        $query .= "          AND PREF_GRADE.GRADE = '{$model->income["GRADE2"]}' ";
        $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = '{$rankNameCd}' ";
        $query .= "          AND L2.NAMECD2 = T1.INCOME_RANK{$setFieldNum}, ";
        $query .= "     (SELECT ";
        $query .= "         ST1.GRADE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ST1 ";
        $query .= "     WHERE ";
        $query .= "         ST1.YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "         ST1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "         ST1.SCHREGNO = '".$model->income["SCHREGNO"]."' ";
        $query .= "     ) T3 ";
        $query .= " WHERE ";
        if (is_numeric($model->income["PREFECTURESCD"])) {     //軽減対象都道府県設定値
            $query .= "     T1.PREFECTURESCD = '".$model->income["PREFECTURESCD"]."' AND ";
        } else {                                                //保護者都道府県
            $query .= "     T1.PREFECTURESCD = (SELECT ";
            $query .= "                             SUBSTR(MIN(ST1.CITYCD),1,2) ";
            $query .= "                         FROM ";
            $query .= "                             ZIPCD_MST ST1, ";
            $query .= "                             GUARDIAN_DAT ST2 ";
            $query .= "                         WHERE ";
            $query .= "                             ST1.NEW_ZIPCD = ST2.GUARANTOR_ZIPCD AND ";
            $query .= "                             ST2.SCHREGNO = '".$model->income["SCHREGNO"]."' ";
            $query .= "                         ) AND ";
        }

        $query .= "     T1.GRADE = T3.GRADE AND ";

        if ($model->income["OBJ_NAME"] == 'REDUC_INCOME_1[]' || $model->income["OBJ_NAME"] == 'REDUC_RANK_1[]') {
            $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
            if ($model->income["INCOME_SIBLINGS1"]) {
                $query .= "     ".$model->income["INCOME_SIBLINGS1"]." = VALUE(T1.INCOME_SIBLINGS1, 0) AND ";
            } else {
                $query .= "     VALUE(T1.INCOME_SIBLINGS1, 0) = 0 AND ";
            }
            if ($model->income["PREF_CD2"] == '28' && $model->income["CASE_CD2"] == 'SH') {
                $query .= "     T1.INCOME_LOW{$setFieldNum} = 999999 AND T1.INCOME_HIGH{$setFieldNum} = 999999 ";
            } else if ($model->income["OBJ_NAME"] == 'REDUC_INCOME_1[]' && strlen($model->income["REDUC_INCOME_1"]) > 0) {
                $query .= "     ".$model->income["REDUC_INCOME_1"]." BETWEEN T1.INCOME_LOW{$setFieldNum} AND ";
                $query .= "     T1.INCOME_HIGH{$setFieldNum} ";
            } else {
                $query .= "     L2.NAMECD2 = T1.INCOME_RANK{$setFieldNum} ";
            }
        } else {
            $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
            if ($model->income["INCOME_SIBLINGS2"]) {
                $query .= "     ".$model->income["INCOME_SIBLINGS2"]." = VALUE(T1.INCOME_SIBLINGS2, 0) AND ";
            } else {
                $query .= "     VALUE(T1.INCOME_SIBLINGS2, 0) = 0 AND ";
            }
            if ($model->income["PREF_CD2"] == '28' && $model->income["CASE_CD2"] == 'SH') {
                $query .= "     T1.INCOME_LOW{$setFieldNum} = 999999 AND T1.INCOME_HIGH{$setFieldNum} = 999999 ";
            } else if ($model->income["OBJ_NAME"] == 'REDUC_INCOME_2[]' && strlen($model->income["REDUC_INCOME_2"]) > 0) {
                $query .= "     ".$model->income["REDUC_INCOME_2"]." BETWEEN T1.INCOME_LOW{$setFieldNum} AND ";
                $query .= "     T1.INCOME_HIGH{$setFieldNum} ";
            } else {
                $query .= "     L2.NAMECD2 = T1.INCOME_RANK{$setFieldNum} ";
            }
        }

        $query .= " ORDER BY ";
        $query .= "     REDUCTIONMONEY_1, ";
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

    function update(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $rankNameCd = "G218";
        if ("2016" > CTRL_YEAR) {
            $rankNameCd = "G213";
        } else if ("2016" == CTRL_YEAR && $model->search["GRADE"] > "01") {
            $rankNameCd = "G213";
        } else if ("2017" == CTRL_YEAR && $model->search["GRADE"] == "03") {
            $rankNameCd = "G213";
        }

        $nameRank = array();
        $query = "SELECT * FROM NAME_MST WHERE NAMECD1 = '{$rankNameCd}' ";
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $nameRank[$row["NAME1"]] = $row["NAMECD2"];
        }
        for($i = 0; $i < get_count($model->schregno); $i++) {
            list($cd, $dummy1, $dummy2) = preg_split("/:/", $model->field["REDUC_RARE_CASE_CD"][$i]);
            $model->field["REDUC_RARE_CASE_CD"][$i] = ($cd == 'DUMMY') ? '' : $cd;

            if (is_array($model->field["REDUC_DEC_FLG_1"])) {
                $reduc_dec_flg_1 = in_array($model->schregno[$i], $model->field["REDUC_DEC_FLG_1"])? 1 : 0;
            } else {
                $reduc_dec_flg_1 = 0;
            }
            if (is_array($model->field["REDUC_DEC_FLG_2"])) {
                $reduc_dec_flg_2 = in_array($model->schregno[$i], $model->field["REDUC_DEC_FLG_2"])? 1 : 0;
            } else {
                $reduc_dec_flg_2 = 0;
            }

            if ($model->field["REDUC_RARE_CASE_CD"][$i] != '') {
                $query = knjp170kQuery::getSpecailCode($model->field["REDUC_RARE_CASE_CD"]);
                $namespare1 = $db->getOne($query);
                list($flg, $dummy) = preg_split("/:/", $namespare1["VALUE"]);
                if ($flg == '1') { //テキスト入力入力有り
                    $reduc_rare_case_cd = true;
                } else { //テキスト入力入力無し
                    $reduc_rare_case_cd = false;
                }
            } else { //テキスト入力入力無し
                $reduc_rare_case_cd = false;
            }

            $query  = " SELECT * FROM";
            $query .= "     REDUCTION_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '".CTRL_YEAR ."' AND ";
            $query .= "     SCHREGNO    = '{$model->schregno[$i]}' ";
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (strlen($model->field["REDUC_INCOME_1"][$i]) == 0 &&
                strlen($model->field["INCOME_SIBLINGS1"][$i]) == 0 &&
                strlen($model->field["REDUC_RANK_1"][$i]) == 0 &&
                strlen($model->field["REDUCTIONMONEY_1"][$i]) == 0 &&
                strlen($model->field["REDUC_INCOME_2"][$i]) == 0 &&
                strlen($model->field["INCOME_SIBLINGS2"][$i]) == 0 &&
                strlen($model->field["REDUC_RANK_2"][$i]) == 0 &&
                strlen($model->field["REDUCTIONMONEY_2"][$i]) == 0 &&
                strlen($model->field["REDUC_RARE_CASE_CD"][$i]) == 0 &&
                strlen($model->field["REDUC_REMARK"][$i]) == 0
            ) {
                /**********/
                /* DELETE */
                /**********/
                $query  = " DELETE FROM";
                $query .= "     REDUCTION_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR     = '".CTRL_YEAR ."' AND ";
                $query .= "     SCHREGNO = '" .$model->schregno[$i] ."' ";
                $result = $db->query($query);

                /**********/
                /* UPDATE */
                /**********/
                $query  = " UPDATE ";
                $query .= "     REDUCTION_COUNTRY_DAT ";
                $query .= " SET ";
                $query .= "     REDUC_REMARK       = NULL, ";
                $query .= "     REGISTERCD         = '".STAFFCD ."', ";
                $query .= "     UPDATED            = sysdate() ";
                $query .= " WHERE ";
                $query .= "     YEAR        = '".CTRL_YEAR ."' ";
                $query .= "     AND SCHREGNO    = '{$model->schregno[$i]}' ";

                $result = $db->query($query);
            } else if (is_array($row)) {
                /**********/
                /* UPDATE */
                /**********/
                $query  = " UPDATE ";
                $query .= "     REDUCTION_DAT ";
                $query .= " SET ";
                //府県コード
                $query .= " PREFECTURESCD = '{$model->field["PREFECTURESCD"][$i]}', ";

                $reduc_income_1   = strlen($model->field["REDUC_INCOME_1"][$i]) > 0   ? $model->field["REDUC_INCOME_1"][$i]   : 'null';
                $siblings_1   = strlen($model->field["INCOME_SIBLINGS1"][$i]) > 0   ? $model->field["INCOME_SIBLINGS1"][$i]   : 'null';
                $reduc_rank_1   = strlen($model->field["REDUC_RANK_1"][$i]) > 0   ? "'{$nameRank[$model->field["REDUC_RANK_1"][$i]]}'"   : "NULL";
                $reduc_income_2   = strlen($model->field["REDUC_INCOME_2"][$i]) > 0   ? $model->field["REDUC_INCOME_2"][$i]   : 'null';
                $siblings_2   = strlen($model->field["INCOME_SIBLINGS2"][$i]) > 0   ? $model->field["INCOME_SIBLINGS2"][$i]   : 'null';
                $reduc_rank_2   = strlen($model->field["REDUC_RANK_2"][$i]) > 0   ? "'{$nameRank[$model->field["REDUC_RANK_2"][$i]]}'"   : "NULL";
                $reductionmoney_1 = strlen($model->field["REDUCTIONMONEY_1"][$i]) > 0 ? $model->field["REDUCTIONMONEY_1"][$i] : 'null';
                $reductionmoney_2 = strlen($model->field["REDUCTIONMONEY_2"][$i]) > 0 ? $model->field["REDUCTIONMONEY_2"][$i] : 'null';

                if ($reduc_rare_case_cd) { //軽減額入力あり
                    $query .= "     REDUCTION_SEQ_1  = Null, "; //連番
                    $query .= "     REDUCTION_SEQ_2  = Null, "; //連番
                } else {  //軽減額入力無し
                    if ($model->field["REDUCTION_SEQ_1"][$i] != "" ) {
                        $query .= " REDUCTION_SEQ_1  = {$model->field["REDUCTION_SEQ_1"][$i]} , "; //連番
                    }
                    if ($model->field["REDUCTION_SEQ_2"][$i] != "" ) {
                        $query .= " REDUCTION_SEQ_2  = {$model->field["REDUCTION_SEQ_2"][$i]} , "; //連番
                    }
                }

                $query .= "     REDUCTIONMONEY_1   =  {$reductionmoney_1}, ";
                $query .= "     REDUC_DEC_FLG_1    = '{$reduc_dec_flg_1}', ";
                $query .= "     REDUC_INCOME_1     =  {$reduc_income_1}, ";
                $query .= "     INCOME_SIBLINGS1   =  {$siblings_1}, ";
                $query .= "     REDUC_RANK_1       =  {$reduc_rank_1}, ";
                $query .= "     REDUCTIONMONEY_2   =  {$reductionmoney_2}, ";
                $query .= "     REDUC_DEC_FLG_2    = '{$reduc_dec_flg_2}', ";
                $query .= "     REDUC_INCOME_2     =  {$reduc_income_2}, ";
                $query .= "     INCOME_SIBLINGS2   =  {$siblings_2}, ";
                $query .= "     REDUC_RANK_2       =  {$reduc_rank_2}, ";
                $query .= "     REDUC_RARE_CASE_CD = '{$model->field["REDUC_RARE_CASE_CD"][$i]}', ";
                $query .= "     REDUC_REMARK       = '".addslashes($model->field["REDUC_REMARK"][$i])."', ";
                $query .= "     REGISTERCD         = '".STAFFCD ."', ";
                $query .= "     UPDATED            = sysdate() ";
                $query .= " WHERE ";
                $query .= "     YEAR        = '".CTRL_YEAR ."' AND ";
                $query .= "     SCHREGNO    = '{$model->schregno[$i]}' ";

                $result = $db->query($query);

                /**********/
                /* UPDATE */
                /**********/
                $query  = " UPDATE ";
                $query .= "     REDUCTION_COUNTRY_DAT ";
                $query .= " SET ";
                $query .= "     REDUC_REMARK       = '".addslashes($model->field["REDUC_REMARK"][$i])."', ";
                $query .= "     REGISTERCD         = '".STAFFCD ."', ";
                $query .= "     UPDATED            = sysdate() ";
                $query .= " WHERE ";
                $query .= "     YEAR        = '".CTRL_YEAR ."' ";
                $query .= "     AND SCHREGNO    = '{$model->schregno[$i]}' ";

                $result = $db->query($query);

            } else {

                /**********/
                /* INSERT */
                /**********/
                $query  = " INSERT INTO REDUCTION_DAT( ";
                if ($model->field["PREFECTURESCD"][$i] != "" ) {
                    $query .= " PREFECTURESCD, ";
                }
                if ($model->field["REDUCTION_SEQ_1"][$i] != "" ) {
                    $query .= " REDUCTION_SEQ_1, ";
                }
                if ($model->field["REDUCTION_SEQ_2"][$i] != "" ) {
                    $query .= " REDUCTION_SEQ_2, ";
                }
                $query .= " YEAR, ";
                $query .= " SCHREGNO, ";
                $query .= " GRADE, ";
                $query .= " REDUCTIONMONEY_1, ";
                $query .= " REDUC_DEC_FLG_1, ";
                $query .= " REDUC_INCOME_1, ";
                $query .= " INCOME_SIBLINGS1, ";
                $query .= " REDUC_RANK_1, ";
                $query .= " REDUCTIONMONEY_2, ";
                $query .= " REDUC_DEC_FLG_2, ";
                $query .= " REDUC_INCOME_2, ";
                $query .= " INCOME_SIBLINGS2, ";
                $query .= " REDUC_RANK_2, ";
                $query .= " REDUC_RARE_CASE_CD, ";
                $query .= " REDUC_REMARK, ";
                $query .= " REGISTERCD, ";
                $query .= " UPDATED ";
                $query .= " ) ";
                $query .= " VALUES ";
                $query .= " ( ";
                if ($model->field["PREFECTURESCD"][$i] != "" ) {
                    $query .= " '{$model->field["PREFECTURESCD"][$i]}', ";
                }
                if ($model->field["REDUCTION_SEQ_1"][$i] != "" ) {
                    $query .= " {$model->field["REDUCTION_SEQ_1"][$i]}, ";
                }
                if ($model->field["REDUCTION_SEQ_2"][$i] != "" ) {
                    $query .= " {$model->field["REDUCTION_SEQ_2"][$i]}, ";
                }

                $reduc_income_1   = strlen($model->field["REDUC_INCOME_1"][$i]) > 0   ? $model->field["REDUC_INCOME_1"][$i]   : 'null';
                $siblings_1   = strlen($model->field["INCOME_SIBLINGS1"][$i]) > 0   ? $model->field["INCOME_SIBLINGS1"][$i]   : 'null';
                $reduc_rank_1   = strlen($model->field["REDUC_RANK_1"][$i]) > 0   ? "'{$nameRank[$model->field["REDUC_RANK_1"][$i]]}'"   : "NULL";
                $reduc_income_2   = strlen($model->field["REDUC_INCOME_2"][$i]) > 0   ? $model->field["REDUC_INCOME_2"][$i]   : 'null';
                $siblings_2   = strlen($model->field["INCOME_SIBLINGS2"][$i]) > 0   ? $model->field["INCOME_SIBLINGS2"][$i]   : 'null';
                $reduc_rank_2   = strlen($model->field["REDUC_RANK_2"][$i]) > 0   ? "'{$nameRank[$model->field["REDUC_RANK_2"][$i]]}'"   : "NULL";
                $reductionmoney_1 = strlen($model->field["REDUCTIONMONEY_1"][$i]) > 0 ? $model->field["REDUCTIONMONEY_1"][$i] : 'null';
                $reductionmoney_2 = strlen($model->field["REDUCTIONMONEY_2"][$i]) > 0 ? $model->field["REDUCTIONMONEY_2"][$i] : 'null';

                $query .= " '".CTRL_YEAR ."', ";
                $query .= " '{$model->schregno[$i]}', ";
                $query .= " '{$model->search["GRADE"]}', ";
                $query .= "  {$reductionmoney_1}, ";
                $query .= " '{$reduc_dec_flg_1}', ";
                $query .= "  {$reduc_income_1}, ";
                $query .= "  {$siblings_1}, ";
                $query .= "  {$reduc_rank_1}, ";
                $query .= "  {$reductionmoney_2}, ";
                $query .= " '{$reduc_dec_flg_2}', ";
                $query .= "  {$reduc_income_2}, ";
                $query .= "  {$siblings_2}, ";
                $query .= "  {$reduc_rank_2}, ";
                $query .= " '{$model->field["REDUC_RARE_CASE_CD"][$i]}', ";
                $query .= ($model->field["REDUC_REMARK"][$i])   ? " '" .addslashes($model->field["REDUC_REMARK"][$i]) ."', " : " null, ";
                $query .= " '".STAFFCD ."', ";
                $query .= " sysdate() ";
                $query .= " ) ";

                $result = $db->query($query);

                /**********/
                /* UPDATE */
                /**********/
                $query  = " UPDATE ";
                $query .= "     REDUCTION_COUNTRY_DAT ";
                $query .= " SET ";
                $query .= "     REDUC_REMARK       = '".addslashes($model->field["REDUC_REMARK"][$i])."', ";
                $query .= "     REGISTERCD         = '".STAFFCD ."', ";
                $query .= "     UPDATED            = sysdate() ";
                $query .= " WHERE ";
                $query .= "     YEAR        = '".CTRL_YEAR ."' AND ";
                $query .= "     SCHREGNO    = '{$model->schregno[$i]}' ";

                $result = $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
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
        $query .= "     NAMECD1 = 'G211' AND ";
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
        $query .= "     NAMECD1 = 'G211' AND ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     NAMECD2 = '{$reduc_rare_case_cd}' ";

        return $query;
    }
}
?>