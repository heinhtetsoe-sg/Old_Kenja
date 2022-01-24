<?php

require_once('for_php7.php');

class knjp900_approvalQuery extends Query {

    //本締めデータチェック
    function getCloseFlgData($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     LEVY_CLOSE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND YEAR = '{$model->year}' ";
        $query .= " AND CLOSE_FLG = '1' ";

        return $query;
    }

    //一覧取得
    function getRow($model) {
    
        $db = Query::dbCheckOut();
    
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= "     AND T1.YEAR        = '{$model->year}' ";
        $query .= "     AND T1.INCOME_L_CD || T1.INCOME_M_CD = '{$model->getIncomeLMcd}' ";
        $query .= "     AND T1.REQUEST_NO  = '{$model->getRequestNo}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.INCOME_L_CD, ";
        $query .= "     T1.INCOME_M_CD, ";
        $query .= "     T1.REQUEST_NO ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //指定の収入項目の収入額に対する支出の合計
    function getOutgoSumGk($model) {
        $query  = " SELECT ";
        $query .= "     SUM(VALUE(REQUEST_GK, 0)) AS SUM_REQUEST_GK ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND YEAR        = '{$model->year}' ";
        $query .= " AND INCOME_L_CD = '{$model->getIncomeLcd}' ";
        $query .= " AND OUTGO_CANCEL IS NULL ";
        return $query;
    }

    //決裁の状況を取得(比較時に利用)
    function getLevyData($model, $flg) {
        $query  = "";
        $query .= " SELECT ";
        if ($flg == "APPROVAL") {
            $query .= "     T1.INCOME_APPROVAL ";
        } else {
            $query .= "     T1.INCOME_CANCEL ";
        }
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= "     AND T1.YEAR        = '{$model->year}' ";
        $query .= "     AND T1.INCOME_L_CD = '{$model->getIncomeLcd}' ";
        $query .= "     AND T1.INCOME_L_CD || T1.INCOME_M_CD = '{$model->getIncomeLMcd}' ";
        $query .= "     AND T1.REQUEST_NO  = '{$model->getRequestNo}' ";
        return $query;
    }

    //収入科目
    function getLevyMDiv($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD || ':' || T1.LEVY_M_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_M_MST T1 ";
        $query .= "     INNER JOIN LEVY_L_MST L1 ON L1.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                             AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                             AND L1.YEAR        = T1.YEAR ";
        $query .= "                             AND L1.LEVY_L_CD   = T1.LEVY_L_CD ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= "     AND T1.YEAR        = '{$model->year}' ";
        $query .= "     AND T1.LEVY_L_CD || T1.LEVY_M_CD = '{$model->getIncomeLMcd}' ";
        return $query;
    }

    //収入金額
    function getRequestGk($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.REQUEST_GK ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= "     AND T1.YEAR        = '{$model->year}' ";
        $query .= "     AND T1.INCOME_L_CD || T1.INCOME_M_CD = '{$model->getIncomeLMcd}' ";
        $query .= "     AND T1.REQUEST_NO  = '{$model->getRequestNo}' ";

        return $query;
    }

    //収入金額
    function getIncomeNo($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.INCOME_NO ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= "     AND T1.YEAR        = '{$model->year}' ";
        $query .= "     AND T1.INCOME_L_CD || T1.INCOME_M_CD = '{$model->getIncomeLMcd}' ";
        $query .= "     AND T1.REQUEST_NO  = '{$model->getRequestNo}' ";

        return $query;
    }

    //受取人氏名
    function getIncomeStaff($model, $staff) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     STAFFCD || ':' || STAFFNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '{$staff}' ";
        return $query;
    }
    
    //UPDATE
    function &getUpdateQuery($db, $model) {
        $db->autoCommit(false);

        $incomelcd = substr($model->getIncomeLMcd, 0, 2);
        $incomemcd = substr($model->getIncomeLMcd, 2, 2);

        $data = array();

        if ($model->field["INCOME_APPROVAL"] === '1' && $model->field["INCOME_CANCEL"] == "") {
            $data["INCOME_NO"][TEXT]  = $model->getRequestNo;
        } else if ($model->field["INCOME_CANCEL"] === '1') {
            $data["INCOME_NO"][TEXT]  = "";
        }
        $data["INCOME_DATE"][DATE]              = str_replace("/","-",$model->field["INCOME_DATE"]);
        if ($model->field["INCOME_APPROVAL"] !== '1') {
            $data["INCOME_STAFF"][TEXT]             = NULL;
        } else if ($model->field["INCOME_APPROVAL"] === '1' && !$model->field["INCOME_STAFF"]) {
            $data["INCOME_STAFF"][TEXT]             = STAFFCD;
        }
        $data["INCOME_CERTIFICATE_CNT"][TEXT]   = $model->field["INCOME_CERTIFICATE_CNT"];

        $data["INCOME_APPROVAL"][TEXT]  = $model->field["INCOME_APPROVAL"];
        $data["INCOME_CANCEL"][TEXT]    = $model->field["INCOME_CANCEL"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        $where  = " WHERE ";
        $where .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $where .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
        $where .= " AND YEAR        = '{$model->year}' ";
        $where .= " AND INCOME_L_CD || INCOME_M_CD = '{$model->getIncomeLMcd}' ";
        $where .= " AND REQUEST_NO  = '{$model->getRequestNo}' ";
        $query = Query::updateSQL($data, "LEVY_REQUEST_INCOME_DAT", $where);
        $db->query($query);
        
        $db->commit();
        return;
    }

}
?>