<?php

require_once('for_php7.php');


class knjp370Query extends Query {

    //担当
    function GetStaff($model){
        $query  = " SELECT ";
        $query .= "     STAFFCD, ";
        $query .= "     STAFFNAME ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     STAFFCD ";

        return $query;
    }

    //小分類 NO005
    function GetPaids($model){
        $query  = " SELECT ";
        $query .= "     T1.EXPENSE_M_CD, ";
        $query .= "     T1.EXPENSE_S_CD, ";
        $query .= "     T2.EXPENSE_S_NAME, ";
        $query .= "     T1.REPAY_MONEY_DATE ";
        $query .= " FROM ";
        $query .= "     MONEY_REPAY_S_DAT T1 ";
        $query .= "     LEFT JOIN V_EXPENSE_S_MST T2 ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.EXPENSE_M_CD = T1.EXPENSE_M_CD ";
        $query .= "     AND T2.EXPENSE_S_CD = T1.EXPENSE_S_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.REPAY_MONEY IS NOT NULL ";
        $query .= "     AND VALUE(T1.REPAY_FLG, '0') <> '1' ";
        $query .= " GROUP BY ";
        $query .= "     T1.EXPENSE_M_CD, ";
        $query .= "     T1.EXPENSE_S_CD, ";
        $query .= "     T2.EXPENSE_S_NAME, ";
        $query .= "     T1.REPAY_MONEY_DATE ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXPENSE_M_CD, ";
        $query .= "     T1.REPAY_MONEY_DATE, ";
        $query .= "     T1.EXPENSE_S_CD, ";
        $query .= "     T2.EXPENSE_S_NAME ";

        return $query;
    }

    //テキスト取得
    function getText(){
        $query  = " SELECT ";
        $query .= "     DIV, ";
        $query .= "     REMARK ";
        $query .= " FROM ";
        $query .= "     SCHOOL_EXPENSES_SYS_INI ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJP370' ";
        $query .= "     AND REMARK IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     DIV ";

        return $query;
    }

    //クラスデータ取得
    function getclass($model,$inSentence)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     GRADE||HR_CLASS VALUE,HR_NAME LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND GRADE||HR_CLASS IN (SELECT GRADE||HR_CLASS ";
        $query .= "                             FROM SCHREG_REGD_DAT ";
        $query .= "                             WHERE YEAR = '".CTRL_YEAR."' AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "                                   AND SCHREGNO IN (SELECT SCHREGNO ";
        $query .= "                                                    FROM MONEY_REPAY_S_DAT ";
        $query .= "                                                    WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "                                                    AND EXPENSE_M_CD || EXPENSE_S_CD || CAST(REPAY_MONEY_DATE AS VARCHAR(10)) IN ".$inSentence." ";
        $query .= "                                                    AND REPAY_MONEY IS NOT NULL ";
        $query .= "                                                    AND VALUE(REPAY_FLG, '0') <> '1') ) ";
        $query .= " GROUP BY ";
        $query .= "     GRADE,HR_CLASS,HR_NAME ";
        $query .= " ORDER BY ";
        $query .= "     GRADE,HR_CLASS,HR_NAME ";

        return $query;
    }

    //返戻データ取得 NO001
    function getrepai($model,$inSentence)
    {
        $query  = "";
        $query .= " WITH MONEYTBL AS (SELECT DISTINCT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     MONEY_REPAY_S_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND EXPENSE_M_CD || EXPENSE_S_CD || CAST(REPAY_MONEY_DATE AS VARCHAR(10)) IN ".$inSentence." ";
        $query .= "     AND REPAY_MONEY IS NOT NULL ";
        $query .= "     AND VALUE(REPAY_FLG, '0') <> '1' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     t2.SCHREGNO,t2.ATTENDNO,t2.MAJORCD,t2.GRADE,t2.HR_CLASS,t3.NAME_SHOW,t4.HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT t2 LEFT JOIN SCHREG_BASE_MST t3 ON t2.SCHREGNO = t3.SCHREGNO, ";
        $query .= "     SCHREG_REGD_HDAT t4 ";
        $query .= " WHERE ";
        $query .= "     t2.YEAR = '".CTRL_YEAR."' AND t2.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     t2.GRADE||t2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     t4.YEAR = t2.YEAR AND t4.SEMESTER = t2.SEMESTER AND ";
        $query .= "     t4.GRADE||t4.HR_CLASS = t2.GRADE||t2.HR_CLASS AND ";
        $query .= "     t2.SCHREGNO IN ( SELECT t5.SCHREGNO ";
        $query .= "                      FROM MONEYTBL t5) ";
        $query .= " ORDER BY ";
        $query .= "     t2.ATTENDNO ";

        return $query;
    }

    //返戻データ取得 NO005
    function getrepai2($model,$inSentence)
    {
        $query  = "";
        $query .= " WITH MONEYTBL AS (SELECT DISTINCT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     MONEY_REPAY_S_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND EXPENSE_M_CD || EXPENSE_S_CD || CAST(REPAY_MONEY_DATE AS VARCHAR(10)) IN ".$inSentence." ";
        $query .= "     AND REPAY_MONEY IS NOT NULL ";
        $query .= "     AND VALUE(REPAY_FLG, '0') <> '1' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     t2.SCHREGNO,t2.ATTENDNO,t2.MAJORCD,t2.GRADE,t2.HR_CLASS,t3.NAME_SHOW,t4.HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT t2 LEFT JOIN SCHREG_BASE_MST t3 ON t2.SCHREGNO = t3.SCHREGNO, ";
        $query .= "     SCHREG_REGD_HDAT t4 ";
        $query .= " WHERE ";
        $query .= "     t2.YEAR = '".CTRL_YEAR."' AND t2.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     t4.YEAR = t2.YEAR AND t4.SEMESTER = t2.SEMESTER AND ";
        $query .= "     t4.GRADE||t4.HR_CLASS = t2.GRADE||t2.HR_CLASS AND ";
        $query .= "     t2.SCHREGNO IN ( SELECT t5.SCHREGNO ";
        $query .= "                      FROM MONEYTBL t5) ";
        $query .= " ORDER BY ";
        $query .= "     t2.ATTENDNO ";

        return $query;
    }

    //DELETE(更新処理) NO001
    function getDeleteQuery()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM SCHOOL_EXPENSES_SYS_INI WHERE PROGRAMID = 'KNJP370' AND DIV IN ('0001','0002') ";

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return ;
    }
    
    //DELETE(更新処理) NO002
    function getDeleteQuery2()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM SCHOOL_EXPENSES_SYS_INI WHERE PROGRAMID = 'KNJP370' AND DIV = '".CTRL_YEAR."' ";

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return ;
    }
    
    //UPDATE(更新処理) NO001
    function getUpdateQuery(&$model,$cnt)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $data["PROGRAMID"][TEXT]  = "KNJP370";
        $data["DIV"][TEXT]        = "000".$cnt;
        if ($cnt == 1){
            $data["REMARK"][TEXT]     = $model->text1;
        }else {
            $data["REMARK"][TEXT]     = $model->text2;
        }
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][NUMBER]  = "SYSDATE()";

        $query = Query::insertSQL($data, "SCHOOL_EXPENSES_SYS_INI");

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }
}
?>
