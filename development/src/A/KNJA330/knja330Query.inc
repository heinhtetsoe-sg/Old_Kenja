<?php

require_once('for_php7.php');

class knja330Query extends Query {

    //対応者を取得
    function getGrade($model, $year, $semester, $final_date, $checkdiv = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE, ";
        $query .= "     G1.GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
        $query .= "          ON T1.YEAR         = G1.YEAR ";
        $query .= "         AND T1.GRADE        = G1.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "         AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "         AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";
        $query .= "     AND T1.SCHREGNO NOT IN (SELECT W1.SCHREGNO ";
        $query .= "                             FROM SCHREG_BASE_MST W1 ";
        $query .= "                             WHERE W1.GRD_DIV IN ('1','2','3') ";
        if ($checkdiv == "1") {
            $query .= "                                OR (W1.GRD_DIV = '4' AND VALUE(W1.GRD_DATE,'0001-01-01') <= '".$final_date."') ";
        }
        $query .= "                                ) ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE ";

        return $query;
    }

    //最高学年を取得
    function getMaxGrade($model, $year, $semester, $final_date, $checkdiv)
    {
        $query  = " SELECT ";
        $query .= "     MAX(GRADE) AS MAXGRADE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND SEMESTER = '".$semester."' ";
        $query .= "     AND SCHREGNO NOT IN (SELECT W1.SCHREGNO ";
        $query .= "                          FROM SCHREG_BASE_MST W1 ";
        $query .= "                          WHERE W1.GRD_DIV IN ('1','2','3') ";
        if ($checkdiv == "1") {
            $query .= "                                OR (W1.GRD_DIV = '4' AND VALUE(W1.GRD_DATE,'0001-01-01') <= '".$final_date."') ";
        }
        $query .= "                                ) ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     G1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                $query .= "                 ) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " GROUP BY ";
        $query .= "     GRADE ";
        $query .= " ORDER BY ";
        $query .= "     GRADE DESC ";

