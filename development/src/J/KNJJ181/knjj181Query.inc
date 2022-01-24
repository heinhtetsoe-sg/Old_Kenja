<?php

require_once('for_php7.php');
class knjj181Query extends Query {

    //対応者を取得
    function getkekka($year, $year_add, $table) {
        $query  = " WITH NEXTYEAR_TBL AS ( ";
        $query .= " SELECT ";
        $query .= "     'tbljoin' AS JOINCD, ";
        $query .= "     CASE WHEN COUNT(*) > 0 THEN '次年度データあり' ELSE '' END AS KEKKA ";
        $query .= " FROM ";
        $query .= "     $table ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year_add."' ";
        $query .= " ), YEAR_TBL AS (SELECT ";
        $query .= "     'tbljoin' AS JOINCD, ";
        $query .= "     CASE WHEN COUNT(*) = 0 THEN '今年度データなし' ELSE '' END AS KEKKA ";
        $query .= " FROM ";
        $query .= "     $table ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     CASE WHEN T1.KEKKA = '' AND T2.KEKKA = '' ";
        $query .= "          THEN '' ";
        $query .= "          ELSE CASE WHEN T1.KEKKA != '' ";
        $query .= "                    THEN T1.KEKKA ";
        $query .= "                    ELSE T2.KEKKA END ";
        $query .= "     END AS KEKKA ";
        $query .= " FROM ";
        $query .= "     YEAR_TBL T1 ";
        $query .= "     LEFT JOIN NEXTYEAR_TBL T2 ON T1.JOINCD = T2.JOINCD ";

        return $query;
    }

    function getFieldQuery($table, $year) {
        $query  = " SELECT * FROM $table WHERE YEAR = '".$year."' ";
        return $query;
    }

    function getInsertQuery($year, $year_add, $table, $Row, $model) {
        $field = "";
        $sep = "";
        foreach ($Row as $key => $val) {
            $v = $key;
            if ($key == "YEAR") {
                $v = "'$this->year_add'";
            } else if ($key == "REGISTERCD") {
                $v = "'".STAFFCD."'";
            } else if ($key == "UPDATED") {
                $v = "SYSDATE()";
            }
            if ($table == "SCHREG_BRANCH_DAT") {
                if ($key == "SCHOOL_KIND") {
                    $v = "REGD_G.SCHOOL_KIND";
                }
            }
            if ($v == $key) {
                $v = "T1.".$v;
            }
            $field .= $sep.$v;
            $sep = ",";
        }

        $query  = " INSERT INTO $table ";
        $query .= " SELECT ";
        $query .= "     $field ";
        $query .= " FROM ";
        $query .= "     $table T1 ";
        if ($table == "SCHREG_BRANCH_DAT") {
            $query .= " INNER JOIN (SELECT SCHREGNO, YEAR, MAX(SEMESTER) AS SEMESTER ";
            $query .= "             FROM SCHREG_REGD_DAT REGD ";
            $query .= "             GROUP BY SCHREGNO, YEAR) A1 ON A1.SCHREGNO = T1.SCHREGNO AND A1.YEAR = '".$year_add."' ";
            $query .= " INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = A1.SCHREGNO ";
            $query .= "                                AND REGD.YEAR = A1.YEAR ";
            $query .= "                                AND REGD.SEMESTER = A1.SEMESTER ";
            $query .= " INNER JOIN SCHREG_REGD_GDAT REGD_G ON REGD_G.YEAR = REGD.YEAR ";
            $query .= "                                   AND REGD_G.GRADE = REGD.GRADE ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' ";
        if ($table == "SCHREG_BRANCH_DAT") {
            $query .= "     AND T1.SCHREGNO IN ( ";
            $query .= "                     SELECT ";
            $query .= "                         REGD_D.SCHREGNO ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_DAT REGD_D ";
            $query .= "                     INNER JOIN ";
            $query .= "                         SCHREG_BASE_MST BASE_M ON REGD_D.SCHREGNO = BASE_M.SCHREGNO ";
            $query .= "                                               AND BASE_M.GRD_DIV IS NULL ";
            $query .= "                                               AND REGD_D.YEAR     = '".$year_add."' ";
            $query .= "                     ) ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            }
        }
        return $query;
    }

    function cnt_table($table_name) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSCOLUMNS ";
        $query .= " WHERE ";
        $query .= "     TBNAME = '{$table_name}' ";

        return $query;
    }
}
?>
