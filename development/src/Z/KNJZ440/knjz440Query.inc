<?php

require_once('for_php7.php');

class knjz440Query extends Query {

    function getCurriculumCD($model) {
        $query  = " SELECT ";
        $query .= "     T1.NAMECD2 AS VALUE, ";
        $query .= "     T1.NAMECD2 || ':' || T1.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1 = 'Z018' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getCouseMajor($model) {
        $query  = " SELECT ";
        $query .= "     T1.COURSECD || ':' || T1.MAJORCD AS VALUE, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || ':' || L1.COURSENAME || T1.MAJORNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     MAJOR_MST T1 ";
        $query .= "     LEFT JOIN COURSE_MST L1 ON T1.COURSECD = L1.COURSECD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getClassName($model) {
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || ':' || T1.CLASSNAME AS LABEL, ";
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND AS VALUE ";
        } else {
            $query .= "     T1.CLASSCD || ':' || T1.CLASSNAME AS LABEL, ";
            $query .= "     T1.CLASSCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     CLASS_MST T1, ";
        $query .= "     STANDARD_CREDIT_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.CLASSCD = T2.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        }
        $query .= "     AND T2.CURRICULUM_CD = '{$model->field["CURRICULUM_CD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getListTitle($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
        }
        $query  = " SELECT ";
        $query .= "     SUBCLASSABBV, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE ";
        } else {
            $query .= "     SUBCLASSCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD          = '".$class_array[0]."' ";
            $query .= "     AND SCHOOL_KIND      = '".$class_array[1]."' ";
            $query .= "     AND CURRICULUM_CD    = '{$model->field["CURRICULUM_CD"]}' ";
        } else {
            $query .= "     substr(SUBCLASSCD, 1, 2) = '{$model->field["CLASSCD"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getList($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
        }
        $query .= " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE, ";
        } else {
            $query .= "     SUBCLASSCD AS VALUE, ";
        }
        $query .= "     SEQ ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_REQUIRED_STUDY_DAT ";
        $query .= " WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD     = '".$class_array[0]."' ";
            $query .= " AND SCHOOL_KIND = '".$class_array[1]."' ";
        } else {
            $query .= "     CLASSCD = '{$model->field["CLASSCD"]}' ";
        }
        $query .= "     AND CURRICULUM_CD = '{$model->field["CURRICULUM_CD"]}' ";
        $query .= "     AND COURSECD || ':' || MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
        $query .= " ORDER BY INT(SEQ) ";

        return $query;
    }

    function getSeq($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
        }
        $query .= " SELECT ";
        $query .= "     CLASSCD, ";
        $query .= "     SEQ ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_REQUIRED_STUDY_DAT ";
        $query .= " WHERE  ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD     = '".$class_array[0]."' ";
            $query .= " AND SCHOOL_KIND = '".$class_array[1]."' ";
        } else {
            $query .= "     CLASSCD = '{$model->field["CLASSCD"]}' ";
        }
        $query .= "     AND CURRICULUM_CD = '{$model->field["CURRICULUM_CD"]}' ";
        $query .= "     AND COURSECD || ':' || MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
        $query .= " GROUP BY ";
        $query .= "     CLASSCD, ";
        $query .= "     SEQ ";
        $query .= " ORDER BY INT(SEQ) ";

        return $query;
    }

    function getDeleteQuery($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
        }
        $query .= " DELETE FROM ";
        $query .= "     SUBCLASS_REQUIRED_STUDY_DAT ";
        $query .= " WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD     = '".$class_array[0]."' ";
            $query .= " AND SCHOOL_KIND = '".$class_array[1]."' ";
        } else {
            $query .= "     CLASSCD = '{$model->field["CLASSCD"]}' ";
        }
        $query .= "     AND CURRICULUM_CD = '{$model->field["CURRICULUM_CD"]}' ";
        $query .= "     AND COURSECD || ':' || MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";

        return $query;
    }

    function getUpdateQuery($seq, $subclassCd, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1' && strlen($subclassCd) > 6) {
            $school_kind = substr($subclassCd, 3, 1);
            $subclassCd = substr($subclassCd, 7, 6);
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
        }
    
        list($courseCd, $majorCd) = preg_split("/:/", $model->field["COURSE_MAJOR"]);
        $query .= " INSERT INTO SUBCLASS_REQUIRED_STUDY_DAT ( ";
        $query .= "     CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND, ";
        }
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     SEQ, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " VALUES ";
        $query .= "  ( ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     '".$class_array[0]."', ";
        } else {
            $query .= "     '{$model->field["CLASSCD"]}', ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     '".$class_array[1]."', ";
        }
        $query .= "     '{$model->field["CURRICULUM_CD"]}', ";
        $query .= "     '{$subclassCd}', ";
        $query .= "     '{$courseCd}', ";
        $query .= "     '{$majorCd}', ";
        $query .= "     '{$seq}', ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " ) ";

        return $query;
    }

}
?>
