<?php

require_once('for_php7.php');


class knjp373Query extends Query {

    //担当
    function GetStaff($model){
        $query  = " SELECT ";
        $query .= "     STAFFCD AS VALUE, ";
        $query .= "     STAFFNAME AS LABEL";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND JOBCD = '3000' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    //メニュータイトル取得
    function getTitle() {
        $query .= "  SELECT ";
        $query .= "    MENUNAME ";
        $query .= "  FROM ";
        $query .= "    MENU_MST ";
        $query .= "  WHERE ";
        $query .= "    PROGRAMID = 'KNJP373' ";

        return $query;
    }
    //対象年月
    function getYearMonth()
    {
        $year = CTRL_YEAR;
        $nextYear = CTRL_YEAR + 1;

        $query  = " SELECT DISTINCT ";
        $query .= "     PAID_YEARMONTH AS VALUE, ";
        $query .= "     SUBSTR(PAID_YEARMONTH, 1, 4) || '年' || SUBSTR(PAID_YEARMONTH, 5) || '月' AS LABEL ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND PAID_YEARMONTH IS NOT NULL ";
        $query .= "     AND VALUE(PLAN_CANCEL_FLG, '0') = '0' ";
        $query .= "     AND VALUE(PLAN_LOCK_FLG, '0') = '0' ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     ADD_PAID_YEARMONTH AS VALUE, ";
        $query .= "     SUBSTR(ADD_PAID_YEARMONTH, 1, 4) || '年' || SUBSTR(ADD_PAID_YEARMONTH, 5) || '月' AS LABEL ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND ADD_PAID_YEARMONTH IS NOT NULL ";
        $query .= "     AND VALUE(ADD_PLAN_CANCEL_FLG, '0') = '0' ";
        $query .= "     AND VALUE(ADD_PLAN_LOCK_FLG, '0') = '0' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //現年月
    function getSysYearMonth()
    {
        $query = " VALUES(CAST(YEAR(SYSDATE()) AS CHAR(4)) || CAST(MONTH(SYSDATE()) AS CHAR(2))) ";

        return $query;
    }

    //クラスデータ取得
    function getclass($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     GRADE || '-' ||HR_CLASS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     GRADE,HR_CLASS,HR_NAME ";

        return $query;
    }

    //生徒データ取得
    function getsch($model)
    {
        list($grade, $hr_class) = explode("-", $model->field["GRADE_HR_CLASS"]);
        $query  = "  SELECT DISTINCT ";
        $query .= "  	T1.SCHREGNO, ";
        $query .= "  	T1.GRADE, ";
        $query .= "  	T1.HR_CLASS, ";
        $query .= "  	T3.HR_NAME, ";
        $query .= "  	T3.HR_NAMEABBV, ";
        $query .= "  	T1.ATTENDNO, ";
        $query .= "  	T2.SEX, ";
        $query .= "  	T2.NAME_SHOW, ";
        $query .= "  	T2.NAME_KANA ";
        $query .= "  FROM ";
        $query .= "  	SCHREG_REGD_DAT T1, ";
        $query .= "  	SCHREG_BASE_MST T2, ";
        $query .= "  	SCHREG_REGD_HDAT T3 ";
        $query .= "  WHERE ";
        $query .= "  	T1.YEAR     = T3.YEAR AND ";
        $query .= "  	T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "  	T1.GRADE    = T3.GRADE AND ";
        $query .= "  	T1.HR_CLASS = T3.HR_CLASS AND ";
        $query .= "  	T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "  	T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "  	T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "  	T1.GRADE = '".$grade."' AND ";
        $query .= "  	T1.HR_CLASS = '".$hr_class."' ";
        $query .= "  ORDER BY ";
        $query .= "  	T1.GRADE, ";
        $query .= "  	T1.HR_CLASS, ";
        $query .= "  	T1.ATTENDNO ";

        return $query;
    }

    //DELETE(更新処理)
    function getDeleteQuery2()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM SCHOOL_EXPENSES_SYS_INI WHERE PROGRAMID = 'KNJP373' AND DIV = '".CTRL_YEAR."' ";

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return ;
    }
    
    //UPDATE(更新処理)
    function getUpdateQuery2(&$model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $data["PROGRAMID"][TEXT]  = "KNJP373";
        $data["DIV"][TEXT]        = CTRL_YEAR;
        $data["DATE1"][TEXT]      = str_replace("/","-",$model->date);
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][NUMBER]  = "SYSDATE()";

        $query = Query::insertSQL($data, "SCHOOL_EXPENSES_SYS_INI");

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    //三井住友
    function getVar1() {
        $query  = " SELECT ";
        $query .= "     T1.VAR1 ";
        //$query .= "    ,T1.VAR2 ";
        //$query .= "    ,L1.BANKNAME ";
        //$query .= "    ,L1.BRANCHNAME ";
        $query .= " FROM ";
        $query .= "     SCHOOL_EXPENSES_SYS_INI T1 ";
        $query .= "     LEFT JOIN BANK_MST L1 ON L1.BANKCD = T1.VAR1 AND L1.BRANCHCD = T1.VAR2 ";
        $query .= " WHERE ";
        $query .= "     T1.PROGRAMID = 'BANK' AND ";
        $query .= "     T1.DIV = '0001' ";

        return $query;
    }

    /** クラス別軽減データを抽出 */
    function meisaiSql($model) {
        $selectleft = explode(",", $model->selectleft); //csv出力対象生徒
        $sqlVar .= "IN (";
        $com = "";
        foreach ($selectleft as $key => $val) {
            $schno = explode(":", $val);
            $sqlVar .= $com . "'" . $schno[0] . "'";
            $com = ",";
        }
        $sqlVar .= ")";
        
        $query  = "  SELECT ";
        $query .= "    T1.SCHREGNO ";
        $query .= "    , T2.NAME ";
        $query .= "    , T1.GRADE ";
        $query .= "    , T1.HR_CLASS ";
        $query .= "    , T1.ATTENDNO ";
        $query .= "    , T4.BANKCD ";
        $query .= "    , T5.BANKNAME_KANA ";
        $query .= "    , T4.BRANCHCD ";
        $query .= "    , T5.BRANCHNAME_KANA ";
        $query .= "    , T4.DEPOSIT_ITEM ";
        $query .= "    , T6.ABBV1 ";
        $query .= "    , T4.ACCOUNTNO ";
        $query .= "    , T4.ACCOUNTNAME ";
        $query .= "  FROM ";
        $query .= "    SCHREG_REGD_DAT T1 ";
        $query .= "    LEFT JOIN SCHREG_BASE_MST T2 ";
        $query .= "      ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "    LEFT JOIN SCHREG_REGD_HDAT T3 ";
        $query .= "      ON T1.YEAR = T3.YEAR ";
        $query .= "      AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "      AND T1.GRADE = T3.GRADE ";
        $query .= "      AND T1.HR_CLASS = T3.HR_CLASS ";
        $query .= "    LEFT JOIN REGISTBANK_DAT T4 ";
        $query .= "      ON T1.SCHREGNO = T4.SCHREGNO ";
        $query .= "    LEFT JOIN BANK_MST T5 ";
        $query .= "      ON T4.BANKCD = T5.BANKCD ";
        $query .= "      AND T4.BRANCHCD = T5.BRANCHCD ";
        $query .= "    LEFT JOIN NAME_MST T6 ";
        $query .= "      ON T6.NAMECD1 = 'G203' ";
        $query .= "      AND T4.DEPOSIT_ITEM = T6.NAMECD2 ";
        $query .= "  WHERE ";
        $query .= "    T1.SCHREGNO ".$sqlVar." AND ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "  ORDER BY ";
        $query .= "    GRADE ";
        $query .= "    , HR_CLASS ";
        $query .= "    , ATTENDNO ";

        return $query;
    }

}
?>
