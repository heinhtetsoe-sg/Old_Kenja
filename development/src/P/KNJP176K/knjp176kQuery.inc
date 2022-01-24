<?php

require_once('for_php7.php');

class knjp176kQuery extends Query {
    function selectQuery(&$model) {
        //変数
        $sdate =  CTRL_YEAR      ."-04-01";
        $edate = (CTRL_YEAR + 1) ."-03-31";
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
        $query .= "   T3.OFFSET_FLG, ";
        $query .= "   RARE.NAMECD2 || ':' || RARE.NAMESPARE1 AS REDUC_RARE_CASE_CD_1, ";
        $query .= "   RARE.NAMESPARE1 AS TEXT_DISP1, ";
        $query .= "   T3.REDUCTION_SEQ_1, ";
        $query .= "   T3.REDUC_INCOME_1, ";
        $query .= "   T3.INCOME_SIBLINGS1, ";
        $query .= "   CASE WHEN RARE.NAMESPARE1 like '1:%' ";
        $query .= "        THEN T3.REDUCTIONMONEY_1 ";
        $query .= "        ELSE L1.REDUCTIONMONEY1 ";
        $query .= "   END AS BASE_MONEY_1, ";
        $query .= "   T3.REDUCTIONMONEY_1, ";
        $query .= "   T3.REDUC_DEC_FLG_1, ";
        $query .= "   CASE WHEN RARE.NAMESPARE1 like '1:%' ";
        $query .= "        THEN T3.REDUCTION_ADD_MONEY_1 ";
        $query .= "        ELSE L1.REDUCTION_ADD_MONEY1 ";
        $query .= "   END AS REDUCTION_ADD_MONEY_1, ";
        $query .= "   T3.REDUC_ADD_FLG_1, ";
        $query .= "   RARE2.NAMECD2 || ':' || RARE2.NAMESPARE1 AS REDUC_RARE_CASE_CD_2, ";
        $query .= "   RARE2.NAMESPARE1 AS TEXT_DISP2, ";
        $query .= "   T3.REDUCTION_SEQ_2, ";
        $query .= "   T3.REDUC_INCOME_2, ";
        $query .= "   T3.INCOME_SIBLINGS2, ";
        $query .= "   CASE WHEN RARE2.NAMESPARE1 like '1:%' ";
        $query .= "        THEN T3.REDUCTIONMONEY_2 ";
        $query .= "        ELSE L2.REDUCTIONMONEY1 ";
        $query .= "   END AS BASE_MONEY_2, ";
        $query .= "   T3.REDUCTIONMONEY_2, ";
        $query .= "   T3.REDUC_DEC_FLG_2, ";
        $query .= "   CASE WHEN RARE2.NAMESPARE1 like '1:%' ";
        $query .= "        THEN T3.REDUCTION_ADD_MONEY_2 ";
        $query .= "        ELSE L2.REDUCTION_ADD_MONEY1 ";
        $query .= "   END AS REDUCTION_ADD_MONEY_2, ";
        $query .= "   T3.REDUC_ADD_FLG_2, ";
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
        $query .= "   value(T6.COUNTTRANSFER,0) + value(G6.COUNTTRANSFER,0) + value(E6.COUNTTRANSFER,0) AS COUNTTRANSFER, ";
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
        $query .= "                            ST1.YEAR, ";
        $query .= "                            ST1.SCHREGNO, ";
        $query .= "                            ST1.OFFSET_FLG, ";
        $query .= "                            ST1.REDUC_RARE_CASE_CD_1, ";
        $query .= "                            ST1.REDUCTION_SEQ_1, ";
        $query .= "                            ST1.REDUC_INCOME_1, ";
        $query .= "                            ST1.INCOME_SIBLINGS1, ";
        $query .= "                            ST1.REDUCTIONMONEY_1, ";
        $query .= "                            ST1.REDUC_DEC_FLG_1, ";
        $query .= "                            ST1.REDUCTION_ADD_MONEY_1, ";
        $query .= "                            ST1.REDUC_ADD_FLG_1, ";
        $query .= "                            ST1.REDUC_RARE_CASE_CD_2, ";
        $query .= "                            ST1.REDUCTION_SEQ_2, ";
        $query .= "                            ST1.REDUC_INCOME_2, ";
        $query .= "                            ST1.INCOME_SIBLINGS2, ";
        $query .= "                            ST1.REDUCTIONMONEY_2, ";
        $query .= "                            ST1.REDUC_DEC_FLG_2, ";
        $query .= "                            ST1.REDUCTION_ADD_MONEY_2, ";
        $query .= "                            ST1.REDUC_ADD_FLG_2, ";
        $query .= "                            ST1.REDUC_REMARK, ";
        $query .= "                            VALUE(ST1.REDUCTIONMONEY_1, 0) + VALUE(ST1.REDUCTIONMONEY_2, 0) AS TOTAL_MONEY ";
        $query .= "                        FROM ";
        $query .= "                            REDUCTION_COUNTRY_DAT ST1 ";
        $query .= "                        ) T3 ON  T1.YEAR     = T3.YEAR ";
        $query .= "                             AND T1.SCHREGNO = T3.SCHREGNO ";
        $query .= "       LEFT JOIN NAME_MST RARE ON RARE.NAMECD1 = 'G216' ";
        $query .= "            AND RARE.NAMECD2 = T3.REDUC_RARE_CASE_CD_1 ";
        $query .= "       LEFT JOIN NAME_MST RARE2 ON RARE2.NAMECD1 = 'G216' ";
        $query .= "            AND RARE2.NAMECD2 = T3.REDUC_RARE_CASE_CD_1 ";
        $query .= "       LEFT JOIN REDUCTION_DAT SUB_REMARK ON T1.YEAR = SUB_REMARK.YEAR ";
        $query .= "            AND T1.SCHREGNO = SUB_REMARK.SCHREGNO ";
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
        $query .= "            AND T1.GRADE = L1.GRADE ";
        $query .= "            AND T3.REDUC_INCOME_1 BETWEEN L1.INCOME_LOW1 AND L1.INCOME_HIGH1 ";
        $query .= "       LEFT JOIN REDUCTION_COUNTRY_MST L2 ON L2.YEAR = '".CTRL_YEAR."' ";
        $query .= "            AND T1.GRADE = L2.GRADE ";
        $query .= "            AND T3.REDUC_INCOME_2 BETWEEN L2.INCOME_LOW2 AND L2.INCOME_HIGH2 ";
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
        $query .= "   NAME1 ";
        $query .= " FROM ";
        $query .= "   NAME_MST ";
        $query .= " WHERE ";
        $query .= "   NAMECD1 = 'G202' ";
        $query .= " ORDER BY ";
        $query .= "   NAMECD2 ";
        return $query;
    }

