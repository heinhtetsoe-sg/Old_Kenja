<?php

require_once('for_php7.php');


class knjp391kQuery extends Query {

    //中高判定用
    function GetJorH(){
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOLNAME1 LIKE '%近畿%中学%' ";

        return $query;
    }

    //クラス取得
    function GetClass()
    {

        $query  = "SELECT DISTINCT ";
        $query .= "    T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "    T1.HR_NAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT T1, ";
        $query .= "    SCHREG_REGD_DAT T2 ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.GRADE = '01' ";
        $query .= "    AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "    AND T1.GRADE || T1.HR_CLASS = T2.GRADE || T2.HR_CLASS ";
        $query .= "    AND T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   T1.TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR T1.TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR T1.TR_CD3 = '".STAFFCD."') ";
        }
        $query .= "ORDER BY ";
        $query .= "    T1.GRADE || T1.HR_CLASS ";

        return $query;
    }

    //個人取得
    function GetSchreg($model)
    {

        $query  = "SELECT DISTINCT ";
        $query .= "    T1.GRADE || ':' || T1.HR_CLASS || ':' || T1.ATTENDNO || ':' || T1.SCHREGNO AS VALUE, ";
        $query .= "    T3.HR_NAME || '　' || T1.ATTENDNO || '番　' || T2.NAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT T1 ";
        $query .= "    LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "    LEFT JOIN SCHREG_REGD_HDAT T3 ON T1.YEAR = T3.YEAR ";
        $query .= "         AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "         AND T1.GRADE || T1.HR_CLASS = T3.GRADE || T3.HR_CLASS ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "    AND T1.GRADE || T1.HR_CLASS = '".$model->hrclass."' ";
        $query .= "ORDER BY ";
        $query .= "    LABEL ";

        return $query;
    }

    //中分類支払い削除11
    function deletePai_M11_Query(){
        $query  = " DELETE FROM MONEY_PAID_M_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO IN (SELECT ";
        $query .= "                          SCHREGNO ";
        $query .= "                      FROM ";
        $query .= "                          FRESHMAN_DAT ";
        $query .= "                      WHERE ";
        $query .= "                          ENTERYEAR = '".CTRL_YEAR."') ";
        $query .= "     AND SCHREGNO NOT IN (SELECT ";
        $query .= "                              SCHREGNO ";
        $query .= "                          FROM ";
        $query .= "                              MONEY_DUE_M_DAT ";
        $query .= "                          WHERE ";
        $query .= "                              YEAR = '".CTRL_YEAR."' ";
        $query .= "                              AND EXPENSE_M_CD = '11' ";
        $query .= "                              AND VALUE(UN_AUTO_PAYFLG, '0') = '1') ";
        $query .= "     AND EXPENSE_L_CD = '01' ";
        $query .= "     AND EXPENSE_M_CD IN '11' ";
        return $query;
    }

    //中分類支払い削除21
    function deletePai_M21_Query(){
        $query  = " DELETE FROM MONEY_PAID_M_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO IN (SELECT ";
        $query .= "                          SCHREGNO ";
        $query .= "                      FROM ";
        $query .= "                          FRESHMAN_DAT ";
        $query .= "                      WHERE ";
        $query .= "                          ENTERYEAR = '".CTRL_YEAR."') ";
        $query .= "     AND SCHREGNO NOT IN (SELECT ";
        $query .= "                              SCHREGNO ";
        $query .= "                          FROM ";
        $query .= "                              MONEY_DUE_M_DAT ";
        $query .= "                          WHERE ";
        $query .= "                              YEAR = '".CTRL_YEAR."' ";
        $query .= "                              AND EXPENSE_M_CD = '11' ";
        $query .= "                              AND VALUE(UN_AUTO_PAYFLG, '0') = '1') ";
        $query .= "     AND EXPENSE_L_CD = '02' ";
        $query .= "     AND EXPENSE_M_CD IN '21' ";
        return $query;
    }

    //中分類支払い削除31
    function deletePai_M31_Query(){
        $query  = " DELETE FROM MONEY_PAID_M_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO IN (SELECT ";
        $query .= "                          SCHREGNO ";
        $query .= "                      FROM ";
        $query .= "                          FRESHMAN_DAT ";
        $query .= "                      WHERE ";
        $query .= "                          ENTERYEAR = '".CTRL_YEAR."') ";
        $query .= "     AND SCHREGNO NOT IN (SELECT ";
        $query .= "                              SCHREGNO ";
        $query .= "                          FROM ";
        $query .= "                              MONEY_DUE_M_DAT ";
        $query .= "                          WHERE ";
        $query .= "                              YEAR = '".CTRL_YEAR."' ";
        $query .= "                              AND EXPENSE_M_CD = '11' ";
        $query .= "                              AND VALUE(UN_AUTO_PAYFLG, '0') = '1') ";
        $query .= "     AND EXPENSE_L_CD = '03' ";
        $query .= "     AND EXPENSE_M_CD IN '31' ";
        return $query;
    }

    //小分類返金削除
    function deleteRePai_S_Query(){
        $query  = " DELETE FROM MONEY_REPAY_S_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO IN (SELECT ";
        $query .= "                          SCHREGNO ";
        $query .= "                      FROM ";
        $query .= "                          FRESHMAN_DAT ";
        $query .= "                      WHERE ";
        $query .= "                          ENTERYEAR = '".CTRL_YEAR."') ";
        $query .= "     AND SCHREGNO NOT IN (SELECT ";
        $query .= "                              SCHREGNO ";
        $query .= "                          FROM ";
        $query .= "                              MONEY_DUE_M_DAT ";
        $query .= "                          WHERE ";
        $query .= "                              YEAR = '".CTRL_YEAR."' ";
        $query .= "                              AND EXPENSE_M_CD = '11' ";
        $query .= "                              AND VALUE(UN_AUTO_PAYFLG, '0') = '1') ";
        $query .= "     AND EXPENSE_M_CD = '31' ";
        return $query;
    }

    //小分類支払い削除
    function deletePai_S_Query(){
        $query  = " DELETE FROM MONEY_PAID_S_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO IN (SELECT ";
        $query .= "                          SCHREGNO ";
        $query .= "                      FROM ";
        $query .= "                          FRESHMAN_DAT ";
        $query .= "                      WHERE ";
        $query .= "                          ENTERYEAR = '".CTRL_YEAR."') ";
        $query .= "     AND SCHREGNO NOT IN (SELECT ";
        $query .= "                              SCHREGNO ";
        $query .= "                          FROM ";
        $query .= "                              MONEY_DUE_M_DAT ";
        $query .= "                          WHERE ";
        $query .= "                              YEAR = '".CTRL_YEAR."' ";
        $query .= "                              AND EXPENSE_M_CD = '11' ";
        $query .= "                              AND VALUE(UN_AUTO_PAYFLG, '0') = '1') ";
        $query .= "     AND EXPENSE_M_CD = '31' ";
        return $query;
    }

    //中分類支払い追加
    function insertPai_M11_Query($model, $selectleft){
        $query  = " INSERT INTO MONEY_PAID_M_DAT ";
        $query .= "     (YEAR, ";
        $query .= "      SCHREGNO, ";
        $query .= "      EXPENSE_L_CD, ";
        $query .= "      EXPENSE_M_CD, ";
        $query .= "      PAID_INPUT_FLG, ";
        $query .= "      PAID_MONEY_DATE, ";
        $query .= "      PAID_MONEY, ";
        $query .= "      PAID_MONEY_DIV, ";
        $query .= "      REPAY_DATE, ";
        $query .= "      REPAY_MONEY, ";
        $query .= "      REPAY_DEV, ";
        $query .= "      REGISTERCD, ";
        $query .= "      UPDATED ) ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     '01', ";
        $query .= "     EXPENSE_M_CD, ";
        $query .= "     '2', ";
        $query .= "     '".str_replace("/","-",$model->field["DATE1"])."', ";
        $query .= "     MONEY_DUE, ";
        $query .= "     '02', ";
        $query .= "     CAST(NULL AS DATE), ";
        $query .= "     CAST(NULL AS INTEGER), ";
        $query .= "     CAST(NULL AS VARCHAR(2)), ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     MONEY_DUE_M_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO IN (SELECT ";
        $query .= "                          SCHREGNO ";
        $query .= "                      FROM ";
        $query .= "                          FRESHMAN_DAT ";
        $query .= "                      WHERE ";
        $query .= "                          ENTERYEAR = '".CTRL_YEAR."') ";
        $query .= "     AND EXPENSE_M_CD = '11' ";
        $query .= "     AND SCHREGNO NOT IN ".$selectleft." ";
        $query .= "     AND VALUE(UN_AUTO_PAYFLG, '0') != '1' ";
        return $query;
    }

    //中分類支払い追加
    function insertPai_M21_Query($model, $selectleft){
        $query  = " INSERT INTO MONEY_PAID_M_DAT ";
        $query .= "     (YEAR, ";
        $query .= "      SCHREGNO, ";
        $query .= "      EXPENSE_L_CD, ";
        $query .= "      EXPENSE_M_CD, ";
        $query .= "      PAID_INPUT_FLG, ";
        $query .= "      PAID_MONEY_DATE, ";
        $query .= "      PAID_MONEY, ";
        $query .= "      PAID_MONEY_DIV, ";
        $query .= "      REPAY_DATE, ";
        $query .= "      REPAY_MONEY, ";
        $query .= "      REPAY_DEV, ";
        $query .= "      REGISTERCD, ";
        $query .= "      UPDATED ) ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     '02', ";
        $query .= "     EXPENSE_M_CD, ";
        $query .= "     '2', ";
        $query .= "     '".str_replace("/","-",$model->field["DATE1"])."', ";
        $query .= "     MONEY_DUE, ";
        $query .= "     '02', ";
        $query .= "     CAST(NULL AS DATE), ";
        $query .= "     CAST(NULL AS INTEGER), ";
        $query .= "     CAST(NULL AS VARCHAR(2)), ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     MONEY_DUE_M_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO IN (SELECT ";
        $query .= "                          SCHREGNO ";
        $query .= "                      FROM ";
        $query .= "                          FRESHMAN_DAT ";
        $query .= "                      WHERE ";
        $query .= "                          ENTERYEAR = '".CTRL_YEAR."') ";
        $query .= "     AND EXPENSE_M_CD = '21' ";
        $query .= "     AND SCHREGNO NOT IN (SELECT ";
        $query .= "                              SCHREGNO ";
        $query .= "                          FROM ";
        $query .= "                              MONEY_DUE_M_DAT ";
        $query .= "                          WHERE ";
        $query .= "                              YEAR = '".CTRL_YEAR."' ";
        $query .= "                              AND EXPENSE_M_CD = '11' ";
        $query .= "                              AND VALUE(UN_AUTO_PAYFLG, '0') = '1') ";
        $query .= "     AND SCHREGNO NOT IN ".$selectleft." ";
        return $query;
    }

    //小分類支払い追加
    function insertPai_S_Query($model, $selectleft){
        $query  = " INSERT INTO MONEY_PAID_S_DAT ";
        $query .= "     (YEAR, ";
        $query .= "      SCHREGNO, ";
        $query .= "      EXPENSE_M_CD, ";
        $query .= "      EXPENSE_S_CD, ";
        $query .= "      PAID_INPUT_FLG, ";
        $query .= "      PAID_MONEY_DATE, ";
        $query .= "      PAID_MONEY, ";
        $query .= "      PAID_MONEY_DIV, ";
        $query .= "      REPAY_MONEY_DATE, ";
        $query .= "      REPAY_MONEY, ";
        $query .= "      REPAY_MONEY_DIV, ";
        $query .= "      REPAY_FLG, ";
        $query .= "      REMARK, ";
        $query .= "      REGISTERCD, ";
        $query .= "      UPDATED ) ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     '31', ";
        $query .= "     EXPENSE_S_CD, ";
        $query .= "     '2', ";
        $query .= "     '".str_replace("/","-",$model->field["DATE1"])."', ";
        $query .= "     MONEY_DUE, ";
        $query .= "     '02', ";
        $query .= "     CAST(NULL AS DATE), ";
        $query .= "     CAST(NULL AS INTEGER), ";
        $query .= "     CAST(NULL AS VARCHAR(2)), ";
        $query .= "     CAST(NULL AS VARCHAR(1)), ";
        $query .= "     CAST(NULL AS VARCHAR(30)), ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     MONEY_DUE_S_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO IN (SELECT ";
        $query .= "                          SCHREGNO ";
        $query .= "                      FROM ";
        $query .= "                          FRESHMAN_DAT ";
        $query .= "                      WHERE ";
        $query .= "                          ENTERYEAR = '".CTRL_YEAR."') ";
        $query .= "     AND SCHREGNO NOT IN (SELECT ";
        $query .= "                              SCHREGNO ";
        $query .= "                          FROM ";
        $query .= "                              MONEY_DUE_M_DAT ";
        $query .= "                          WHERE ";
        $query .= "                              YEAR = '".CTRL_YEAR."' ";
        $query .= "                              AND EXPENSE_M_CD = '11' ";
        $query .= "                              AND VALUE(UN_AUTO_PAYFLG, '0') = '1') ";
        $query .= "     AND SCHREGNO NOT IN ".$selectleft." ";
        return $query;
    }

    //中分類支払い追加
    function insertPai_M31_Query($model, $selectleft){
        $query  = " INSERT INTO MONEY_PAID_M_DAT ";
        $query .= "     (YEAR, ";
        $query .= "      SCHREGNO, ";
        $query .= "      EXPENSE_L_CD, ";
        $query .= "      EXPENSE_M_CD, ";
        $query .= "      PAID_INPUT_FLG, ";
        $query .= "      PAID_MONEY_DATE, ";
        $query .= "      PAID_MONEY, ";
        $query .= "      PAID_MONEY_DIV, ";
        $query .= "      REPAY_DATE, ";
        $query .= "      REPAY_MONEY, ";
        $query .= "      REPAY_DEV, ";
        $query .= "      REGISTERCD, ";
        $query .= "      UPDATED ) ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     '03', ";
        $query .= "     '31', ";
        $query .= "     '2', ";
        $query .= "     '".str_replace("/","-",$model->field["DATE1"])."', ";
        $query .= "     SUM(MONEY_DUE), ";
        $query .= "     '02', ";
        $query .= "     CAST(NULL AS DATE), ";
        $query .= "     CAST(NULL AS INTEGER), ";
        $query .= "     CAST(NULL AS VARCHAR(2)), ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     MONEY_DUE_S_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO IN (SELECT ";
        $query .= "                          SCHREGNO ";
        $query .= "                      FROM ";
        $query .= "                          FRESHMAN_DAT ";
        $query .= "                      WHERE ";
        $query .= "                          ENTERYEAR = '".CTRL_YEAR."') ";
        $query .= "     AND SCHREGNO NOT IN (SELECT ";
        $query .= "                              SCHREGNO ";
        $query .= "                          FROM ";
        $query .= "                              MONEY_DUE_M_DAT ";
        $query .= "                          WHERE ";
        $query .= "                              YEAR = '".CTRL_YEAR."' ";
        $query .= "                              AND EXPENSE_M_CD = '11' ";
        $query .= "                              AND VALUE(UN_AUTO_PAYFLG, '0') = '1') ";
        $query .= "     AND SCHREGNO NOT IN ".$selectleft." ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO ";
        return $query;
    }

    //交付データ削除
    function delete_Grant_Query(){
        $query  = " DELETE FROM SCHREG_GRANT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO IN (SELECT ";
        $query .= "                          SCHREGNO ";
        $query .= "                      FROM ";
        $query .= "                          FRESHMAN_DAT ";
        $query .= "                      WHERE ";
        $query .= "                          ENTERYEAR = '".CTRL_YEAR."') ";
        return $query;
    }

    //交付データ追加
    function insert_Grant_Query($model){

        $query  = " INSERT INTO SCHREG_GRANT_DAT ";
        $query .= "     (YEAR, ";
        $query .= "      SCHREGNO, ";
        $query .= "      GRANTCD, ";
        $query .= "      GRANTSDATE, ";
        $query .= "      GRANTEDATE, ";
        $query .= "      GRANT_MONEY, ";
        $query .= "      REMARK, ";
        $query .= "      REGISTERCD, ";
        $query .= "      UPDATED ) ";
        $query .= " SELECT ";
        $query .= "     ENTERYEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     SCALASHIPDIV, ";
        $query .= "     '".str_replace("/","-",$model->field["DATE2"])."', ";
        $query .= "     '".str_replace("/","-",$model->edate)."', ";    //NO001
        //NO001
        if (!$model->field["MONEY"]){
            $query .= "     CAST(NULL AS INTEGER), ";
        }else {
            $query .= "     CASE WHEN SCALASHIPDIV = '01' THEN ".$model->field["MONEY"]." ELSE CAST(NULL AS INTEGER) END, ";
        }
        $query .= "     CAST(NULL AS VARCHAR(75)), ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     FRESHMAN_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTERYEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCALASHIPDIV IS NOT NULL ";
        return $query;
    }

}
?>
