<?php

require_once('for_php7.php');

class knjp172kQuery extends Query {

    //学年
    function getYear($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";

        return $query;
    }

    //異動情報
    function getTransferInfo($model) {
        //変数
        $sdate =  $model->year      ."-04-01";
        $edate = ($model->year + 1) ."-03-31";
        //SQL
        $query  = "";
        $query .= " WITH T_TRANSFER AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         TRANSFERCD, ";
        $query .= "         MAX(TRANSFER_SDATE) AS TRANSFER_SDATE, ";
        $query .= "         COUNT(*) AS CNT ";
        $query .= "     FROM ";
        $query .= "         SCHREG_TRANSFER_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '{$model->schregno}' ";
        $query .= "         AND TRANSFERCD IN ('1','2') "; //1:留学、2:休学
        $query .= "         AND ((TRANSFER_SDATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}')) ";
        $query .= "          OR  (TRANSFER_EDATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}'))) ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         TRANSFERCD ";
        $query .= "     ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.TRANSFERCD AS DIV, ";
        $query .= "     L1.NAME1 AS DIV_NAME,  ";
        $query .= "     '2' AS SORT, ";
        $query .= "     T1.TRANSFER_SDATE AS SDATE, ";
        $query .= "     T1.TRANSFER_EDATE AS EDATE, ";
        $query .= "     T2.CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_TRANSFER_DAT T1 ";
        $query .= "     INNER JOIN T_TRANSFER T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                             AND T2.TRANSFERCD = T1.TRANSFERCD ";
        $query .= "                             AND T2.TRANSFER_SDATE = T1.TRANSFER_SDATE ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'A004' ";
        $query .= "                          AND L1.NAMECD2 = T1.TRANSFERCD ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRD_DIV AS DIV, ";
        $query .= "     L1.NAME1 AS DIV_NAME, ";
        $query .= "     '1' AS SORT, ";
        $query .= "     T1.GRD_DATE AS SDATE, ";
        $query .= "     CAST(NULL AS DATE) AS EDATE, ";
        $query .= "     1 AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'A003' ";
        $query .= "                          AND L1.NAMECD2 = T1.GRD_DIV ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND T1.GRD_DIV IN ('2','3') "; //2:退学、3:転学
        $query .= "     AND T1.GRD_DATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}') ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ENT_DIV AS DIV, ";
        $query .= "     L1.NAME1 AS DIV_NAME, ";
        $query .= "     '3' AS SORT, ";
        $query .= "     T1.ENT_DATE AS SDATE, ";
        $query .= "     CAST(NULL AS DATE) AS EDATE, ";
        $query .= "     1 AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'A002' ";
        $query .= "                          AND L1.NAMECD2 = T1.ENT_DIV ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND T1.ENT_DIV IN ('4','5') "; //4:転入学、5:編入学
        $query .= "     AND T1.ENT_DATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}') ";
        $query .= " ORDER BY ";
        $query .= "     SORT, ";
        $query .= "     SDATE DESC ";

        return $query;
    }

    //授業料予定額
    function getSelectMoneyDue($model)
    {
        $query  = " SELECT ";
        $query .= "     CASE EXPENSE_M_CD WHEN '11' THEN '4' WHEN '12' THEN '9' ELSE '12' END AS SET_MONTH, ";
        $query .= "     MONEY_DUE AS SET_MONEY ";
        $query .= " FROM ";
        $query .= "     MONEY_DUE_M_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND EXPENSE_M_CD IN ('11', '12', '13') ";

        return $query;
    }

    //授業料予定額
    function getSelectMoneyPaid($model)
    {
        $query  = " SELECT ";
        $query .= "     MONTH(PAID_MONEY_DATE) AS SET_MONTH, ";
        $query .= "     SUM(VALUE(PAID_MONEY, 0)) AS SET_MONEY ";
        $query .= " FROM ";
        $query .= "     MONEY_PAID_M_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND EXPENSE_M_CD IN ('11', '12', '13') ";
        $query .= " GROUP BY ";
        $query .= "     MONTH(PAID_MONEY_DATE) ";

        return $query;
    }

