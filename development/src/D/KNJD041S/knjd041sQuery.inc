<?php

require_once('for_php7.php');

class knjd041sQuery extends Query {
    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //１レコード取得 ※未使用
    function getRow($cd) {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM subclass_mst ".
                 " WHERE SUBCLASSCD = '" .$cd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //テスト種別取得
    function getTestItem($model) {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD AS VALUE, ";
        $query .= "     TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getClassData($model) {
        $query  = "SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND AS VALUE, ";
        } else {
	        $query .= "		substr(T1.SUBCLASSCD,1,2) AS VALUE, ";
        }
        $query .= "		T2.CLASSNAME AS LABEL ";
        $query .= " FROM chair_dat T1 INNER JOIN class_mst T2";
        $query .= "                         ON substr(T1.SUBCLASSCD,1,2)=T2.CLASSCD";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                        AND T1.SCHOOL_KIND = T2.SCHOOL_KIND";
        }
        $query .= " WHERE ((T1.YEAR='".CTRL_YEAR."')";
        if ($model->field["SEMESTER"] != 9 ) {
            $query .= " AND (T1.SEMESTER='".$model->field["SEMESTER"]."')";
        }
        $query .= " AND (substr(T1.SUBCLASSCD,1,2) < '"."90"."'))";
        $query .= " order by ";
        $query .= " 	VALUE ";
        return $query;
    }

    //学校名取得の取得(
    function getSchoolName() {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }
}
?>
