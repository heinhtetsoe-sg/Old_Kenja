<?php

require_once('for_php7.php');


class knjm040Query extends Query {

    function getSchoolCd() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJM040' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //日付
    function GetYear()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCH_ATTEND_DAT ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";

        return $query;
    }

    //クラス
    function GetCls($model)
    {

        $query  = " WITH CHAIRTABLE AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     CHAIRCD ";
        $query .= " FROM ";
        $query .= "     HR_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     t1.CHAIRCD AS VALUE, ";
        $query .= "     t2.CHAIRNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIRTABLE t1 ";
        $query .= "     LEFT JOIN CHAIR_DAT t2 ON t1.CHAIRCD = t2.CHAIRCD ";
        $query .= "     AND t2.YEAR = '".$model->field["YEAR"]."' ";
        $query .= "     AND t2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     t1.CHAIRCD ";

        return $query;
    }

    //CVS作成用のQUERY
    function selectCsvQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     t1.YEAR, ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t1.EXECUTEDATE, ";
        $query .= "     t1.CHAIRCD, ";
        $query .= "     t2.CHAIRNAME, ";
        $query .= "     t1.PERIODCD, ";
        $query .= "     t1.RECEIPT_DATE, ";
        $query .= "     t1.RECEIPT_TIME ";
        $query .= " FROM ";
        $query .= "     HR_ATTEND_DAT t1 ";
        $query .= "     LEFT JOIN CHAIR_DAT t2 ON t1.CHAIRCD = t2.CHAIRCD ";
        $query .= "     AND t2.YEAR = '".$model->field["YEAR"]."' ";
        $query .= "     AND t2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR = '".$model->field["YEAR"]."' ";
        if ($model->field["CHAIRCD"] != 0){
            $query .= "     AND t1.CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        }
        $query .= " ORDER BY t1.CHAIRCD,t1.PERIODCD,t1.SCHREGNO ";

        return $query;
    }

}
?>
