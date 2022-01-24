<?php

require_once('for_php7.php');


class knjh120Query extends Query {

    //クラス一覧取得
    function getClassList($model) {
        $query  = "SELECT ";
        $query .= "     T1.GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= "FROM "; 
        $query .= "     SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "WHERE "; 
        $query .= "     T1.YEAR      = '".$model->control["年度"]."' ";
        $query .= " AND SEMESTER  = '".$model->control["学期"]."' ";

        return $query;
    }

    //科目名称取得
    function getName($model) {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || '　' || SUBCLASSNAME AS LABEL, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE ";
        } else {
            $query .= "     SUBCLASSCD || '　' || SUBCLASSNAME AS LABEL, ";
            $query .= "     SUBCLASSCD AS VALUE";
        }
        $query .= " FROM "; 
        $query .= "     SUBCLASS_MST ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .=" WHERE ";
                $query .= " SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .=" WHERE ";
            $query .= " SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY "; 
        $query .= "     VALUE ";
        
        return $query;
    }
}
?>
