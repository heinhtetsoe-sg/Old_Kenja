<?php

require_once('for_php7.php');

class knje460n_kiso_zyouhouQuery extends Query {

    //更新年度コンボ
    function getYearCmb($model)
    {
        $query  = "  SELECT DISTINCT ";
        $query .= "      YEAR AS VALUE, ";
        $query .= "      YEAR AS LABEL ";
        $query .= "  FROM ";
        $query .= "      SCHREG_REGD_DAT ";
        $query .= "  WHERE ";
        $query .= "      SCHREGNO = '".$model->schregno."' ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //メインの取得
    function getMainQuery($model)
    {
        $query .= " WITH MXSCHREG AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     MAX(SEMESTER) AS SEMESTER, ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field2["YEAR"]."' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO ";
        $query .= " ), MXSCHADDR AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     MAX(T1.ISSUEDATE) AS ISSUEDATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_ADDRESS_DAT T1 ";
        $query .= "     LEFT JOIN (SELECT SCHREGNO, MAX(GRADE) AS GRADE FROM SCHREG_REGD_DAT WHERE YEAR = '".$model->field2["YEAR"]."' GROUP BY SCHREGNO) T3 ";
        $query .= "       ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = '".$model->field2["YEAR"]."' ";
        $query .= "      AND GDAT.GRADE = T3.GRADE ";
        $query .= "     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "      AND T2.SCHOOL_KIND = GDAT.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND (T1.EXPIREDATE IS NULL OR T2.ENT_DATE < T1.EXPIREDATE) AND T1.ISSUEDATE <= '".CTRL_DATE."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   T2WK.YEAR, ";
        $query .= "   TA1.STATUS AS ZYOUHOU1_1, ";
        $query .= "   TA1.STATUS2 AS ZYOUHOU1_2, ";
        $query .= "   TA1.STATUS3 AS ZYOUHOU1_3, ";
        $query .= "   TA2.STATUS AS ZYOUHOU2_1, ";
        $query .= "   TA2.STATUS2 AS ZYOUHOU2_2, ";
        $query .= "   TA2.STATUS3 AS ZYOUHOU2_3, ";
        $query .= "   TA3.STATUS AS ZYOUHOU3_1, ";
        $query .= "   TA3.STATUS2 AS ZYOUHOU3_2, ";
        $query .= "   TA3.STATUS3 AS ZYOUHOU3_3, ";
        $query .= "   TA4.STATUS AS ZYOUHOU4_1, ";
        $query .= "   TA4.STATUS2 AS ZYOUHOU4_2, ";
        $query .= "   TA4.STATUS3 AS ZYOUHOU4_3, ";
        $query .= "   TA5.STATUS AS ZYOUHOU5_1, ";
        $query .= "   TA5.STATUS2 AS ZYOUHOU5_2, ";
        $query .= "   TA5.STATUS3 AS ZYOUHOU5_3, ";
        $query .= "   TA6.STATUS AS ZYOUHOU6_1, ";
        $query .= "   TA6.STATUS2 AS ZYOUHOU6_2, ";
        $query .= "   TA6.STATUS3 AS ZYOUHOU6_3, ";
        $query .= "   TB1.RECORD AS RENKEI_ZYOUHOU ";
        $query .= " FROM ";
        $query .= "   MXSCHREG T2WK ";
        $query .= "   INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T2WK.YEAR AND T2.SEMESTER = T2WK.SEMESTER AND T2.SCHREGNO = T2WK.SCHREGNO ";
        $query .= "   LEFT JOIN SCHREG_CHALLENGED_SUPPORTPLAN_STATUS_DAT TA1 ";
        $query .= "     ON TA1.YEAR = T2WK.YEAR ";
        $query .= "    AND TA1.SCHREGNO = T2WK.SCHREGNO ";
        $query .= "    AND TA1.RECORD_DATE = T2WK.YEAR ";
        $query .= "    AND TA1.DATA_DIV = '01' ";
        $query .= "   LEFT JOIN SCHREG_CHALLENGED_SUPPORTPLAN_STATUS_DAT TA2 ";
        $query .= "     ON TA2.YEAR = T2WK.YEAR ";
        $query .= "    AND TA2.SCHREGNO = T2WK.SCHREGNO ";
        $query .= "    AND TA2.RECORD_DATE = T2WK.YEAR ";
        $query .= "    AND TA2.DATA_DIV = '02' ";
        $query .= "   LEFT JOIN SCHREG_CHALLENGED_SUPPORTPLAN_STATUS_DAT TA3 ";
        $query .= "     ON TA3.YEAR = T2WK.YEAR ";
        $query .= "    AND TA3.SCHREGNO = T2WK.SCHREGNO ";
        $query .= "    AND TA3.RECORD_DATE = T2WK.YEAR ";
        $query .= "    AND TA3.DATA_DIV = '03' ";
        $query .= "   LEFT JOIN SCHREG_CHALLENGED_SUPPORTPLAN_STATUS_DAT TA4 ";
        $query .= "     ON TA4.YEAR = T2WK.YEAR ";
        $query .= "    AND TA4.SCHREGNO = T2WK.SCHREGNO ";
        $query .= "    AND TA4.RECORD_DATE = T2WK.YEAR ";
        $query .= "    AND TA4.DATA_DIV = '04' ";
        $query .= "   LEFT JOIN SCHREG_CHALLENGED_SUPPORTPLAN_STATUS_DAT TA5 ";
        $query .= "     ON TA5.YEAR = T2WK.YEAR ";
        $query .= "    AND TA5.SCHREGNO = T2WK.SCHREGNO ";
        $query .= "    AND TA5.RECORD_DATE = T2WK.YEAR ";
        $query .= "    AND TA5.DATA_DIV = '05' ";
        $query .= "   LEFT JOIN SCHREG_CHALLENGED_SUPPORTPLAN_STATUS_DAT TA6 ";
        $query .= "     ON TA6.YEAR = T2WK.YEAR ";
        $query .= "    AND TA6.SCHREGNO = T2WK.SCHREGNO ";
        $query .= "    AND TA6.RECORD_DATE = T2WK.YEAR ";
        $query .= "    AND TA6.DATA_DIV = '06' ";
        $query .= "   LEFT JOIN SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT TB1 ";
        $query .= "     ON TB1.YEAR = T2WK.YEAR ";
        $query .= "    AND TB1.SCHREGNO = T2WK.SCHREGNO ";
        $query .= "    AND TB1.RECORD_DATE = T2WK.YEAR ";
        $query .= " WHERE ";
        $query .= "    T2WK.YEAR = '".$model->field2["YEAR"]."' ";
        $query .= "    AND T2WK.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //SCHREG_CHALLENGED_SUPPORTPLAN_DATを取得
    function getSchregChallengedSupportplanDat($db, $model, $div, $seq, $field)
    {
        $query  = " SELECT ";
        if($field == "COUNT"){
            $query .= "     COUNT(*) AS CNT ";
        } else  {
            $query .= "     ".$field." ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_SUPPORTPLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field2["YEAR"]."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND SPRT_DIV = '".$div."' ";
        $query .= "     AND SPRT_SEQ = '".$seq."' ";
        $getDate = $db->getOne($query);
        return $getDate;
    }

    //記入者名を取得
    function getStaffName($model)
    {
        $query  = " SELECT ";
        $query .= "   T2.REMARK AS STAFFNAME ";
        $query .= " FROM ";
        $query .= "   SCHREG_CHALLENGED_SUPPORTPLAN_DAT T2 ";
        $query .= " WHERE ";
        $query .= "   T2.YEAR     = '".$model->field2["YEAR"]."' ";
        $query .= "   AND T2.SCHREGNO = '".$model->schregno."' ";
        $query .= "   AND T2.SPRT_DIV = '01' ";
        $query .= "   AND T2.SPRT_SEQ = '03' ";

        return $query;
    }

    //SCHREG_CHALLENGED_SUPPORTPLAN_STATUS_DATを取得
    function getSchregChallengedSupportplanStatusDat($db, $model, $dDiv)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_SUPPORTPLAN_STATUS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field2["YEAR"]."' ";
        $query .= "     AND SCHREGNO    = '".$model->schregno."' ";
        $query .= "     AND RECORD_DATE = '".$model->field2["YEAR"]."' ";
        $query .= "     AND DATA_DIV = '".$dDiv."' ";
        $getDate = $db->getOne($query);
        return $getDate;
    }

    //SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DATを取得
    function getSchregChallengedSupportplanMainDat($db, $model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field2["YEAR"]."' ";
        $query .= "     AND SCHREGNO    = '".$model->schregno."' ";
        $query .= "     AND RECORD_DATE = '".$model->field2["YEAR"]."' ";
        $getDate = $db->getOne($query);
        return $getDate;
    }


/*************** 更新処理 ***************/

    //UPDATE (SCHREG_CHALLENGED_SUPPORTPLAN_STATUS_DAT)
    function getUpdateStatusDat($db, $model)
    {
        //データ有無チェック
        $db->autoCommit(false);
        $data = array();
        $data["YEAR"][TEXT]         = $model->field2["YEAR"];
        $data["SCHREGNO"][TEXT]     = $model->schregno;
        $data["RECORD_DATE"][TEXT]  = $model->field2["YEAR"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        for ($cntidx = 1;$cntidx <= 6;$cntidx++) {
            if ($cntidx == 1) {
                $data["DATA_DIV"][TEXT] = "01";
                $data["STATUS"][TEXT] = $model->field2["ZYOUHOU1_1"];
                $data["STATUS2"][TEXT] = $model->field2["ZYOUHOU1_2"];
                $data["STATUS3"][TEXT] = $model->field2["ZYOUHOU1_3"];
            } else if ($cntidx == 2) {
                $data["DATA_DIV"][TEXT] = "02";
                $data["STATUS"][TEXT] = $model->field2["ZYOUHOU2_1"];
                $data["STATUS2"][TEXT] = $model->field2["ZYOUHOU2_2"];
                $data["STATUS3"][TEXT] = $model->field2["ZYOUHOU2_3"];
            } else if ($cntidx == 3) {
                $data["DATA_DIV"][TEXT] = "03";
                $data["STATUS"][TEXT] = $model->field2["ZYOUHOU3_1"];
                $data["STATUS2"][TEXT] = $model->field2["ZYOUHOU3_2"];
                $data["STATUS3"][TEXT] = $model->field2["ZYOUHOU3_3"];
            } else if ($cntidx == 4) {
                $data["DATA_DIV"][TEXT] = "04";
                $data["STATUS"][TEXT] = $model->field2["ZYOUHOU4_1"];
                $data["STATUS2"][TEXT] = $model->field2["ZYOUHOU4_2"];
                $data["STATUS3"][TEXT] = $model->field2["ZYOUHOU4_3"];
            } else if ($cntidx == 5) {
                $data["DATA_DIV"][TEXT] = "05";
                $data["STATUS"][TEXT] = $model->field2["ZYOUHOU5_1"];
                $data["STATUS2"][TEXT] = $model->field2["ZYOUHOU5_2"];
                $data["STATUS3"][TEXT] = $model->field2["ZYOUHOU5_3"];
            } else if ($cntidx == 6) {
                $data["DATA_DIV"][TEXT] = "06";
                $data["STATUS"][TEXT] = $model->field2["ZYOUHOU6_1"];
                $data["STATUS2"][TEXT] = $model->field2["ZYOUHOU6_2"];
                $data["STATUS3"][TEXT] = $model->field2["ZYOUHOU6_3"];
            }

            $getCount = knje460n_kiso_zyouhouQuery::getSchregChallengedSupportplanStatusDat($db, $model, $data["DATA_DIV"][TEXT]);
            //更新処理
            if ($getCount > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR = '".$model->field2["YEAR"]."' ";
                $where .= "     AND SCHREGNO = '".$model->schregno."' ";
                $where .= "     AND RECORD_DATE = '".$model->field2["YEAR"]."' ";
                $where .= "     AND DATA_DIV = '".$data["DATA_DIV"][TEXT]."' ";
                $db->query(Query::updateSQL($data, "SCHREG_CHALLENGED_SUPPORTPLAN_STATUS_DAT", $where));
            } else {
                $db->query(Query::insertSQL($data, "SCHREG_CHALLENGED_SUPPORTPLAN_STATUS_DAT"));
            }
        }
        $db->commit();
        $db->autoCommit(true);
        return;
    }

    //UPDATE (SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT)
    function getUpdateMainDat($db, $model)
    {
        //データ有無チェック
        $getCount = knje460n_kiso_zyouhouQuery::getSchregChallengedSupportplanMainDat($db, $model);

        $data = array();
        $data["YEAR"][TEXT]         = $model->field2["YEAR"];
        $data["SCHREGNO"][TEXT]     = $model->schregno;
        $data["RECORD_DATE"][TEXT]  = $model->field2["YEAR"];
        $data["WRITING_DATE"][DATE] = CTRL_DATE;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";
        
        $data["RECORD"][TEXT] = $model->field2["RENKEI_ZYOUHOU"];
        //更新処理
        if ($getCount > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR = '".$model->field2["YEAR"]."' ";
            $where .= "     AND SCHREGNO = '".$model->schregno."' ";
            $where .= "     AND RECORD_DATE = '".$model->field2["YEAR"]."' ";
            $db->query(Query::updateSQL($data, "SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT", $where));
        } else {
            $db->query(Query::insertSQL($data, "SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT"));
        }

        return;
    }

    //UPDATE (SCHREG_CHALLENGED_SUPPORTPLAN_DAT)
    function getUpdateMainQuery($db, $model, $div, $seq, $remakr)
    {
        //データ有無チェック
        $getCount = knje460n_kiso_zyouhouQuery::getSchregChallengedSupportplanDat($db, $model, $div, $seq, "COUNT");

        $data = array();
        $data["YEAR"][TEXT]         = $model->field2["YEAR"];
        $data["SCHREGNO"][TEXT]     = $model->schregno;
        $data["SPRT_DIV"][TEXT]     = $div;
        $data["SPRT_SEQ"][TEXT]     = $seq;
        $data["REMARK"][TEXT]       = $remakr;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        //更新処理
        if ($getCount > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR = '".$model->field2["YEAR"]."' ";
            $where .= "     AND SCHREGNO = '".$model->schregno."' ";
            $where .= "     AND SPRT_DIV = '".$div."' ";
            $where .= "     AND SPRT_SEQ = '".$seq."' ";
            $db->query(Query::updateSQL($data, "SCHREG_CHALLENGED_SUPPORTPLAN_DAT", $where));
        } else {
            $db->query(Query::insertSQL($data, "SCHREG_CHALLENGED_SUPPORTPLAN_DAT"));
        }

        return;
    }

}
?>
