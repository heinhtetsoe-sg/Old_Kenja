<?php

require_once('for_php7.php');

class knji040kQuery extends Query {
    //学校区分抽出
    function GetSchoolDiv($year) {
        $query  = " SELECT ";
        $query .= "     SCHOOLDIV";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."'";

        return $query;
    }

    function GetYear() {
        $query .= " WITH THIS_YEAR(GRADUATE_YEAR) AS ( ";
        $query .= "     VALUES('".CTRL_YEAR."') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     GRADUATE_YEAR AS VALUE, ";
        $query .= "     GRADUATE_YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     THIS_YEAR ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     FISCALYEAR(GRD_DATE) AS VALUE, ";
        $query .= "     FISCALYEAR(GRD_DATE) AS LABEL ";
        $query .= " FROM ";
        $query .= "     GRD_BASE_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //卒業時組取得
    function getHrclass($model, $g_year) {

        if ($g_year == CTRL_YEAR) {
            $query .= " SELECT DISTINCT ";
            $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.GRADE || '-' || T1.HR_CLASS || '：' || T1.HR_NAME AS LABEL";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "INNER JOIN SCHREG_REGD_GDAT T6 ON T6.YEAR = T1.YEAR AND T6.GRADE = T1.GRADE ";
                    $query .= " AND T6.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T6 ON T6.YEAR = T1.YEAR AND T6.GRADE = T1.GRADE ";
                $query .= " AND T6.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '{$g_year}' ";
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     AND T1.GRADE = '03' ";
            $query .= " ORDER BY ";
            $query .= "     VALUE, ";
            $query .= "     LABEL ";
        } else {
            $query  = " WITH HR_CLASS AS ( ";
            $query .= "     SELECT ";
            $query .= "         YEAR, ";
            $query .= "         SEMESTER, ";
            $query .= "         GRADE, ";
            $query .= "         HR_CLASS ";
            $query .= "     FROM ";
            $query .= "         V_GRD_REGDYEAR_GRADE_DAT ";
            $query .= "     UNION ";
            $query .= "     SELECT ";
            $query .= "         YEAR, ";
            $query .= "         SEMESTER, ";
            $query .= "         GRADE, ";
            $query .= "         HR_CLASS ";
            $query .= "     FROM ";
            $query .= "         V_GRD_REGDYEAR_UNIT_DAT ";
            $query .= " ) ";

            $query .= " SELECT DISTINCT ";
            $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.GRADE || '-' || T1.HR_CLASS || '：' || T2.HR_NAME AS LABEL";
            $query .= " FROM ";
            $query .= "     HR_CLASS T1, ";
            $query .= "     GRD_REGD_HDAT T2 ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "INNER JOIN SCHREG_REGD_GDAT T6 ON T6.YEAR = T2.YEAR AND T6.GRADE = T2.GRADE ";
                    $query .= " AND T6.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T6 ON T6.YEAR = T2.YEAR AND T6.GRADE = T2.GRADE ";
                $query .= " AND T6.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR = T2.YEAR ";
            $query .= "     AND T1.YEAR = '".$g_year."' ";
            $query .= "     AND T1.GRADE = '03' ";
            $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
            $query .= "     AND T1.GRADE    = T2.GRADE ";
            $query .= "     AND T1.HR_CLASS = T2.HR_CLASS ";
            $query .= " ORDER BY ";
            $query .= "     VALUE, ";
            $query .= "     LABEL ";
        }

        return $query;
    }

    function SearchStudent($model,$fields,$schooldiv) {
        if ($fields["graduate_year"] == CTRL_YEAR) {
            $query .= " WITH REGD_MAX AS ( ";
            $query .= " SELECT ";
            $query .= "     REGD.YEAR, ";
            $query .= "     MAX(REGD.SEMESTER) AS SEMESTER, ";
            $query .= "     REGD.SCHREGNO ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT REGD ";
            $query .= " WHERE ";
            $query .= "     REGD.YEAR = '{$fields["graduate_year"]}' ";
            $query .= "     AND REGD.GRADE = '03' ";
            $query .= " GROUP BY ";
            $query .= "     REGD.YEAR, ";
            $query .= "     REGD.SCHREGNO ";
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     REGD.YEAR AS GRADUATEYEAR, ";
            $query .= "     REGD.SEMESTER, ";
            $query .= "     REGD.GRADE, ";
            $query .= "     REGD.HR_CLASS, ";
            $query .= "     REGD.ATTENDNO, ";
            $query .= "     REGH.HR_NAME, ";
            $query .= "     REGD.SCHREGNO, ";
            $query .= "     BASE.NAME, ";
            $query .= "     BASE.NAME_KANA as KANA, ";
            $query .= "     BASE.SEX as SEXNUM, ";
            $query .= "     CASE BASE.SEX WHEN Z002.NAMECD2 THEN Z002.NAME2 ELSE BASE.SEX END as SEX, ";
            $query .= "     BASE.BIRTHDAY ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT REGD ";
            $query .= "     LEFT JOIN SCHREG_REGD_HDAT REGH ON REGD.YEAR = REGH.YEAR ";
            $query .= "          AND REGD.SEMESTER = REGH.SEMESTER ";
            $query .= "          AND REGD.GRADE = REGH.GRADE ";
            $query .= "          AND REGD.HR_CLASS = REGH.HR_CLASS ";
            $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
            $query .= "     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ";
            $query .= "          AND BASE.SEX = Z002.NAMECD2, ";
            $query .= "     REGD_MAX ";
            $query .= " WHERE ";
            $query .= "     REGD.YEAR = REGD_MAX.YEAR ";
            $query .= "     AND REGD.SEMESTER = REGD_MAX.SEMESTER ";
            $query .= "     AND REGD.GRADE = '03' ";
            $query .= "     AND REGD.SCHREGNO = REGD_MAX.SCHREGNO ";
            if (strlen($fields["graduate_class"])) {
                $query .= "     AND REGD.GRADE || '-' || REGD.HR_CLASS   = '{$fields["graduate_class"]}' ";
            }
            if (strlen($fields["lkanji"])) {
                $query .= "     AND BASE.NAME LIKE '%{$fields["lkanji"]}%' ";
            }
            if (strlen($fields["lkana"])) {
                $query .= "     AND BASE.NAME_KANA LIKE '%{$fields["lkana"]}%' ";
            }
            $query .= " ORDER BY ";
            $query .= "     REGD.GRADE, ";
            $query .= "     REGD.HR_CLASS, ";
            $query .= "     REGD.ATTENDNO ";
        } else {
            $query  = " SELECT ";
            $query .= "     REGD.GRADE, ";
            $query .= "     REGD.HR_CLASS, ";
            $query .= "     REGD.ATTENDNO, ";
            $query .= "     REGH.HR_NAME, ";
            $query .= "     REGD.SEMESTER, ";
            $query .= "     BASE.NAME , ";
            $query .= "     REGD.YEAR AS GRADUATEYEAR, ";
            $query .= "     BASE.NAME_KANA AS KANA, ";
            $query .= "     BASE.SEX AS SEXNUM, ";
            $query .= "     CASE BASE.SEX WHEN Z002.NAMECD2 THEN Z002.NAME2 ELSE BASE.SEX END AS SEX, ";
            $query .= "     BASE.BIRTHDAY, ";
            $query .= "     BASE.SCHREGNO ";
            $query .= " FROM ";
            $query .= "     GRD_BASE_MST BASE ";
            $query .= "     LEFT OUTER JOIN ";
            $query .= "         (SELECT NAME2, NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'Z002') Z002 ON BASE.SEX = Z002.NAMECD2 ";
            if($schooldiv=="0") {//学年制
                $query .= "      ,V_GRD_REGDYEAR_GRADE_DAT REGD ";
            }
            if($schooldiv=="1") {//単位制
                $query .= "      ,V_GRD_REGDYEAR_UNIT_DAT REGD ";
            }
            $query .= "      ,GRD_REGD_HDAT REGH ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " INNER JOIN SCHREG_REGD_GDAT T6 ON T6.YEAR = REGH.YEAR AND T6.GRADE = REGH.GRADE ";
                    $query .= " AND T6.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " INNER JOIN SCHREG_REGD_GDAT T6 ON T6.YEAR = REGH.YEAR AND T6.GRADE = REGH.GRADE ";
                $query .= " AND T6.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " WHERE BASE.SCHREGNO = REGD.SCHREGNO";
            $query .= "   AND FISCALYEAR(BASE.GRD_DATE) = REGD.YEAR";
            $query .= "   AND REGD.GRADE = '03' ";
            $query .= "   AND REGH.YEAR = REGD.YEAR";
            $query .= "   AND REGH.SEMESTER = REGD.SEMESTER";
            $query .= "   AND REGH.GRADE = REGD.GRADE";
            $query .= "   AND REGH.HR_CLASS = REGD.HR_CLASS";
            if (strlen($fields["graduate_year"])) {
                $query .= "   AND REGD.YEAR       = '".$fields["graduate_year"]."'";
            }
            if (strlen($fields["graduate_class"])) {
                $query .= "   AND REGD.GRADE || '-' || REGD.HR_CLASS   = '".$fields["graduate_class"]."'";
            }
            if (strlen($fields["lkanji"])) {
                $query .= "   AND BASE.NAME_SHOW LIKE '%".$fields["lkanji"]."%'";
            }
            if (strlen($fields["lkana"])) {
                $query .= "   AND BASE.NAME_KANA LIKE '%".$fields["lkana"]."%'";
            }
            $query .= " ORDER BY ";
            $query .= "     GRADUATEYEAR, ";
            $query .= "     REGD.GRADE, ";
            $query .= "     REGD.HR_CLASS, ";
            $query .= "     REGD.ATTENDNO";
        }

        return $query;
    }

}
?>
