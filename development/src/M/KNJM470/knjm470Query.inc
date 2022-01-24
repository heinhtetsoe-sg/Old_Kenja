<?php

require_once('for_php7.php');

class knjm470Query extends Query {

    //�Ȗڎ擾
    function GetSubclass($model)
    {

        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "     t1.SUBCLASSCD, ";
        }
        $query .= "     t2.CHAIRCD,t1.SUBCLASSNAME,t2.CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST t1 ";
        $query .= "     LEFT JOIN CHAIR_DAT t2 ON t2.SUBCLASSCD = t1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND t2.CLASSCD = t1.CLASSCD ";
            $query .= "     AND t2.SCHOOL_KIND = t1.SCHOOL_KIND ";
            $query .= "     AND t2.CURRICULUM_CD = t1.CURRICULUM_CD ";
        }
        $query .= "     AND t2.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND t2.SEMESTER = '".$model->field["GAKKI"]."' ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND t1.SUBCLASSCD NOT LIKE '92%' ";
        $query .= " ORDER BY ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     t1.CLASSCD, ";
            $query .= "     t1.SCHOOL_KIND, ";
            $query .= "     t1.CURRICULUM_CD, ";
        }
        $query .= "     t1.SUBCLASSCD,t2.CHAIRCD ";

        return $query;
    }

    //�N���X�f�[�^�擾
    function getClass($model)
    {
        $query  = " SELECT ";
        $query .= "     HR_NAME, ";
        $query .= "     GRADE || HR_CLASS AS VALUE ";
        $query .= " FROM ";
        if ($model->subcd != 0){
            $query .= "     CHAIR_CLS_DAT t1 ";
            $query .= "     LEFT JOIN SCHREG_REGD_HDAT t2 ON TRGTGRADE || TRGTCLASS = GRADE || HR_CLASS ";
            $query .= "     AND t2.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND t2.SEMESTER = '".$model->field["GAKKI"]."' ";
            $query .= " WHERE ";
            $query .= "     t1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND t1.SEMESTER = '".$model->field["GAKKI"]."' ";
            $query .= "     AND t1.CHAIRCD = '".$model->chacd."' ";
        }else {
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".$model->field["GAKKI"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     GRADE,HR_CLASS ";

        return $query;
    }

}
?>
