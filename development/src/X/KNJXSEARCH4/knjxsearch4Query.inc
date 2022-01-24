<?php

require_once('for_php7.php');

class knjxsearch4Query extends Query {
    //学校区分抽出
    function GetSchoolDiv($year) {
        $query  = "select schooldiv";
        $query .= "  from school_mst";
        $query .= " where year = '".$year."'";
        return $query;
    }

    function GetYear() {
        $query  = "select distinct Fiscalyear(grd_date) as graduate_year";
        $query .= "  from grd_base_mst";
        $query .= " order by graduate_year desc";
        return $query;
    }

    //卒業時組取得
    function getHrclass($model, $g_year) {
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
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".$g_year."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.HR_CLASS = T2.HR_CLASS ";
        $query .= " ORDER BY ";
        $query .= "     VALUE, ";
        $query .= "     LABEL ";

        return $query;
    }

    function SearchStudent($model,$fields,$schooldiv) {
        $query  = "select t3.grade";
        $query .= "      ,t3.hr_class";
        $query .= "      ,t3.attendno";
        $query .= "      ,t5.hr_name";
        $query .= "      ,t3.semester";
        $query .= "      ,t1.name_show as name";
        $query .= "      ,t3.year as graduateyear";
        $query .= "      ,t1.name_kana as kana";
        $query .= "      ,t1.sex as sexnum";
        $query .= "      ,CASE t1.sex WHEN t4.namecd2 THEN t4.name2 ELSE t1.sex END as sex";
        $query .= "      ,t1.birthday";
        $query .= "      ,t1.schregno";
        $query .= "  from grd_base_mst t1 LEFT OUTER JOIN ";
        $query .= "      (select name2, namecd2 from name_mst where namecd1 = 'Z002') t4 on t1.sex = t4.namecd2";
        if($schooldiv=="0") {//学年制
            $query .= "      ,v_grd_regdyear_grade_dat t3";
        }
        if($schooldiv=="1") {//単位制
            $query .= "      ,v_grd_regdyear_unit_dat t3";
        }
        $query .= "      ,grd_regd_hdat t5";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT T6 ON T6.YEAR = T5.YEAR AND T6.GRADE = T5.GRADE ";
                $query .= " AND T6.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T6 ON T6.YEAR = T5.YEAR AND T6.GRADE = T5.GRADE ";
            $query .= " AND T6.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " where t1.schregno = t3.schregno";
        $query .= "   and Fiscalyear(t1.grd_date) = t3.year";
        $query .= "   and t5.year = t3.year";
        $query .= "   and t5.semester = t3.semester";
        $query .= "   and t5.grade = t3.grade";
        $query .= "   and t5.hr_class = t3.hr_class";
        if (strlen($fields["graduate_year"])) {
            $query .= "   and t3.year       = '".$fields["graduate_year"]."'";
        }
        if (strlen($fields["graduate_class"])) {
            $query .= "   and t3.grade || '-' || t3.hr_class   = '".$fields["graduate_class"]."'";
        }
        if (strlen($fields["lkanji"])) {
            $query .= "   and t1.name_show like '%".$fields["lkanji"]."%'";
        }
        if (strlen($fields["lkana"])) {
            $query .= "   and t1.name_kana like '%".$fields["lkana"]."%'";
        }
        $query .= " ORDER BY graduateyear,t3.grade,t3.hr_class,t3.attendno";

        return $query;
    }
}
?>
