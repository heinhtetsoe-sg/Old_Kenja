<?php

require_once('for_php7.php');

class knji092tQuery extends Query {

    //卒業年度取得
    function selectYear($model)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    t1.year ";
        $query .= "FROM ";
        $query .= "    schreg_regd_hdat t1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "ORDER BY ";
        $query .= "    t1.year desc";

        return $query;
    }

    //校種取得
    function getA023($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->field["YEAR"]."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }


    //学期制・学年数上限取得
    function selectGradeSemesterDiv($model)
    {
        $query  = "SELECT grade_hval, semesterdiv FROM school_mst WHERE year='".$model->field["YEAR"]."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        }

        return $query;
    }

    //中高一貫取得
    function getNameMst()
    {
        $query  = "SELECT NAMESPARE2 AS VAL FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";

        return $query;
    }

    //年組取得
    function getAuth($model, $opt_Grade) {
        if ($model->field["CLASS_MAJOR"] == "1") {
            $query  = " SELECT DISTINCT ";
            $query .= "     T1. GRADE || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
            if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
            }
            $query .= "     ,NAME_MST T2 ";
            if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= " INNER JOIN (SELECT DISTINCT SCHOOL_KIND FROM SCHREG_REGD_GDAT T6 WHERE T6.YEAR = '".$model->field["YEAR"]."' ";
                $query .= " AND T6.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."') T7 ON T7.SCHOOL_KIND = T2.NAME1 ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '".$model->field["YEAR"]."' AND ";
            $query .= "     T1.SEMESTER = '".$model->field["GAKKI"]."' AND ";
            $query .= "     T2.NAMECD1  = 'A023' AND ";
            $query .= "     T1.GRADE BETWEEN T2.NAMESPARE2 AND T2.NAMESPARE3 ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            $query  = " SELECT DISTINCT ";
            $query .= "     L1.COURSECD || L1.MAJORCD || ' ' || L3.MAJORNAME AS LABEL, ";
            $query .= "     L1.COURSECD || L1.MAJORCD AS VALUE ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
            if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T5 ON T5.YEAR = T1.YEAR AND T5.GRADE = T1.GRADE ";
                $query .= " AND T5.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
            }
            $query .= "     LEFT JOIN ";
            $query .= "         SCHREG_REGD_DAT L1 ON L1.YEAR       = T1.YEAR ";
            $query .= "                           AND L1.SEMESTER   = T1.SEMESTER ";
            $query .= "                           AND L1.GRADE      = T1.GRADE ";
            $query .= "     LEFT JOIN ";
            $query .= "         COURSE_MST L2 ON L2.COURSECD = L1.COURSECD ";
            $query .= "     LEFT JOIN ";
            $query .= "         MAJOR_MST L3 ON L3.COURSECD = L1.COURSECD ";
            $query .= "                     AND L3.MAJORCD  = L1.MAJORCD, ";
            $query .= "     NAME_MST T2 ";
            if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= " INNER JOIN (SELECT DISTINCT SCHOOL_KIND FROM SCHREG_REGD_GDAT T6 WHERE T6.YEAR = '".$model->field["YEAR"]."' ";
                $query .= " AND T6.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."') T7 ON T7.SCHOOL_KIND = T2.NAME1 ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '{$model->field["YEAR"]}' AND ";
            $query .= "     T1.SEMESTER = '{$model->field["GAKKI"]}' AND ";
            $query .= "     L1.MAJORCD IS NOT NULL AND ";
            $query .= "     T2.NAMECD1  = 'A023' AND ";
            $query .= "     T1.GRADE BETWEEN T2.NAMESPARE2 AND T2.NAMESPARE3 ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        return $query;
    }

    //近大判定
    function getCheckSchool()
    {
        $query  = "SELECT ";
        $query .= "     COUNT(*) ";
        $query .= "FROM ";
        $query .= "     NAME_MST ";
        $query .= "WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' AND ";
        $query .= "     (NAME1 = 'kindai' OR NAME1 = 'KINJUNIOR') ";

        return $query;
    }
}
?>
