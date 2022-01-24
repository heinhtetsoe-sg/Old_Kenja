<?php

require_once('for_php7.php');

class knji093Query extends Query {

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //卒業年度取得
    function getYear() {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";

        return $query;
    }

    //学期制・学年数上限取得
    function selectGradeSemesterDiv($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTERDIV ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' ";

        return $query;
    }

    //年組取得
    function getAuth($model) {
        if ($model->field["CLASS_MAJOR"] == '2') {
            $query  = " SELECT DISTINCT ";
            $query .= "     L1.COURSECD || L1.MAJORCD || ' ' || value(L3.MAJORNAME, '') AS LABEL, ";
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
        } else {
            $query   = "  SELECT DISTINCT ";
            $query  .= "    T1.GRADE || T1.HR_CLASS AS VALUE, ";
            $query  .= "    T1.HR_NAME AS LABEL ";
            $query  .= "  FROM ";
            $query  .= "    SCHREG_REGD_HDAT T1 ";
            $query  .= "    INNER JOIN V_NAME_MST T2 ";
            $query  .= "      ON T1.YEAR = T2.YEAR ";
            $query  .= "    INNER JOIN SCHREG_REGD_GDAT T3 ";
            $query  .= "      ON T1.YEAR = T3.YEAR ";
            $query  .= "      AND T1.GRADE = T3.GRADE ";
            $query  .= "  WHERE ";
            $query  .= "    T1.YEAR = '".$model->field["YEAR"]."' ";
            $query  .= "    AND T1.SEMESTER = '".$model->field["GAKKI"]."' ";
            $query  .= "    AND T2.NAMECD1 = 'A023' ";
            $query  .= "    AND T1.GRADE BETWEEN T2.NAMESPARE2 AND T2.NAMESPARE3 ";
            if ($model->Properties["useSchool_KindField"] == "1") {
                $query  .= "    AND T3.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
            }
            $query  .= "  ORDER BY ";
            $query  .= "    VALUE ";
        }

        return $query;
    }
}
?>
