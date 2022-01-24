<?php

require_once('for_php7.php');

class knjc110bQuery extends Query {

    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";
        $query .= "     AND SEMESTER <> '9' ";

        return $query;

    }
    //年組取得（権限チェック）
    function getAuth($year,$semester)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = "SELECT GRADE || HR_CLASS AS VALUE, HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .$year ."'";
            $query .= "AND SEMESTER='".$semester ."'";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = "SELECT GRADE || HR_CLASS AS VALUE ,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .$year ."' ";
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "    OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "    OR TR_CD3 = '" .STAFFCD ."') ";
            $query .= "AND SEMESTER='".$semester ."'";
        }
        return $query;
    }

    //学校区分を取得
    function getSchooldiv()
    {
        $query  = "SELECT SCHOOLDIV ";
        $query .= "FROM SCHOOL_MST ";
        $query .= "WHERE YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //生徒取得
    function getStudent($year, $hrclass, $seme) {
        $query  = " SELECT ";
        $query .= "     T2.SCHREGNO AS VALUE, ";
        $query .= "     T2.SCHREGNO || '　' || T2.ATTENDNO || '番' || '　' || T1.NAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 INNER JOIN SCHREG_REGD_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR                 = '{$year}' AND ";
        $query .= "     T2.GRADE || T2.HR_CLASS = '{$hrclass}' ";
        $query .= "AND     T2.SEMESTER      = '{$seme}' ";
        $query .= " ORDER BY ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }

    //学期開始日と終了日の取得
    function getSEdate($semester, $schooldiv) {
        $query .= " SELECT ";
        $query .= "     CHAR(T2.SDATE) || ',' || CHAR(T2.EDATE) AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST T1 LEFT JOIN SEMESTER_MST T2 ON T1.YEAR = T2.YEAR ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '" . CTRL_YEAR . "' AND ";
        if ($schooldiv == 0) {
            $query .= "     T2.SEMESTER = '{$semester}' ";
        } else {
            $query .= "     T2.SEMESTER = '9' ";
        }
        
        return $query;
    }
}
?>