    //軽減額取得
    function selectQueryReductionMonty(&$model) {
        $setSoeji = "1";
        if ($model->income["OBJ_NAME"] == 'REDUC_INCOME_1[]' || $model->income["OBJ_NAME"] == 'INCOME_SIBLINGS1[]') {
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

    function update($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        for($i = 0; $i < get_count($model->schregno); $i++) {
            $schregNo = $model->schregno[$i];
            //計画データの有無
            $planCnt = $db->getRow(knjp176kQuery::getPlanCnt($schregNo), DB_FETCHMODE_ASSOC);

            /*************************/
            /* REDUCTION_COUNTRY_DAT */
            /*************************/
            $reduc_dec_flg_2 = NULL;
            if (is_array($model->field["OFFSET_FLG"])) {
                $offSetFlg = in_array($schregNo, $model->field["OFFSET_FLG"])? 1 : NULL;
            } else {
                $offSetFlg = NULL;
            }
            if (is_array($model->field["REDUC_ADD_FLG_1"])) {
                $addFlg1 = in_array($schregNo, $model->field["REDUC_ADD_FLG_1"])? 1 : NULL;
            } else {
                $addFlg1 = NULL;
            }
            if (is_array($model->field["REDUC_ADD_FLG_2"])) {
                $addFlg2 = in_array($schregNo, $model->field["REDUC_ADD_FLG_2"])? 1 : NULL;
            } else {
                $addFlg2 = NULL;
            }
            $data = array();
            $data["GRADE"][TEXT]                    = $model->search["GRADE"];
            $data["OFFSET_FLG"][TEXT]               = $offSetFlg;

            list($cd, $baseDummy1, $dummy2) = preg_split("/:/", $model->field["REDUC_RARE_CASE_CD_1"][$i]);
            list($cd, $addDummy1, $dummy2) = preg_split("/:/", $model->field["REDUC_RARE_CASE_CD_1"][$i]);
            $model->field["REDUC_RARE_CASE_CD_1"][$i] = ($cd == 'DUMMY') ? '' : $cd;
            $data["REDUC_RARE_CASE_CD_1"][TEXT]       = $model->field["REDUC_RARE_CASE_CD_1"][$i] != "" ? $model->field["REDUC_RARE_CASE_CD_1"][$i] : NULL;

            $data["REDUCTION_SEQ_1"][NUMBER]        = $model->field["REDUCTION_SEQ_1"][$i] != "" ? $model->field["REDUCTION_SEQ_1"][$i] : NULL;
            $data["REDUCTIONMONEY_1"][NUMBER]       = strlen($model->field["BASE_MONEY_1"][$i]) > 0 ? $model->field["BASE_MONEY_1"][$i] : NULL;
            $data["REDUC_DEC_FLG_1"][TEXT]          = $addFlg1;
            $data["REDUCTION_ADD_MONEY_1"][NUMBER]  = strlen($model->field["REDUCTION_ADD_MONEY_1"][$i]) > 0 ? $model->field["REDUCTION_ADD_MONEY_1"][$i]   : NULL;
            $data["REDUC_ADD_FLG_1"][TEXT]          = $addFlg1;
            $data["REDUC_INCOME_1"][NUMBER]         = strlen($model->field["REDUC_INCOME_1"][$i]) > 0 ? $model->field["REDUC_INCOME_1"][$i]   : NULL;
            $data["INCOME_SIBLINGS1"][NUMBER]       = strlen($model->field["INCOME_SIBLINGS1"][$i]) > 0 ? $model->field["INCOME_SIBLINGS1"][$i] : NULL;

            $model->field["REDUC_RARE_CASE_CD_2"][$i] = ($cd == 'DUMMY') ? '' : $cd;
            $data["REDUC_RARE_CASE_CD_2"][TEXT]       = $model->field["REDUC_RARE_CASE_CD_1"][$i] != "" ? $model->field["REDUC_RARE_CASE_CD_1"][$i] : NULL;

            $data["REDUCTION_SEQ_2"][NUMBER]        = $model->field["REDUCTION_SEQ_2"][$i] != "" ? $model->field["REDUCTION_SEQ_2"][$i] : NULL;
            $data["REDUCTIONMONEY_2"][NUMBER]       = strlen($model->field["BASE_MONEY_2"][$i]) > 0 ? $model->field["BASE_MONEY_2"][$i]   : NULL;
            $data["REDUC_DEC_FLG_2"][TEXT]          = $addFlg2;
            $data["REDUCTION_ADD_MONEY_2"][NUMBER]  = strlen($model->field["REDUCTION_ADD_MONEY_2"][$i]) > 0 ? $model->field["REDUCTION_ADD_MONEY_2"][$i] : NULL;
            $data["REDUC_ADD_FLG_2"][TEXT]          = $addFlg2;
            $data["REDUC_INCOME_2"][NUMBER]         = strlen($model->field["REDUC_INCOME_2"][$i]) > 0 ? $model->field["REDUC_INCOME_2"][$i]   : NULL;
            $data["INCOME_SIBLINGS2"][NUMBER]       = strlen($model->field["INCOME_SIBLINGS2"][$i]) > 0 ? $model->field["INCOME_SIBLINGS2"][$i] : NULL;

            $data["REDUC_REMARK"][TEXT]             = $model->field["REDUC_REMARK"][$i]   ? $model->field["REDUC_REMARK"][$i]   : NULL;
            $data["REGISTERCD"][TEXT]               = STAFFCD;
            $data["UPDATED"][FUNC]                  = "SYSDATE()";

            $query  = " SELECT * FROM";
            $query .= "     REDUCTION_COUNTRY_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '".CTRL_YEAR ."' AND ";
            $query .= "     SCHREGNO    = '{$schregNo}' ";
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            if (is_array($row)) {
                /**********/
                /* UPDATE */
                /**********/

                $where  = " WHERE ";
                $where .= "     YEAR        = '".CTRL_YEAR ."' AND ";
                $where .= "     SCHREGNO    = '{$schregNo}' ";

                $query = Query::updateSQL($data, "REDUCTION_COUNTRY_DAT", $where);
                $result = $db->query($query);
            } else {

                /**********/
                /* INSERT */
                /**********/
                $data["YEAR"][TEXT]     = CTRL_YEAR;
                $data["SCHREGNO"][TEXT] = $schregNo;

                $query = Query::insertSQL($data, "REDUCTION_COUNTRY_DAT");
                $result = $db->query($query);
            }

            /**********/
            /* UPDATE */
            /**********/
            $setRemark = $model->field["REDUC_REMARK"][$i] ? $model->field["REDUC_REMARK"][$i] : NULL;
            $query  = " UPDATE ";
            $query .= "     REDUCTION_DAT ";
            $query .= " SET ";
            $query .= "     REDUC_REMARK       = '".$setRemark."', ";
            $query .= "     REGISTERCD         = '".STAFFCD ."', ";
            $query .= "     UPDATED            = sysdate() ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '".CTRL_YEAR ."' ";
            $query .= "     AND SCHREGNO    = '{$schregNo}' ";

            $result = $db->query($query);

            /******************************/
            /* REDUCTION_COUNTRY_PLAN_DAT */
            /******************************/
            //メインデータ取得
            $reducDat = $db->getRow(knjp176kQuery::getReducMoney($schregNo), DB_FETCHMODE_ASSOC);
            $zenkiData = array("DEC" => $reducDat["REDUC_DEC_FLG_1"] == "1" ? $reducDat["REDUCTIONMONEY_1"] : NULL,
                               "ADD" => $reducDat["REDUC_ADD_FLG_1"] == "1" ? $reducDat["REDUCTION_ADD_MONEY_1"] : NULL);
            $koukiData = array("DEC" => $reducDat["REDUC_DEC_FLG_2"] == "1" ? $reducDat["REDUCTIONMONEY_2"] : NULL,
                               "ADD" => $reducDat["REDUC_ADD_FLG_2"] == "1" ? $reducDat["REDUCTION_ADD_MONEY_2"] : NULL);

            if ($baseDummy1 == "1") {
                $zenkiMonth = array("06");
            } else {
                $zenkiMonth = array("04", "05", "06");
            }
            if ($addDummy1 == "1") {
                $koukiMonth = array("03");
            } else {
                $koukiMonth = array("07", "08", "09", "10", "11", "12", "01", "02", "03");
            }
            if ($planCnt["ALLCNT"] == 0) {
                foreach ($zenkiMonth as $key => $val) {
                    knjp176kQuery::insertPlan($db, $model, $reducDat, $val, $zenkiData);
                }
                foreach ($koukiMonth as $key => $val) {
                    knjp176kQuery::insertPlan($db, $model, $reducDat, $val, $koukiData);
                }
            } else {
                if ($planCnt["CNT"] == "0" || $planCnt["ZENKI_CNT"] == "0") {
                    foreach ($zenkiMonth as $key => $val) {
                        knjp176kQuery::updatePlan($db, $model, $reducDat, $val, $zenkiData, $planCnt, "ZENKI");
                    }
                }
                if ($planCnt["CNT"] == "0" || $planCnt["KOUKI_CNT"] == "0") {
                    foreach ($koukiMonth as $key => $val) {
                        knjp176kQuery::updatePlan($db, $model, $reducDat, $val, $koukiData, $planCnt, "KOUKI");
                    }
                }
            }

            //KNJD170の更新 特殊フラグ != 'T' 且つ 所得割額が不一致 が対象
            $query = knjp176kQuery::getRaducRareCase($schregNo);
            $rareCnt = $db->getOne($query);
            $query = knjp176kQuery::getRaducIncome1($schregNo);
            $reducIncome = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($rareCnt == 0 && ($model->field["REDUC_INCOME_1"][$i] != $reducIncome["REDUC_INCOME_1"] || $model->field["REDUC_INCOME_2"][$i] != $reducIncome["REDUC_INCOME_2"])) {
                $query = knjp176kQuery::delReductionDat($schregNo);
                $db->query($query);
                // Delete→Insertになったので
                // $reducCnt、$reduc1Cnt、$reduc2Cntは不要だが残しておく
                $query = knjp176kQuery::reductionDatCnt("", $schregNo);
                $reducCnt = $db->getOne($query);
                $query = knjp176kQuery::reductionDatCnt("1", $schregNo);
                $reduc1Cnt = $db->getOne($query);
                $query = knjp176kQuery::reductionDatCnt("2", $schregNo);
                $reduc2Cnt = $db->getOne($query);

                //都道府県
                $rankUsePref = array();
                $result = $db->query(knjp176kQuery::selectQueryPref170($model));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($row["USE_RANK"] == "1") {
                        $rankUsePref[$row["NAMECD2"]] = $row;
                    }
                }
                $usePref = $model->schregnoPref[$schregNo];
                $rankFlg1 = is_array($rankUsePref[$usePref]) && CTRL_YEAR >= $rankUsePref[$usePref]["ZENKI_KAISI_YEAR"] ? "1" : "2";
                $rankFlg2 = is_array($rankUsePref[$usePref]) && CTRL_YEAR >= $rankUsePref[$usePref]["KOUKI_KAISI_YEAR"] ? "1" : "2";

                //金額入力
                $g202Spare2 = $db->getOne(knjp176kQuery::getG202NameSpare2($model, $usePref));
                $curriculumQuery = knjp176kQuery::getCurriculumYear($model, $schregNo);
                $curriculumYear = $db->getOne($curriculumQuery);
                $useCurriculumYear = false;
                if ($g202Spare2 && !$curriculumYear) {
                    continue;
                } else {
                    $curriculumYear = $g202Spare2 ? $curriculumYear : CTRL_YEAR;
                    $useCurriculumYear = $g202Spare2 ? true : false;
                }

                //軽減額取得
                if ($rankFlg1 == "2" && is_numeric($model->field["REDUC_INCOME_1"][$i])) {
                    $query = knjp176kQuery::selectQueryReductionMony1($model, $schregNo, $curriculumYear, $useCurriculumYear, $rankFlg1, $model->field["REDUC_INCOME_1"][$i], $model->field["INCOME_SIBLINGS1"][$i]);
                    $row1 = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    $query = knjp176kQuery::insUpdReducDat($model, "1", $schregNo, $row1, $model->field["REDUC_INCOME_1"][$i], $model->field["INCOME_SIBLINGS1"][$i], $reducCnt);
                    $db->query($query);
                    $reducCnt = 1;
                }

                if ($rankFlg2 == "2" && is_numeric($model->field["REDUC_INCOME_2"][$i])) {
                    //軽減額取得
                    $query = knjp176kQuery::selectQueryReductionMony2($model, $schregNo, $curriculumYear, $useCurriculumYear, $rankFlg2, $model->field["REDUC_INCOME_2"][$i], $model->field["INCOME_SIBLINGS2"][$i]);
                    $row2 = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    $query = knjp176kQuery::insUpdReducDat($model, "2", $schregNo, $row2, $model->field["REDUC_INCOME_2"][$i], $model->field["INCOME_SIBLINGS2"][$i], $reducCnt);
                    $db->query($query);
                }
            }

        }
        $db->commit();
        Query::dbCheckIn($db);
    }

    function getRaducRareCase($schregNo) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     REDUCTION_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR ."' ";
        $query .= "     AND SCHREGNO    = '{$schregNo}' ";
        $query .= "     AND REDUC_RARE_CASE_CD    = 'T' ";

        return $query;
    }

