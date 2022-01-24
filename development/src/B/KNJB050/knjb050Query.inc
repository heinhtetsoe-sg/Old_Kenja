<?php

require_once('for_php7.php');

class knjb050Query extends Query {
    //時間割パターンヘッダクエリ
    function getBscHdQuery($model) {
        $query  =" SELECT ";
        $query .="     T1.YEAR, ";
        $query .="     T1.SEMESTER, ";
        $query .="     T2.SEMESTERNAME, ";
        $query .="     T1.BSCSEQ, ";
        $query .="     T1.TITLE ";
        $query .=" FROM ";
        $query .="     SCH_PTRN_HDAT T1, ";
        $query .="     SEMESTER_MST T2 ";
        $query .=" WHERE ";
        $query .="         T1.YEAR     = '" .$model->control["年度"] ."' ";
        $query .="     AND T1.YEAR     = T2.YEAR ";
        $query .="     AND T1.SEMESTER = T2.SEMESTER ";
        $query .=" ORDER BY ";
        $query .="     T1.SEMESTER, ";
        $query .="     T1.BSCSEQ ";

        return $query;
    }

    //所属クエリ
    function getSectQuery() {
        $query  = " SELECT ";
        $query .= "     SECTIONCD AS VALUE, ";
        $query .= "     SECTIONCD || '　' || SECTIONNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SECTION_MST ";
        $query .= " ORDER BY ";
        $query .= "     SECTIONCD ";

        return $query;
    }

    //施設を取得
    function getFacility() {
        $query  = " SELECT ";
        $query .= "     FACCD AS VALUE, ";
        $query .= "     FACCD || '　' || FACILITYNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_FACILITY_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     FACCD ";

        return $query;
    }
}
?>
