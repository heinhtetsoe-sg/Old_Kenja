<?php

require_once('for_php7.php');


class knjm060mQuery extends Query {

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
        $query .= "     PROGRAMID = 'KNJM060M' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //年度
    function GetYear()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     RECORD_DAT ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";

        return $query;
    }

    //学期
    function GetSem($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' ";
        $query .= "     AND SEMESTER < '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //講座
    function GetSub($model)
    {
        $query  = " with ATABLE as (SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
            $query .= "     SUBCLASSCD ";
        } else {
            $query .= "     SUBCLASSCD ";
        }
        $query .= " FROM ";
        $query .= "     RECORD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD AS VALUE, ";
        } else {
            $query .= "     t1.SUBCLASSCD AS VALUE, ";
        }
        $query .= "     t2.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ATABLE t1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST t2 ON t1.SUBCLASSCD = t2.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "          AND t1.CLASSCD = t2.CLASSCD ";
            $query .= "          AND t1.SCHOOL_KIND = t2.SCHOOL_KIND ";
            $query .= "          AND t1.CURRICULUM_CD = t2.CURRICULUM_CD ";
        }
        $query .= " ORDER BY VALUE ";

        return $query;
    }

    //CVS作成用のQUERY
    function selectCsvQuery($model)
    {

        $query  = " SELECT ";
        $query .= "     t1.CLASSCD, ";
        $query .= "     t1.SCHOOL_KIND, ";
        $query .= "     t1.CURRICULUM_CD, ";
        $query .= "     t1.SUBCLASSCD, ";
        $query .= "     t2.SUBCLASSNAME,t1.SCHREGNO,t3.NAME, ";
        switch ($model->field["GAKKI"]){
            case 0;
                if ($model->field["OUTDIV"] == 0){
                    $query .= "     t1.SEM1_INTR_VALUE, ";
                    $query .= "     t1.SEM1_TERM_VALUE, ";
                    $query .= "     t1.SEM2_INTR_VALUE, ";
                    $query .= "     t1.SEM2_TERM_VALUE, ";
                    $query .= "     t1.GRAD_VALUE2, ";
                    $query .= "     t1.GRAD_VALUE ";
                }else if ($model->field["OUTDIV"] == 1) {
                    $query .= "     t1.SEM1_INTR_VALUE, ";
                    $query .= "     t1.SEM1_TERM_VALUE, ";
                    $query .= "     t1.SEM2_INTR_VALUE, ";
                    $query .= "     t1.SEM2_TERM_VALUE ";
                }else {
                    $query .= "     t1.GRAD_VALUE2, ";
                    $query .= "     t1.GRAD_VALUE ";
                }
                break ;
            case 1;
                if ($model->field["OUTDIV"] == 0){
                    $query .= "     t1.SEM1_INTR_VALUE, ";
                    $query .= "     t1.SEM1_TERM_VALUE, ";
                    $query .= "     t1.GRAD_VALUE2, ";
                    $query .= "     t1.GRAD_VALUE ";
                }else if ($model->field["OUTDIV"] == 1) {
                    $query .= "     t1.SEM1_INTR_VALUE, ";
                    $query .= "     t1.SEM1_TERM_VALUE ";
                }else {
                    $query .= "     t1.GRAD_VALUE2, ";
                    $query .= "     t1.GRAD_VALUE ";
                }
                break ;
            case 2;
                if ($model->field["OUTDIV"] == 0){
                    $query .= "     t1.SEM2_INTR_VALUE, ";
                    $query .= "     t1.SEM2_TERM_VALUE, ";
                    $query .= "     t1.GRAD_VALUE2, ";
                    $query .= "     t1.GRAD_VALUE ";
                }else if ($model->field["OUTDIV"] == 1) {
                    $query .= "     t1.SEM2_INTR_VALUE, ";
                    $query .= "     t1.SEM2_TERM_VALUE ";
                }else {
                    $query .= "     t1.GRAD_VALUE2, ";
                    $query .= "     t1.GRAD_VALUE ";
                }
                break ;
        }
        $query .= " FROM ";
        $query .= "     RECORD_DAT t1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST t2 ON t1.SUBCLASSCD = t2.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "          AND t1.CLASSCD = t2.CLASSCD ";
            $query .= "          AND t1.SCHOOL_KIND = t2.SCHOOL_KIND ";
            $query .= "          AND t1.CURRICULUM_CD = t2.CURRICULUM_CD ";
        }
        $query .= "     LEFT JOIN SCHREG_BASE_MST t3 ON t1.SCHREGNO = t3.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR = '".$model->field["YEAR"]."' ";
        if ($model->field["SUBCLASSNAME"] != 0){
            if ($model->Properties["useCurriculumcd"] == "1") {
                $query .= "     AND t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD = '".$model->field["SUBCLASSNAME"]."' ";
            } else {
                $query .= "     AND t1.SUBCLASSCD = '".$model->field["SUBCLASSNAME"]."' ";
            }
        }
        $query .= " ORDER BY t1.SUBCLASSCD,t1.SCHREGNO ";

        return $query;
    }

}
?>
