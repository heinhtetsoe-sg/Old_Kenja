<?php

require_once('for_php7.php');


class knjm312Query extends Query {

    function getSubclass($model) {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
	        $query .= "     T1.SUBCLASSNAME AS LABEL ";
        } else {
	        $query .= "     T1.SUBCLASSCD AS VALUE, ";
	        $query .= "     T1.SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getCheckSdate() {
        $query  = " SELECT ";
        $query .= "     SDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '9' ";

        return $query;
    }

    function getRepStandard($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $subclass_array = array();
	        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $query .= "    T1.CLASSCD, ";
	        $query .= "    T1.SCHOOL_KIND, ";
	        $query .= "    T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.STANDARD_SEQ, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.STANDARD_DATE, ";
        $query .= "     T1.DEADLINE_DATE ";
        $query .= " FROM ";
        $query .= "     REP_STANDARDDATE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $query .= "     AND T1.CLASSCD		 = '".$subclass_array[0]."'  ";
	        $query .= "     AND T1.SCHOOL_KIND   = '".$subclass_array[1]."' ";
	        $query .= "     AND T1.CURRICULUM_CD = '".$subclass_array[2]."' ";
	        $query .= "     AND T1.SUBCLASSCD    = '".$subclass_array[3]."' ";
        } else {
	        $query .= "     AND T1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "     AND T1.DEADLINE_DATE IS NOT NULL ";
        $query .= "     AND T1.DEADLINE_DATE <= '".str_replace("/", "-", $model->field["DEADLINE_DATE"])."' ";
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $query .= "    T1.CLASSCD, ";
	        $query .= "    T1.SCHOOL_KIND, ";
	        $query .= "    T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.STANDARD_SEQ ";

        return $query;
    }

    function getUpdateQuery($db, $model, $repStandard) {
        $query  = " INSERT INTO REP_PRESENT_DAT ( ";
        $query .= " SELECT ";
        $query .= "     '".CTRL_YEAR."', ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $query .= "     '{$repStandard["CLASSCD"]}', ";
	        $query .= "     '{$repStandard["SCHOOL_KIND"]}', ";
	        $query .= "     '{$repStandard["CURRICULUM_CD"]}', ";
        }
        $query .= "     '{$repStandard["SUBCLASSCD"]}', ";
        $query .= "     {$repStandard["STANDARD_SEQ"]}, ";
        $query .= "     0, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     '".CTRL_DATE."', ";
        $query .= "     current time, ";
        $query .= "     '{$repStandard["CHAIRCD"]}', ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     CAST(NULL AS VARCHAR(5)), ";
        $query .= "     '8', ";
        $query .= "     '".CTRL_DATE."', ";
        $query .= "     current time, ";
        $query .= "     CAST(NULL AS VARCHAR(1)), ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.CHAIRCD = '{$repStandard["CHAIRCD"]}' ";
        $query .= "     AND '{$repStandard["DEADLINE_DATE"]}' BETWEEN T1.APPDATE AND T1.APPENDDATE ";
        $query .= "     AND NOT EXISTS( ";
        $query .= "             SELECT ";
        $query .= "                 'x' ";
        $query .= "             FROM ";
        $query .= "                 REP_PRESENT_DAT E1 ";
        $query .= "             WHERE ";
        $query .= "                 E1.YEAR = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $query .= "                 AND E1.CLASSCD 		 = '{$repStandard["CLASSCD"]}' ";
	        $query .= "                 AND E1.SCHOOL_KIND 	 = '{$repStandard["SCHOOL_KIND"]}' ";
	        $query .= "                 AND E1.CURRICULUM_CD = '{$repStandard["CURRICULUM_CD"]}' ";
        }
        $query .= "                 AND E1.SUBCLASSCD = '{$repStandard["SUBCLASSCD"]}' ";
        $query .= "                 AND E1.STANDARD_SEQ = {$repStandard["STANDARD_SEQ"]} ";
        $query .= "                 AND E1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     ) ";
        $query .= " ) ";

        $db->query($query);

    }
}
?>