    //授業料予定額
    function getSelectCountry($model, $div)
    {
        $query  = " SELECT ";
        $query .= "     T1.PLAN_MONTH AS SET_MONTH, ";
        $query .= "     T1.PLAN_CANCEL_FLG, ";
        $query .= "     T1.ADD_PLAN_CANCEL_FLG, ";
        $query .= "     T1.PLAN_LOCK_FLG, ";
        $query .= "     T1.ADD_PLAN_LOCK_FLG, ";
        if ($div == "DUE") {
            $query .= "     T1.PLAN_MONEY AS SET_MONEY, ";
            $query .= "     T1.PAID_YEARMONTH AS PAID_YEARMONTH, ";
            $query .= "     L1.REDUC_REMARK ";
        } else if ($div == "DUE_PAY") {
            $query .= "     T1.PAID_MONEY AS SET_MONEY, ";
            $query .= "     T1.PAID_YEARMONTH AS PAID_YEARMONTH ";
        } else if ($div == "ADD") {
            $query .= "     T1.ADD_PLAN_MONEY AS SET_MONEY, ";
            $query .= "     T1.ADD_PAID_YEARMONTH AS PAID_YEARMONTH ";
        } else {
            $query .= "     T1.ADD_PAID_MONEY AS SET_MONEY, ";
            $query .= "     T1.ADD_PAID_YEARMONTH AS PAID_YEARMONTH ";
        }
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT T1 ";
        $query .= "     LEFT JOIN REDUCTION_COUNTRY_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "     AND T1.SCHREGNO = L1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= "     AND T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //軽減額
    function getReductionDat($model)
    {
        $query .= " SELECT ";
        $query .= "     CASE WHEN T1.REDUC_DEC_FLG_1 = '1' OR T1.REDUC_DEC_FLG_2 = '1' ";
        $query .= "          THEN CASE WHEN T1.REDUC_DEC_FLG_1 = '1' ";
        $query .= "                    THEN VALUE(T1.REDUCTIONMONEY_1, 0) ";
        $query .= "                    ELSE 0 ";
        $query .= "               END ";
        $query .= "               + ";
        $query .= "               CASE WHEN T1.REDUC_DEC_FLG_2 = '1' ";
        $query .= "                    THEN VALUE(T1.REDUCTIONMONEY_2, 0) ";
        $query .= "                    ELSE 0 ";
        $query .= "               END ";
        $query .= "          ELSE NULL ";
        $query .= "     END AS SET_MONEY, ";
        $query .= "     CASE WHEN T1.REDUC_DEC_FLG_1 = '1' ";
        $query .= "          THEN VALUE(T1.REDUCTIONMONEY_1, 0) ";
        $query .= "          ELSE NULL ";
        $query .= "     END AS SET_MONEY1, ";
        $query .= "     CASE WHEN T1.REDUC_DEC_FLG_2 = '1' ";
        $query .= "          THEN VALUE(T1.REDUCTIONMONEY_2, 0) ";
        $query .= "          ELSE NULL ";
        $query .= "     END AS SET_MONEY2, ";
        $query .= "     '12' AS SET_MONTH, ";
        $query .= "     L1.PREF_CD AS FUKEN_CD, ";
        $query .= "     L1.PREF_NAME AS FUKEN, ";
        $query .= "     T1.REDUC_REMARK ";
        $query .= " FROM ";
        $query .= "     REDUCTION_DAT T1 ";
        $query .= "     LEFT JOIN PREF_MST L1 ON T1.PREFECTURESCD = L1.PREF_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= "     AND T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //学籍基礎マスタより学籍番号と名前を取得
    function getSchregno_name($model, $schregno)
    {
        $query  = " SELECT ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.NAME, ";
        $query .= "   L2.HR_NAMEABBV, ";
        $query .= "   L1.ATTENDNO, ";
        $query .= "   L3.PASSNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT L1 ON L1.YEAR = '".$model->year."' ";
        $query .= "          AND L1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "          AND T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT L2 ON L1.YEAR = L2.YEAR ";
        $query .= "       AND L1.SEMESTER = L2.SEMESTER ";
        $query .= "       AND L1.GRADE = L2.GRADE ";
        $query .= "       AND L1.HR_CLASS = L2.HR_CLASS ";
        $query .= "     LEFT JOIN REDUCTION_AUTHORIZE_DAT L3 ON L1.SCHREGNO = L3.SCHREGNO ";
        $query .= "          AND L3.DATA_DIV = '1' ";
        $query .= "          AND L3.DATA_DIV_SUB = '1' ";
        $query .= " WHERE ";
        $query .= "   T1.SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //１レコード取得
    function getTrainRow($model, $schregno)
    {
        $db = Query::dbCheckOut();
        $query  = "SELECT * FROM schreg_train_dat ";
        $query .= " WHERE year    = '".$model->year."'";
        $query .= " AND  schregno = '".$schregno. "'";
        
        $record = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $record[$row["SEMESTER"]] = array("caution"    => $row["CAUTION_GUIDANCE"],
                                              "admonition" => $row["ADMONITION_GUIDANCE"]);    
        }
        Query::dbCheckIn($db);
        return $record;
    }

}
?>