    function getRaducIncome1($schregNo) {
        $query  = " SELECT ";
        $query .= "     REDUC_INCOME_1, ";
        $query .= "     REDUC_INCOME_2 ";
        $query .= " FROM ";
        $query .= "     REDUCTION_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR ."' ";
        $query .= "     AND SCHREGNO    = '{$schregNo}' ";

        return $query;
    }

    function delReductionDat($schregNo) {
        $query  = " DELETE FROM REDUCTION_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR ."' AND ";
        $query .= "     SCHREGNO    = '{$schregNo}' ";

        return $query;
    }

    function insUpdReducDat($model, $div, $schregNo, $row, $income, $siblings, $reducCnt) {
        $data = array();
        $data["PREFECTURESCD"][TEXT]            = $row["PREFECTURESCD"];
        $data["GRADE"][TEXT]                    = $model->search["GRADE"];
        $data["REDUCTION_SEQ_".$div][NUMBER]    = $row["REDUCTION_SEQ"];
        $data["REDUCTIONMONEY_".$div][NUMBER]   = $row["REDUCTIONMONEY_".$div];
        $data["REDUC_DEC_FLG_".$div][TEXT]      = "0";
        $data["REDUC_INCOME_".$div][NUMBER]     = $income;
        $data["INCOME_SIBLINGS".$div][NUMBER]   = $siblings;
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "SYSDATE()";
        if ($reducCnt > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR        = '".CTRL_YEAR ."' AND ";
            $where .= "     SCHREGNO    = '{$schregNo}' ";

            $query = Query::updateSQL($data, "REDUCTION_DAT", $where);
        } else {
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]             = $schregNo;

            $query = Query::insertSQL($data, "REDUCTION_DAT");
        }

