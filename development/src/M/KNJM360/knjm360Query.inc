<?php

require_once('for_php7.php');


class knjm360Query extends Query {

    //科目取得
    function GetSubclass($model)
    {
        $query  = "SELECT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "     t1.SUBCLASSCD, ";
        }
        $query .= "    t2.SUBCLASSNAME ";
        $query .= "FROM ";
        $query .= "    REP_PRESENT_DAT t1 left join SUBCLASS_MST t2 ON t1.SUBCLASSCD = t2.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND t1.CLASSCD = t2.CLASSCD ";
            $query .= "     AND t1.SCHOOL_KIND = t2.SCHOOL_KIND ";
            $query .= "     AND t1.CURRICULUM_CD = t2.CURRICULUM_CD ";
        }
        $query .= "WHERE ";
        $query .= "    t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND t1.GRAD_DATE is null ";
        $query .= "GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     t1.CLASSCD, ";
            $query .= "     t1.SCHOOL_KIND, ";
            $query .= "     t1.CURRICULUM_CD, ";
        }
        $query .= "    t1.SUBCLASSCD, ";
        $query .= "    t2.SUBCLASSNAME ";
        $query .= "ORDER BY ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     t1.CLASSCD, ";
            $query .= "     t1.SCHOOL_KIND, ";
            $query .= "     t1.CURRICULUM_CD, ";
        }
        $query .= "    t1.SUBCLASSCD ";

        return $query;
    }

}
?>
