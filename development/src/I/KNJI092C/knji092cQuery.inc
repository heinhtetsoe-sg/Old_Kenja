<?php

require_once('for_php7.php');

class knji092cQuery extends Query
{

    //卒業年度取得
    public function selectYear($model)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    t1.year ";
        $query .= "FROM ";
        $query .= "    schreg_regd_hdat t1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "ORDER BY ";
        $query .= "    t1.year desc";

        return $query;
    }

    //校種取得
    public function getA023($model)
    {
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
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }


    //学期制・学年数上限取得
    public function selectGradeSemesterDiv($model)
    {
        $query  = "SELECT grade_hval, semesterdiv FROM school_mst WHERE year='".$model->field["YEAR"]."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        }

        return $query;
    }

    //中高一貫取得
    public function getNameMst()
    {
        $query  = "SELECT NAMESPARE2 AS VAL FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";

        return $query;
    }

    //年組取得
    public function getAuth($model, $opt_Grade)
    {
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
    public function getCheckSchool()
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

    //一覧表示
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "   T2.GRD_NO, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.GRADE, ";
        $query .= "   T1.HR_CLASS, ";
        $query .= "   T1.ATTENDNO, ";
        $query .= "   T2.NAME, ";
        $query .= "   T2.REAL_NAME, ";
        $query .= "   T2.BIRTHDAY, ";
        $query .= "   S1.GRADUATE_DATE, ";
        $query .= "   C1.PRINCIPAL_NAME, ";
        $query .= "   R1.GRADE_RANK ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= "   INNER JOIN SCHREG_BASE_MST T2  ";
        $query .= "      ON T2.SCHREGNO = T1.SCHREGNO ";
        if ($model->field["MIKOMI"] != "on") {
            $query .= "     AND T2.GRD_DIV  <> '4' ";
        }
        $query .= "   LEFT JOIN SCHOOL_MST S1  ";
        $query .= "     ON S1.YEAR        = T1.YEAR ";
        $query .= "    AND S1.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "    AND S1.SCHOOLCD    = '000000000000' ";
        $query .= "   LEFT JOIN CERTIF_SCHOOL_DAT C1  ";
        $query .= "      ON C1.YEAR          = T1.YEAR ";
        $query .= "     AND C1.CERTIF_KINDCD = '001' ";
        $query .= "   LEFT JOIN RECORD_RANK_SDIV_DAT R1  ";
        $query .= "      ON R1.YEAR = T1.YEAR ";
        $query .= "     AND R1.SEMESTER || R1.TESTKINDCD || R1.TESTITEMCD || R1.SCORE_DIV = '9990008' ";
        $query .= "     AND R1.CLASSCD || R1.SCHOOL_KIND || R1.CURRICULUM_CD || R1.SUBCLASSCD = '99".$model->field["SCHOOL_KIND"]."99999999' ";
        $query .= "     AND R1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "       T1.YEAR = '".$model->field["YEAR"]."' ";
        $query .= "   AND T1.GRADE || T1.HR_CLASS IN ('".implode($model->select_data["selectdata"], "','")."') ";
        $query .= "   AND T1.SEMESTER = '".$model->field["GAKKI"]."' ";
        $query .= " ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ";
        return $query;
    }
}