        return $query;
    }

    function insertPlan($db, $model, $reducDat, $month, $moneyData) {
        $data = array();

        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SCHREGNO"][TEXT]         = $reducDat["SCHREGNO"];
        $data["PLAN_YEAR"][TEXT]        = $month < "04" ? CTRL_YEAR + 1 : CTRL_YEAR;
        $data["PLAN_MONTH"][TEXT]       = $month;
        $data["PLAN_MONEY"][NUMBER]     = $moneyData["DEC"];
        $data["ADD_PLAN_MONEY"][NUMBER] = $moneyData["ADD"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($data, "REDUCTION_COUNTRY_PLAN_DAT");

        $db->query($query);
    }

    function updatePlan($db, $model, $reducDat, $month, $moneyData, $planCnt, $div) {
        $data = array();

        if ($planCnt[$div."_CNT"] == "0") {
            $data["PLAN_MONEY"][NUMBER]     = $moneyData["DEC"];
        }
        if ($planCnt[$div."_ADD_CNT"] == "0") {
            $data["ADD_PLAN_MONEY"][NUMBER] = $moneyData["ADD"];
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $setYear = $month < "04" ? CTRL_YEAR + 1 : CTRL_YEAR;
        $where  = " WHERE ";
        $where .= "     YEAR            = '".CTRL_YEAR."' ";
        $where .= "     AND SCHREGNO    = '".$reducDat["SCHREGNO"]."' ";
        $where .= "     AND PLAN_YEAR   = '".$setYear."' ";
        $where .= "     AND PLAN_MONTH  = '{$month}' ";

        $query = Query::updateSQL($data, "REDUCTION_COUNTRY_PLAN_DAT", $where);

        $db->query($query);
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

    //REDUCTION_DAT有無
    function reductionDatCnt($div, $schregNo) {
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     REDUCTION_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";
        $query .= "     AND SCHREGNO = '{$schregNo}' ";
        if ($div) {
            $query .= "     AND REDUC_INCOME_{$div} IS NOT NULL ";
        }

        return $query;
    }

    //都道府県
    function selectQueryPref170(&$model) {
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

    //都道府県
    function getG202NameSpare2($model, $usePref) {
        $query  = " SELECT ";
        $query .= "   COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "   NAME_MST T1 ";
        $query .= "   LEFT JOIN REDUCTION_PREF_GRADE_MST PREF_GRADE ON PREF_GRADE.YEAR = '".CTRL_YEAR."' ";
        $query .= "        AND T1.NAMECD2 = PREF_GRADE.PREFECTURESCD ";
        $query .= "        AND PREF_GRADE.GRADE = '{$model->search["GRADE"]}' ";
        $query .= " WHERE ";
        $query .= "   NAMECD1 = 'G202' ";
        $query .= "   AND NAMECD2 = '{$usePref}' ";
        $query .= "   AND PREF_GRADE.CURRICULUM_FLG = '1' ";
        return $query;
    }

    //入学年度取得
    function getCurriculumYear($model, $schregNo) {
        $query  = " SELECT ";
        $query .= "   T1.CURRICULUM_YEAR ";
        $query .= " FROM ";
        $query .= "   SCHREG_ENT_GRD_HIST_DAT T1 ";
        $query .= " WHERE ";
        $query .= "   T1.SCHREGNO = '{$schregNo}' ";
        $query .= "   AND T1.SCHOOL_KIND IN (SELECT ";
        $query .= "                             I1.SCHOOL_KIND ";
        $query .= "                          FROM ";
        $query .= "                             SCHREG_REGD_GDAT I1 ";
        $query .= "                          WHERE ";
        $query .= "                             I1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                             AND I1.GRADE = '".$model->search["GRADE"]."' ) ";

        return $query;
    }

    //軽減額取得
    function selectQueryReductionMony1(&$model, $schregNo, $curriculumYear, $useCurriculumYear, $rankFlg1, $income1, $siblings) {
        $query  = " SELECT ";
        $query .= "     T1.PREFECTURESCD, ";
        $query .= "     T1.REDUCTION_SEQ, ";
        $query .= "     T1.REDUCTIONMONEY_1, ";
        $query .= "     T1.REDUCTIONMONEY_2, ";
        $query .= "     L1.NAME1 AS PREF ";
        $query .= " FROM ";
        $query .= "     REDUCTION_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'G202' ";
        $query .= "          AND T1.PREFECTURESCD = L1.NAMECD2 ";
        $query .= "     LEFT JOIN REDUCTION_PREF_GRADE_MST PREF_GRADE ON PREF_GRADE.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND T1.PREFECTURESCD = PREF_GRADE.PREFECTURESCD ";
        $query .= "          AND PREF_GRADE.GRADE = '{$model->search["GRADE"]}', ";
        $query .= "     (SELECT ";
        $query .= "         ST1.GRADE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ST1 ";
        $query .= "     WHERE ";
        $query .= "         ST1.YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "         ST1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "         ST1.SCHREGNO = '{$schregNo}' ";
        $query .= "     ) T3, ";
        $query .= "     (SELECT ";
        $query .= "         MAX(ST1.GRADE) AS GRADE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ST1 ";
        $query .= "     WHERE ";
        $query .= "         ST1.YEAR    = '{$model->mstLastYear}' AND ";
        $query .= "         ST1.SCHREGNO = '{$schregNo}' ";
        $query .= "     ) T4 ";
        $query .= " WHERE ";
        $query .= "     T1.PREFECTURESCD = (SELECT ";
        $query .= "                             SUBSTR(MIN(ST1.CITYCD),1,2) ";
        $query .= "                         FROM ";
        $query .= "                             ZIPCD_MST ST1, ";
        $query .= "                             GUARDIAN_DAT ST2 ";
        $query .= "                         WHERE ";
        $query .= "                             ST1.NEW_ZIPCD = ST2.GUARANTOR_ZIPCD AND ";
        $query .= "                             ST2.SCHREGNO = '{$schregNo}' ";
        $query .= "                         ) AND ";

        if ($useCurriculumYear) {
            $query .= "     T1.GRADE = '01' AND ";
        } else {
            $query .= "     T1.GRADE = CASE WHEN PREF_GRADE.THIS_YEAR_FLG = '1' ";
            $query .= "                     THEN T3.GRADE ";
            $query .= "                     ELSE T4.GRADE ";
            $query .= "                END AND ";
        }

        $query .= "     T1.YEAR = CASE WHEN PREF_GRADE.THIS_YEAR_FLG = '1' ";
        $query .= "                    THEN '".$curriculumYear."' ";
        $query .= "                    ELSE '".($curriculumYear - 1)."' ";
        $query .= "               END AND ";
        if ($rankFlg1 == "2") {
            $query .= "     {$income1} BETWEEN T1.INCOME_LOW1 AND ";
            $query .= "     T1.INCOME_HIGH1 ";
        }
        if (strlen($siblings) > 0) {
            $query .= "     AND {$siblings} = VALUE(T1.INCOME_SIBLINGS1, 0) ";
        } else {
            $query .= "     AND 0 = VALUE(T1.INCOME_SIBLINGS1, 0) ";
        }

        $query .= " ORDER BY ";
        $query .= "     REDUCTIONMONEY_1, ";
        $query .= "     REDUCTION_SEQ ";
        $query .= " FETCH FIRST 1 ROWS ONLY ";

        return $query;
    }

    //軽減額取得
    function selectQueryReductionMony2(&$model, $schregNo, $curriculumYear, $useCurriculumYear, $rankFlg2, $income2, $siblings) {
        $query  = " SELECT ";
        $query .= "     T1.PREFECTURESCD, ";
        $query .= "     T1.REDUCTION_SEQ, ";
        $query .= "     T1.REDUCTIONMONEY_1, ";
        $query .= "     T1.REDUCTIONMONEY_2, ";
        $query .= "     L1.NAME1 AS PREF ";
        $query .= " FROM ";
        $query .= "     REDUCTION_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'G202' ";
        $query .= "          AND T1.PREFECTURESCD = L1.NAMECD2 ";
        $query .= "     LEFT JOIN REDUCTION_PREF_GRADE_MST PREF_GRADE ON PREF_GRADE.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND T1.PREFECTURESCD = PREF_GRADE.PREFECTURESCD ";
        $query .= "          AND PREF_GRADE.GRADE = '{$model->search["GRADE"]}', ";
        $query .= "     (SELECT ";
        $query .= "         ST1.GRADE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ST1 ";
        $query .= "     WHERE ";
        $query .= "         ST1.YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "         ST1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "         ST1.SCHREGNO = '{$schregNo}' ";
        $query .= "     ) T3, ";
        $query .= "     (SELECT ";
        $query .= "         MAX(ST1.GRADE) AS GRADE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ST1 ";
        $query .= "     WHERE ";
        $query .= "         ST1.YEAR    = '{$model->mstLastYear}' AND ";
        $query .= "         ST1.SCHREGNO = '{$schregNo}' ";
        $query .= "     ) T4 ";
        $query .= " WHERE ";
        $query .= "     T1.PREFECTURESCD = (SELECT ";
        $query .= "                             SUBSTR(MIN(ST1.CITYCD),1,2) ";
        $query .= "                         FROM ";
        $query .= "                             ZIPCD_MST ST1, ";
        $query .= "                             GUARDIAN_DAT ST2 ";
        $query .= "                         WHERE ";
        $query .= "                             ST1.NEW_ZIPCD = ST2.GUARANTOR_ZIPCD AND ";
        $query .= "                             ST2.SCHREGNO = '{$schregNo}' ";
        $query .= "                         ) AND ";

        if ($useCurriculumYear) {
            $query .= "     T1.GRADE = '01' AND ";
        } else {
            $query .= "     T1.GRADE = T3.GRADE AND ";
        }

        $query .= "     T1.YEAR = '".$curriculumYear."' AND ";
        if ($rankFlg2 == "2") {
            $query .= "     {$income2} BETWEEN T1.INCOME_LOW2 AND ";
            $query .= "     T1.INCOME_HIGH2 ";
        }
        if (strlen($siblings) > 0) {
            $query .= "     AND {$siblings} = VALUE(T1.INCOME_SIBLINGS2, 0) ";
        } else {
            $query .= "     AND 0 = VALUE(T1.INCOME_SIBLINGS2, 0) ";
        }

        $query .= " ORDER BY ";
        $query .= "     REDUCTIONMONEY_1, ";
        $query .= "     REDUCTION_SEQ ";
        $query .= " FETCH FIRST 1 ROWS ONLY ";

        return $query;
    }
}
?>