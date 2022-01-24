<?php

require_once('for_php7.php');
class knjp171kQuery extends Query {
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
        $query .= "   T3.REDUCTION_SEQ_1, ";
        $query .= "   T3.REDUC_INCOME_1, ";
        $query .= "   L1.REDUCTIONMONEY AS BASE_MONEY_1, ";
        $query .= "   T3.REDUCTIONMONEY_1, ";
        $query .= "   T3.REDUC_DEC_FLG_1, ";
        $query .= "   T3.REDUCTION_ADD_MONEY_1, ";
        $query .= "   T3.REDUC_ADD_FLG_1, ";
        $query .= "   T3.REDUCTION_SEQ_2, ";
        $query .= "   T3.REDUC_INCOME_2, ";
        $query .= "   L2.REDUCTIONMONEY AS BASE_MONEY_2, ";
        $query .= "   T3.REDUCTIONMONEY_2, ";
        $query .= "   T3.REDUC_DEC_FLG_2, ";
        $query .= "   T3.REDUCTION_ADD_MONEY_2, ";
        $query .= "   T3.REDUC_ADD_FLG_2, ";
        $query .= "   CASE WHEN T3.REDUC_REMARK IS NOT NULL ";
        $query .= "        THEN T3.REDUC_REMARK ";
        $query .= "        ELSE SUB_REMARK.REDUC_REMARK ";
        $query .= "   END AS REDUC_REMARK, ";
        $query .= "   T3.TOTAL_MONEY, ";
        $query .= "   T4.GRANTCD, ";
        $query .= "   value(T6.COUNTTRANSFER,0) + value(G6.COUNTTRANSFER,0) + value(E6.COUNTTRANSFER,0) AS COUNTTRANSFER, ";
        $query .= "   L4.BEGIN_YEARMONTH, ";
        $query .= "   L4.REMAIN_SUP_LIMIT_MONTH, ";
        $query .= "   L3.CNT, ";
        $query .= "   L3.ZENKI_CNT, ";
        $query .= "   L3.KOUKI_CNT ";
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
        $query .= "     REGD_T ST2 LEFT OUTER JOIN SCHREG_REGD_HDAT ST3  ";
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
        $query .= "                            ST1.OFFSET_FLG, ";
        $query .= "                            ST1.REDUCTION_SEQ_1, ";
        $query .= "                            ST1.REDUC_INCOME_1, ";
        $query .= "                            ST1.REDUCTIONMONEY_1, ";
        $query .= "                            ST1.REDUC_DEC_FLG_1, ";
        $query .= "                            ST1.REDUCTION_ADD_MONEY_1, ";
        $query .= "                            ST1.REDUC_ADD_FLG_1, ";
        $query .= "                            ST1.REDUCTION_SEQ_2, ";
        $query .= "                            ST1.REDUC_INCOME_2, ";
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
        $query .= "       LEFT JOIN REDUCTION_DAT SUB_REMARK ON T1.YEAR = SUB_REMARK.YEAR ";
        $query .= "            AND T1.SCHREGNO = SUB_REMARK.SCHREGNO ";
        $query .= "       LEFT OUTER JOIN (SELECT MAX(GRANTCD) AS GRANTCD, ";
        $query .= "                            YEAR, ";
        $query .= "                            SCHREGNO  ";
        $query .= "                        FROM  ";
        $query .= "                            SCHREG_GRANT_DAT ST1  ";
        $query .= "                        GROUP BY ";
        $query .= "                            YEAR, ";
        $query .= "                            SCHREGNO ";
        $query .= "                        ) T4 ON  T1.YEAR = T4.YEAR  ";
        $query .= "                             AND T1.SCHREGNO = T4.SCHREGNO ";
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
        $query .= "       LEFT JOIN REDUCTION_COUNTRY_MST L1 ON L1.YEAR = '".(CTRL_YEAR - 1)."' ";
        $query .= "            AND T1.GRADE = L1.GRADE ";
        $query .= "            AND VALUE(T3.REDUC_INCOME_1, 0) BETWEEN L1.INCOME_LOW AND L1.INCOME_HIGH ";
        $query .= "       LEFT JOIN REDUCTION_COUNTRY_MST L2 ON L2.YEAR = '".CTRL_YEAR."' ";
        $query .= "            AND T1.GRADE = L2.GRADE ";
        $query .= "            AND VALUE(T3.REDUC_INCOME_1, 0) BETWEEN L2.INCOME_LOW AND L2.INCOME_HIGH ";
        $query .= "       LEFT JOIN (SELECT ";
        $query .= "                    SCHREGNO, ";
        $query .= "                    SUM(CASE WHEN PLAN_MONEY IS NOT NULL ";
        $query .= "                             THEN 1 ";
        $query .= "                             ELSE 0 ";
        $query .= "                        END ";
        $query .= "                    ) AS CNT, ";
        $query .= "                    SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH < '".CTRL_YEAR."07' AND ADD_PLAN_MONEY IS NOT NULL ";
        $query .= "                             THEN 1 ";
        $query .= "                             ELSE 0 ";
        $query .= "                        END ";
        $query .= "                    ) AS ZENKI_CNT, ";
        $query .= "                    SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH >= '".CTRL_YEAR."07' AND ADD_PLAN_MONEY IS NOT NULL ";
        $query .= "                             THEN 1 ";
        $query .= "                             ELSE 0 ";
        $query .= "                        END ";
        $query .= "                    ) AS KOUKI_CNT ";
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
        $query  = " SELECT ";
        $query .= "     T1.REDUCTION_SEQ, ";
        $query .= "     T1.REDUCTION_ADD_MONEY ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_MST T1, ";
        $query .= "     (SELECT ";
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

