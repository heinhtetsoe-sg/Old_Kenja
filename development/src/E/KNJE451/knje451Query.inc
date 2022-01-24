<?php

require_once('for_php7.php');

class knje451Query extends Query {
    //年組コンボ
    function getHrclass($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || '-' || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
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
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象生徒
    function getStudents($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.SCHREGNO || '　' || T1.ATTENDNO || '番　' || T2.NAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_BASE_YEAR_DETAIL_MST S1 ";
        $query .= "          ON S1.YEAR         = T1.YEAR ";
        $query .= "         AND S1.SCHREGNO     = T1.SCHREGNO ";
        $query .= "         AND S1.BASE_SEQ     = '004' ";
        $query .= "     INNER JOIN NAME_MST S2 ";
        $query .= "          ON S2.NAMECD1      = 'A025' ";
        $query .= "         AND S2.NAMECD2      = S1.BASE_REMARK1 ";
        $query .= "         AND S2.NAMESPARE2   = '1' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";
        
        return $query;
    }

    //対象職員
    function GetStaffcd() {
        $query .= " SELECT ";
        $query .= "     STAFFCD || ':' || STAFFNAME AS LABEL, ";
        $query .= "     STAFFCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
}
?>
