<?php

require_once('for_php7.php');


class knjm030mQuery extends Query {

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
        $query .= "     PROGRAMID = 'KNJM030M' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //名称
    function GetName($model)
    {
        $query  = " WITH ATABLE AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     SCHOOLINGKINDCD ";
        $query .= " FROM ";
        $query .= "     SCH_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' ";
        if ($model->field["SUBCLASSNAME"] > 0){
            $query .= "     AND CHAIRCD = '".$model->field["SUBCLASSNAME"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOLINGKINDCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     SCHOOLINGKINDCD AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     ATABLE LEFT JOIN NAME_MST ON SCHOOLINGKINDCD = NAMECD2 ";
        $query .= "     AND NAMECD1 = 'M001' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

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
        $query .= "     VALUE DESC ";

        return $query;
    }

    //講座
    function GetChr($model)
    {
        $query  = " with ATABLE(CCD) as (SELECT DISTINCT ";
        $query .= "     CHAIRCD ";
        $query .= " FROM ";
        $query .= "     SCH_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     t2.SUBCLASSNAME AS LABEL, ";
        $query .= "     t1.CHAIRCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT t1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST t2 ON t1.SUBCLASSCD = t2.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "          AND t1.CLASSCD = t2.CLASSCD ";
            $query .= "          AND t1.SCHOOL_KIND = t2.SCHOOL_KIND ";
            $query .= "          AND t1.CURRICULUM_CD = t2.CURRICULUM_CD ";
        }
        $query .= "     ,atable w2 ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR = '".$model->field["YEAR"]."' AND ";
        $query .= "     t1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     t1.CHAIRCD = CCD ";
        $query .= " ORDER BY VALUE ";
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
        $query .= "     t1.SCHOOLING_SEQ, ";
        $query .= "     t1.CREDIT_TIME, ";
        $query .= "     t1.RECEIPT_DATE, ";
        $query .= "     t1.RECEIPT_TIME, ";
        $query .= "     t1.STAFFCD, ";
        $query .= "     t1.SCHOOLINGKINDCD, ";
        $query .= "     t1.REMARK ";
        $query .= " FROM ";
        $query .= "     SCH_ATTEND_DAT t1 ";
        $query .= "     LEFT JOIN CHAIR_DAT t2 ON t1.CHAIRCD = t2.CHAIRCD ";
        $query .= "     AND t2.YEAR = '".$model->field["YEAR"]."' ";
        $query .= "     AND t2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR = '".$model->field["YEAR"]."' ";
        if ($model->field["SUBCLASSNAME"] != 0){
            $query .= "     AND t1.CHAIRCD = '".$model->field["SUBCLASSNAME"]."' ";
        }
        if ($model->field["SCHOOLINGKINDCD"] != 0){
            $query .= "     AND t1.SCHOOLINGKINDCD = '".$model->field["SCHOOLINGKINDCD"]."' ";
        }
        $query .= " ORDER BY t1.CHAIRCD,t1.SCHOOLINGKINDCD,t1.SCHREGNO,t1.PERIODCD,t1.SCHOOLING_SEQ ";

        return $query;
    }

}
?>
