<?php

require_once('for_php7.php');


class knjm050Query extends Query {

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
        $query .= "     PROGRAMID = 'KNJM050' ";
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
        $query .= "     REP_PRESENT_DAT ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";

        return $query;
    }

    //科目
    function GetChr($model)
    {
        $query  = " SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD AS VALUE, ";
        } else {
            $query .= "     t1.SUBCLASSCD AS VALUE, ";
        }
        $query .= "     t2.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     REP_PRESENT_DAT t1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST t2 ON t1.SUBCLASSCD = t2.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "          AND t1.CLASSCD = t2.CLASSCD ";
            $query .= "          AND t1.SCHOOL_KIND = t2.SCHOOL_KIND ";
            $query .= "          AND t1.CURRICULUM_CD = t2.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     t1.YEAR = '".$model->field["YEAR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //CVS作成用のQUERY
    function selectCsvQuery($model)
    {

        $query  = " SELECT ";
        $query .= "     SUBSTR(t1.YEAR,4,1) as YEAR,t1.STANDARD_SEQ,t1.REPRESENT_SEQ, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     t1.CLASSCD, ";
            $query .= "     t1.SCHOOL_KIND, ";
            $query .= "     t1.CURRICULUM_CD, ";
        }
        $query .= "     t1.SUBCLASSCD, ";
        $query .= "     t2.SUBCLASSNAME, ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t1.RECEIPT_DATE, ";
        $query .= "     t1.RECEIPT_TIME, ";
        $query .= "     t1.STAFFCD, ";
        $query .= "     t1.GRAD_VALUE, ";
        $query .= "     t1.GRAD_DATE, ";
        $query .= "     t1.GRAD_TIME ";
        $query .= " FROM ";
        $query .= "     REP_PRESENT_DAT t1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST t2 ON t1.SUBCLASSCD = t2.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "          AND t1.CLASSCD = t2.CLASSCD ";
            $query .= "          AND t1.SCHOOL_KIND = t2.SCHOOL_KIND ";
            $query .= "          AND t1.CURRICULUM_CD = t2.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     t1.YEAR = '".$model->field["YEAR"]."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            if ($model->field["SUBCLASSNAME"] != 0){
                $query .= "     AND t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD = '".$model->field["SUBCLASSNAME"]."' ";
            }
        } else {
            if ($model->field["SUBCLASSNAME"] != 0){
                $query .= "     AND t1.SUBCLASSCD = '".$model->field["SUBCLASSNAME"]."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     t1.SUBCLASSCD, ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t1.RECEIPT_DATE, ";
        $query .= "     t1.RECEIPT_TIME ";

        return $query;
    }

}
?>
