<?php

require_once('for_php7.php');

class knjb1231Query extends Query {

    //処理年度
    function getExeYear($model)
    {
        $query  = " WITH EXE_YEAR (LABEL, VALUE) AS ( ";
        if ($model->field["SEARCH_DIV"] == "1") {
            $query .="     VALUES (".(CTRL_YEAR + 1).",".(CTRL_YEAR + 1).")";
        } else {
            $query .="     VALUES (".CTRL_YEAR.",".CTRL_YEAR."), (".(CTRL_YEAR + 1).",".(CTRL_YEAR + 1).")";
        }
        $query .="     ) ";
        $query .=" SELECT ";
        $query .="     * ";
        $query .=" FROM ";
        $query .="     EXE_YEAR ";
        $query .=" ORDER BY ";
        $query .="     VALUE DESC ";

        return $query;
    }

    //年組取得（権限チェック） 
    function getAuth($model, $year, $semester) {
        $query .= " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE  ";
        $query .= "     T1.YEAR         = '".$year."' ";
        $query .= " AND T1.SEMESTER     = '".$semester."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= " AND (   TR_CD1 = '".STAFFCD."' ";
            $query .= "      OR TR_CD2 = '".STAFFCD."' ";
            $query .= "      OR TR_CD3 = '".STAFFCD."' ";
            $query .= "      OR SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "      OR SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "      OR SUBTR_CD3 = '".STAFFCD."') ";
        }

        return $query;
    }


    //生徒取得
    function getSchno($model,$year,$semester) {
        $query .= "SELECT ";
        // $query .= "     T3.HR_NAME || '　' || T1.ATTENDNO || '番　' || T2.NAME_SHOW AS LABEL, ";
        $query .= "     T1.SCHREGNO || '　' || T2.NAME_SHOW AS LABEL, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.NAME_SHOW, ";
        $query .= "     T1.GRADE||T1.HR_CLASS AS GRADE_HR_CLASS, ";
        $query .= "     T1.ATTENDNO AS ATTENDNO, ";
        $query .= "     T3.HR_NAME AS HR_NAME ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT T1 ";
        $query .= "    INNER JOIN SCHREG_BASE_MST T2 ";
        $query .= "           ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "    LEFT JOIN SCHREG_REGD_HDAT T3 ";
        $query .= "           ON T3.YEAR     = T1.YEAR ";
        $query .= "          AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "          AND T3.GRADE    = T1.GRADE ";
        $query .= "          AND T3.HR_CLASS = T1.HR_CLASS ";
        $query .= "WHERE  T1.YEAR       = '".$year."' AND ";
        $query .= "       T1.SEMESTER   = '".$semester."' AND ";
        $query .= "       T1.GRADE||T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "ORDER BY ATTENDNO ";

        return $query;
    }

    //Z010 名称1
    function getZ010Name1() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //生徒項目名取得
    function getSchName($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     REMARK1, ";
        $query .= "     SCHOOLCD ";
        $query .= " FROM ";
        $query .= "     SETTING_DAT ";
        $query .= " WHERE ";
        $query .= "     SEQ = '001' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= " AND SCHOOL_KIND IN (SELECT ";
                $query .= "                         SCHOOL_KIND ";
                $query .= "                     FROM ";
                $query .= "                         SCHREG_REGD_GDAT ";
                $query .= "                     WHERE ";
                $query .= "                         YEAR = '".CTRL_YEAR."' AND ";
                $query .= "                         GRADE = SUBSTR('".$model->field["GRADE_HR_CLASS"]."',1,2) ";
                $query .= "                     ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else {
            $query .= " AND SCHOOL_KIND IN (SELECT ";
            $query .= "                         SCHOOL_KIND ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT ";
            $query .= "                     WHERE ";
            $query .= "                         YEAR = '".CTRL_YEAR."' AND ";
            $query .= "                         GRADE = SUBSTR('".$model->field["GRADE_HR_CLASS"]."',1,2) ";
            $query .= "                     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOLCD ";

        return $query;
    }
}
?>