        if ($model->income["OBJ_NAME"] == 'REDUC_INCOME_1[]') {
            $query .= "     AND T1.YEAR = '".(CTRL_YEAR - 1)."' ";
            $query .= "     AND ".$model->income["REDUC_INCOME_1"]." BETWEEN T1.INCOME_LOW AND T1.INCOME_HIGH ";
        } else {
            $query .= "     AND T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND ".$model->income["REDUC_INCOME_2"]." BETWEEN T1.INCOME_LOW AND T1.INCOME_HIGH ";
        }

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
        $query .= "   SUM(CASE WHEN PLAN_MONEY IS NOT NULL ";
        $query .= "            THEN 1 ";
        $query .= "            ELSE 0 ";
        $query .= "       END ";
        $query .= "   ) AS CNT, ";
        $query .= "   SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH < '".CTRL_YEAR."07' AND ADD_PLAN_MONEY IS NOT NULL ";
        $query .= "            THEN 1 ";
        $query .= "            ELSE 0 ";
        $query .= "       END ";
        $query .= "   ) AS ZENKI_CNT, ";
        $query .= "   SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH >= '".CTRL_YEAR."07' AND ADD_PLAN_MONEY IS NOT NULL ";
        $query .= "            THEN 1 ";
        $query .= "            ELSE 0 ";
        $query .= "       END ";
        $query .= "   ) AS KOUKI_CNT ";
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

