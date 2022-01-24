<?php

require_once('for_php7.php');

class knjb061Query extends Query {
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
        $query .="         T1.YEAR     = '" .CTRL_YEAR."' ";
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

    //年組取得
    function getHrclass($model) {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND GRADE IN (SELECT ";
                $query .= "                      GRADE ";
                $query .= "                   FROM ";
                $query .= "                      SCHREG_REGD_GDAT ";
                $query .= "                   WHERE ";
                $query .= "                          YEAR = '".CTRL_YEAR."' ";
                $query .= "                      AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                $query .= "                   ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND GRADE IN (SELECT ";
            $query .= "                      GRADE ";
            $query .= "                   FROM ";
            $query .= "                      SCHREG_REGD_GDAT ";
            $query .= "                   WHERE ";
            $query .= "                          YEAR        = '".CTRL_YEAR."' ";
            $query .= "                      AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "                   ) ";
        }

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