        return $query;
    }

    //備考を取得
    function getRemark($year, $year_add, $grade, $makediv, $final_date, $checkdiv)
    {
        $query  = " WITH NEXTYEAR_TBL AS ( ";
        $query .= " SELECT ";
        $query .= "     'tbljoin' AS JOINCD, ";
        $query .= "     CASE WHEN COUNT(*) > 0 THEN '次年度データあり' ELSE '' END AS REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year_add."' ";
        $query .= "     AND SEMESTER = '1' ";
        $query .= "     AND GRADE = '".sprintf("%02d",(int)$grade + 1)."' ";
        $query .= " ), YEAR_TBL AS (SELECT ";
        $query .= "     'tbljoin' AS JOINCD, ";
        $query .= "     CASE WHEN COUNT(*) = 0 THEN '今年度データなし' ELSE '' END AS REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND GRADE = '".$grade."' ";
        $query .= "     AND SCHREGNO NOT IN (SELECT W1.SCHREGNO ";
        $query .= "                          FROM SCHREG_BASE_MST W1 ";
        $query .= "                          WHERE W1.GRD_DIV IN ('1','2','3') ";
        if ($checkdiv == "1") {
            $query .= "                                OR (W1.GRD_DIV = '4' AND VALUE(W1.GRD_DATE,'0001-01-01') <= '".$final_date."') ";
        }
        $query .= "                                ) ";
        $query .= " ) ";

        // 次年度の学年判定
        $query .= ", NEXTGRADE_TBL AS ( ";
        $query .= " SELECT ";
        $query .= "     'tbljoin' AS JOINCD, ";
        $query .= "     CASE WHEN COUNT(*) = 0 THEN '次学年なし' ELSE '' END AS REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year_add."' ";
        $query .= "     AND GRADE = '".sprintf("%02d",(int)$grade + 1)."' ";
        $query .= " ) ";

        // 最終学年判定
        $query .= ", LASTGRADE AS ( ";
        $query .= "   SELECT ";
        $query .= "     YEAR ";
        $query .= "     , MAX(GRADE) GRADE ";
        $query .= "     , SCHOOL_KIND ";
        $query .= "   FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= "   GROUP BY ";
        $query .= "     YEAR ";
        $query .= "     , SCHOOL_KIND ";
        $query .= " ) ";
        $query .= ", LASTGRADE_TBL AS ( ";
        $query .= " SELECT ";
        $query .= "     'tbljoin' AS JOINCD, ";
        $query .= "     CASE WHEN COUNT(*) > 0 THEN '最終学年' ELSE '' END AS REMARK ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_GDAT REGDG ";
        $query .= "   INNER JOIN LASTGRADE ";
        $query .= "     ON REGDG.YEAR = LASTGRADE.YEAR ";
        $query .= "     AND REGDG.GRADE = LASTGRADE.GRADE ";
        $query .= "     AND REGDG.SCHOOL_KIND = LASTGRADE.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     REGDG.YEAR = '".$year."' ";
        $query .= "     AND REGDG.GRADE = '".sprintf("%02d", $grade)."' ";
        $query .= " ) ";

        $query .= " SELECT ";
        if ($makediv == "ADD") {
            $query .= "     CASE WHEN T1.REMARK = '' AND T2.REMARK = '' AND T3.REMARK = '' ";
            $query .= "          THEN '' ";
            $query .= "          ELSE CASE WHEN T1.REMARK != '' ";
            $query .= "                    THEN T1.REMARK ";
            $query .= "                    WHEN T3.REMARK != '' ";
            $query .= "                    THEN T3.REMARK ";
            $query .= "                    WHEN T4.REMARK != '' ";
            $query .= "                    THEN T4.REMARK ";
            $query .= "                    ELSE T2.REMARK END ";
            $query .= "     END AS REMARK ";
            $query .= " FROM ";
            $query .= "     YEAR_TBL T1 ";
            $query .= "     LEFT JOIN NEXTYEAR_TBL T2 ON T1.JOINCD = T2.JOINCD ";
            $query .= "     LEFT JOIN NEXTGRADE_TBL T3 ON T1.JOINCD = T3.JOINCD ";
            $query .= "     LEFT JOIN LASTGRADE_TBL T4 ON T1.JOINCD = T4.JOINCD ";
        } else {
            $query .= "     REMARK ";
            $query .= " FROM ";
            $query .= "     YEAR_TBL T1 ";
        }

        return $query;
    }

    //テーブルのフィールド取得
    function getFieldQuery($table, $year)
    {
        $query  = " SELECT * FROM $table WHERE YEAR = '".$year."' ";
        return $query;
    }

    //CONTROL_MSTの年度取得
    function getCtrl()
    {
        $query  = " SELECT CTRL_YEAR FROM CONTROL_MST ";
        return $query;
    }

    //INSERT
    function getInsertQuery($year, $year_add, $grade, $table, $field, $final_date, $checkdiv)
    {
        $query  = " INSERT INTO $table ";
        $query .= " SELECT ";
        $query .= "     $field ";
        $query .= " FROM ";
        $query .= "     $table ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND GRADE = '".$grade."' ";
        if ($table == "SCHREG_REGD_DAT") {
            $query .= "     AND SCHREGNO NOT IN (SELECT W1.SCHREGNO ";
            $query .= "                          FROM SCHREG_BASE_MST W1 ";
            $query .= "                          WHERE W1.GRD_DIV IN ('1','2','3') ";
            if ($checkdiv == "1") {
                $query .= "                                OR (W1.GRD_DIV = '4' AND VALUE(W1.GRD_DATE,'0001-01-01') <= '".$final_date."') ";
            }
            $query .= "                                ) ";
            $query .= "     AND SCHREGNO NOT IN (SELECT ";
            $query .= "                             W2.SCHREGNO ";
            $query .= "                          FROM ";
            $query .= "                             SCHREG_REGD_DAT W2 ";
            $query .= "                          WHERE ";
            $query .= "                             W2.YEAR     = '".$year_add."' AND ";
            $query .= "                             W2.SEMESTER = '1' ";
            $query .= "                         ) ";
        }
        return $query;
    }

    //DELETE
    function getDeleteQuery($year, $grade, $table)
    {
        $query  = " DELETE FROM $table ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND GRADE = '".$grade."' ";
        return $query;
    }

}
?>
