<?php

require_once('for_php7.php');

class knjm330Query extends Query
{

    //�Ȗڃ��X�g�擾
    function getSubclass(&$model)
    {
        $query  = "SELECT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "       t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' ||  t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "    t1.SUBCLASSCD, ";
        }
        $query .= "    MAX(t2.SUBCLASSABBV) AS SUBCLASSABBV ";
        $query .= "FROM ";
        $query .= "    REP_STANDARDDATE_DAT t1 ";
        $query .= "    LEFT JOIN SUBCLASS_MST t2 ON t1.SUBCLASSCD = t2.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         AND t1.CLASSCD = t2.CLASSCD ";
            $query .= "         AND t1.SCHOOL_KIND = t2.SCHOOL_KIND ";
            $query .= "         AND t1.CURRICULUM_CD = t2.CURRICULUM_CD ";
        }
        $query .= "WHERE ";
        $query .= "    t1.YEAR = '" .$model->field["YEAR"] ."' ";
        $query .= "GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "t1.CLASSCD, ";
            $query .= "t1.SCHOOL_KIND, ";
            $query .= "t1.CURRICULUM_CD, ";
        }
        $query .= "t1.SUBCLASSCD ";
        return $query;
    }
    //���|�[�g�񐔎擾
    function getReportcount(&$model)
    {
        $query  = "SELECT ";
        $query .= "    STANDARD_SEQ ";
        $query .= "FROM ";
        $query .= "    REP_STANDARDDATE_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '" .$model->field["YEAR"] ."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "    AND CLASSCD || '-' || SCHOOL_KIND || '-' ||  CURRICULUM_CD || '-' || SUBCLASSCD = '" .$model->field["KAMOKU"] ."' ";
        } else {
            $query .= "    AND SUBCLASSCD = '" .$model->field["KAMOKU"] ."' ";
        }

        return $query;
    }

}
?>