        $zenkiMonth = array("04", "05", "06");
        $koukiMonth = array("07", "08", "09", "10", "11", "12", "01", "02", "03");
        for($i = 0; $i < get_count($model->schregno); $i++) {

            //計画データの有無
            $planCnt = $db->getRow(knjp171kQuery::getPlanCnt($model->schregno[$i]), DB_FETCHMODE_ASSOC);

            /*************************/
            /* REDUCTION_COUNTRY_DAT */
            /*************************/
            if (is_array($model->field["REDUC_DEC_FLG_1"])) {
                $reduc_dec_flg_1 = in_array($model->schregno[$i], $model->field["REDUC_DEC_FLG_1"])? 1 : NULL;
            } else {
                $reduc_dec_flg_1 = NULL;
            }
            $reduc_dec_flg_2 = NULL;
            if (is_array($model->field["OFFSET_FLG"])) {
                $offSetFlg = in_array($model->schregno[$i], $model->field["OFFSET_FLG"])? 1 : NULL;
            } else {
                $offSetFlg = NULL;
            }
            if (is_array($model->field["REDUC_ADD_FLG_1"])) {
                $addFlg1 = in_array($model->schregno[$i], $model->field["REDUC_ADD_FLG_1"])? 1 : NULL;
            } else {
                $addFlg1 = NULL;
            }
            if (is_array($model->field["REDUC_ADD_FLG_2"])) {
                $addFlg2 = in_array($model->schregno[$i], $model->field["REDUC_ADD_FLG_2"])? 1 : NULL;
            } else {
                $addFlg2 = NULL;
            }
            $data = array();
            $data["GRADE"][TEXT]                    = $model->search["GRADE"];
            $data["OFFSET_FLG"][TEXT]               = $offSetFlg;
            $data["REDUCTION_SEQ_1"][NUMBER]        = $model->field["REDUCTION_SEQ_1"][$i] != "" ? $model->field["REDUCTION_SEQ_1"][$i] : NULL;
            $data["REDUCTIONMONEY_1"][NUMBER]       = $model->field["BASE_MONEY_1"][$i]   ? $model->field["BASE_MONEY_1"][$i]   : NULL;
            if ($planCnt["CNT"] == 0) {
                $data["REDUC_DEC_FLG_1"][TEXT]          = $reduc_dec_flg_1;
            }
            $data["REDUCTION_ADD_MONEY_1"][NUMBER]  = strlen($model->field["REDUCTION_ADD_MONEY_1"][$i]) > 0 ? $model->field["REDUCTION_ADD_MONEY_1"][$i]   : NULL;
            if ($planCnt["ZENKI_CNT"] == 0) {
                $data["REDUC_ADD_FLG_1"][TEXT]          = $addFlg1;
            }
            $data["REDUC_INCOME_1"][NUMBER]         = strlen($model->field["REDUC_INCOME_1"][$i]) > 0 ? $model->field["REDUC_INCOME_1"][$i]   : NULL;
            $data["REDUCTION_SEQ_2"][NUMBER]        = $model->field["REDUCTION_SEQ_2"][$i] != "" ? $model->field["REDUCTION_SEQ_2"][$i] : NULL;
            $data["REDUCTIONMONEY_2"][NUMBER]       = NULL;
            $data["REDUC_DEC_FLG_2"][TEXT]          = $reduc_dec_flg_2;
            $data["REDUCTION_ADD_MONEY_2"][NUMBER]  = strlen($model->field["REDUCTION_ADD_MONEY_2"][$i]) > 0 ? $model->field["REDUCTION_ADD_MONEY_2"][$i] : NULL;
            if ($planCnt["KOUKI_CNT"] == 0) {
                $data["REDUC_ADD_FLG_2"][TEXT]          = $addFlg2;
            }
            $data["REDUC_INCOME_2"][NUMBER]         = strlen($model->field["REDUC_INCOME_2"][$i]) > 0 ? $model->field["REDUC_INCOME_2"][$i]   : NULL;
            $data["REDUC_REMARK"][TEXT]             = $model->field["REDUC_REMARK"][$i]   ? $model->field["REDUC_REMARK"][$i]   : NULL;
            $data["REGISTERCD"][TEXT]               = STAFFCD;
            $data["UPDATED"][FUNC]                  = "SYSDATE()";
            
            $query  = " SELECT * FROM";
            $query .= "     REDUCTION_COUNTRY_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '".CTRL_YEAR ."' AND ";
            $query .= "     SCHREGNO    = '{$model->schregno[$i]}' ";
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            if (is_array($row)) {
                /**********/
                /* UPDATE */
                /**********/

                $where  = " WHERE ";
                $where .= "     YEAR        = '".CTRL_YEAR ."' AND ";
                $where .= "     SCHREGNO    = '{$model->schregno[$i]}' ";

                $query = Query::updateSQL($data, "REDUCTION_COUNTRY_DAT", $where);
                $result = $db->query($query);
            } else {

                /**********/
                /* INSERT */
                /**********/
                $data["YEAR"][TEXT]     = CTRL_YEAR;
                $data["SCHREGNO"][TEXT] = $model->schregno[$i];

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
            $query .= "     AND SCHREGNO    = '{$model->schregno[$i]}' ";

            $result = $db->query($query);

            /******************************/
            /* REDUCTION_COUNTRY_PLAN_DAT */
            /******************************/
            //メインデータ取得
            $reducDat = $db->getRow(knjp171kQuery::getReducMoney($model->schregno[$i]), DB_FETCHMODE_ASSOC);
            $zenkiData = array("DEC" => $reducDat["REDUC_DEC_FLG_1"] == "1" ? $reducDat["REDUCTIONMONEY_1"] : NULL,
                               "ADD" => $reducDat["REDUC_ADD_FLG_1"] == "1" ? $reducDat["REDUCTION_ADD_MONEY_1"] : NULL);
            $koukiData = array("DEC" => $reducDat["REDUC_DEC_FLG_1"] == "1" ? $reducDat["REDUCTIONMONEY_1"] : NULL,
                               "ADD" => $reducDat["REDUC_ADD_FLG_2"] == "1" ? $reducDat["REDUCTION_ADD_MONEY_2"] : NULL);

            if ($planCnt["ALLCNT"] == 0) {
                foreach ($zenkiMonth as $key => $val) {
                    knjp171kQuery::insertPlan($db, $model, $reducDat, $val, $zenkiData);
                }
                foreach ($koukiMonth as $key => $val) {
                    knjp171kQuery::insertPlan($db, $model, $reducDat, $val, $koukiData);
                }
            } else {
                if ($planCnt["CNT"] == "0" || $planCnt["ZENKI_CNT"] == "0") {
                    foreach ($zenkiMonth as $key => $val) {
                        knjp171kQuery::updatePlan($db, $model, $reducDat, $val, $zenkiData, $planCnt, "ZENKI");
                    }
                }
                if ($planCnt["CNT"] == "0" || $planCnt["KOUKI_CNT"] == "0") {
                    foreach ($koukiMonth as $key => $val) {
                        knjp171kQuery::updatePlan($db, $model, $reducDat, $val, $koukiData, $planCnt, "KOUKI");
                    }
                }
            }

        }
        $db->commit();
        Query::dbCheckIn($db);
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

        if ($planCnt["CNT"] == "0") {
            $data["PLAN_MONEY"][NUMBER]     = $moneyData["DEC"];
        }
        if ($planCnt[$div."_CNT"] == "0") {
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