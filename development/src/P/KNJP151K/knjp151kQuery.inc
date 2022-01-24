<?php

require_once('for_php7.php');

class knjp151kQuery extends Query {

    //名称マスタより取得
    function getNamecd($cd, $lavelNameFlg = false)
    {
        $query  = " SELECT ";
        if ($lavelNameFlg) {
            $query .= "     NAME1 AS LABEL, ";
        } else {
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        }
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '".$cd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //返金詳細
    function getRepaySeq($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     CAST(T1.REPAY_MONEY_DATE AS CHAR(10)) || ':' || rtrim(CAST(T1.REPAY_MONEY AS CHAR(10))) AS VALUE, ";
        $query .= "     CAST(T1.REPAY_MONEY_DATE AS CHAR(10)) || ':' || rtrim(CAST(T1.REPAY_MONEY AS CHAR(10))) AS LABEL, ";
        $query .= "     T1.REPAY_MONEY_DATE, ";
        $query .= "     T1.REPAY_MONEY ";
        $query .= " FROM ";
        $query .= "     MONEY_REPAY_S_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.EXPENSE_M_CD || T1.EXPENSE_S_CD = '".$model->field["TOTALCD"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.REPAY_MONEY_DATE, ";
        $query .= "     T1.REPAY_MONEY ";

        return $query;
    }

    //学年毎のクラスの取得
    function GetHrclass()
    {
        $query  = " SELECT ";
        $query .= "     GRADE || '-' || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一括更新画面の生徒一覧
    function GetStudent($model)
    {
        list($grade, $hr) = explode("-", $model->field["HRCLASS"]);
        list($rdate, $rmoney) = explode(":", $model->field["REPAY_SEQ"]);

        $query  = " SELECT T1.NAME_SHOW, ";
        $query .= "     T2.SCHREGNO,T2.ATTENDNO, ";
        $query .= "     T3.HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= "     RIGHT OUTER JOIN SCHREG_REGD_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T3 ON T2.YEAR     = T3.YEAR ";
        $query .= "           AND T2.SEMESTER = T3.SEMESTER ";
        $query .= "           AND T2.GRADE    = T3.GRADE ";
        $query .= "           AND T2.HR_CLASS = T3.HR_CLASS ";
        if ($model->field["SEX"] != 99){
            $query .= " WHERE ";
            $query .= "     T1.SEX = '".$model->field["SEX"]."'";
            $query .= "     AND T2.YEAR = '".CTRL_YEAR."'";
        } else {
            $query .= " WHERE ";
            $query .= "     T2.YEAR = '".CTRL_YEAR."'";
        }
        $query .= "     AND T2.SEMESTER = '".CTRL_SEMESTER."'";
        $query .= "     AND T2.GRADE    = '".$grade."'";
        $query .= "     AND T2.HR_CLASS = '".$hr."'";

        $query .= "     AND EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             MONEY_REPAY_S_DAT E1 ";
        $query .= "         WHERE ";
        $query .= "             E1.REPAY_MONEY_DATE = '".($rdate ? $rdate : "9999-12-31")."' ";
        $query .= "             AND E1.EXPENSE_M_CD || E1.EXPENSE_S_CD = '".$model->field["TOTALCD"]."' ";
        $query .= "             AND E1.REPAY_MONEY = ".($rmoney ? $rmoney : "0")." ";
        $query .= "             AND T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "         ) ";
        $query .= " ORDER BY ";
        $query .= "     ATTENDNO";

        return $query;
    }

    //対象費目の取得
    function getScd()
    {
        $query  = " SELECT ";
        $query .= "     T2.EXPENSE_M_CD || T1.EXPENSE_S_CD AS TOTALCD, ";
        $query .= "     T1.EXPENSE_S_NAME, ";
        $query .= "     T1.EXPENSE_S_MONEY, ";
        $query .= "     T2.BANK_TRANS_SDATE ";
        $query .= " FROM ";
        $query .= "     V_EXPENSE_S_MST T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     EXPENSE_M_MST T2 ";
        $query .= " ON ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.EXPENSE_M_CD = T2.EXPENSE_M_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."'";

        return $query;
    }

    //更新
    function &getUpdateRePayQuery($model, $schregno)
    {
        $data = array();
        $data["REPAY_FLG"][TEXT]    = $model->field["REPAY_FLG"] == "1" ? $model->field["REPAY_FLG"] : '0';
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        list($rdate, $rmoney) = explode(":", $model->field["REPAY_SEQ"]);
        $where  = " WHERE ";
        $where .= "     YEAR = '".CTRL_YEAR."' ";
        $where .= "     AND SCHREGNO = '".$schregno."' ";
        $where .= "     AND REPAY_MONEY_DATE = '".($rdate ? $rdate : "9999-12-31")."' ";
        $where .= "     AND EXPENSE_M_CD || EXPENSE_S_CD = '".$model->field["TOTALCD"]."' ";
        $where .= "     AND REPAY_MONEY = ".($rmoney ? $rmoney : "0")." ";

        $query = Query::updateSQL($data, "MONEY_REPAY_S_DAT", $where);

        return $query;
    }
}
?>
