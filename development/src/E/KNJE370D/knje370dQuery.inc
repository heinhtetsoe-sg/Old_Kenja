<?php

require_once('for_php7.php');

class knje370dQuery extends Query {

    //学年取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOL_KIND IN ('H') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getHrClass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
        $query .= "          ON G1.YEAR         = T1.YEAR ";
        $query .= "         AND G1.GRADE        = T1.GRADE ";
        $query .= "         AND G1.SCHOOL_KIND IN ('H') ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->field["DISP"] == 1) {
            $query .= " AND T1.GRADE    = '".$model->field["GRADE"]."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "  AND '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3, SUBTR_CD1, SUBTR_CD2, SUBTR_CD3) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒取得
    function getSchList($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME_SHOW, ";
        $query .= "     T3.HR_NAME, ";
        $query .= "     T1.SCHREGNO || '-' || T1.GRADE || T1.HR_CLASS || T1.ATTENDNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T3 ";
        $query .= "          ON T3.YEAR     = T1.YEAR ";
        $query .= "         AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "         AND T3.GRADE    = T1.GRADE ";
        $query .= "         AND T3.HR_CLASS = T1.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //卒業生取得
    function getGrdSchList($model) {
        $query  = " WITH SCHREG_DAT_MAX_SEMESTER AS ( ";
        $query .= "    SELECT SCHREGNO, ";
        $query .= "           YEAR, ";
        $query .= "           MAX(SEMESTER) AS SEMESTER ";
        $query .= "      FROM SCHREG_REGD_DAT ";
        $query .= "      GROUP BY SCHREGNO, YEAR ";
        $query .= " ), ";
        $query .= " SCHREG_DAT AS ( ";
        $query .= "    SELECT REGD.SCHREGNO, ";
        $query .= "           REGD.YEAR, ";
        $query .= "           REGD.SEMESTER, ";
        $query .= "           REGD.GRADE, ";
        $query .= "           REGD.HR_CLASS, ";
        $query .= "           REGD.ATTENDNO, ";
        $query .= "           REGDH.HR_NAME ";
        $query .= "      FROM SCHREG_REGD_DAT REGD ";
        $query .= "      INNER JOIN ";
        $query .= "           SCHREG_DAT_MAX_SEMESTER W1 ";
        $query .= "         ON W1.SCHREGNO = REGD.SCHREGNO ";
        $query .= "         AND W1.YEAR = REGD.YEAR ";
        $query .= "         AND W1.SEMESTER = REGD.SEMESTER ";
        $query .= "      LEFT JOIN SCHREG_REGD_HDAT REGDH ";
        $query .= "        ON REGDH.YEAR = REGD.YEAR ";
        $query .= "        AND REGDH.SEMESTER = REGD.SEMESTER ";
        $query .= "        AND REGDH.GRADE = REGD.GRADE ";
        $query .= "        AND REGDH.HR_CLASS = REGD.HR_CLASS ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     I1.SCHREGNO, ";
        $query .= "     FISCALYEAR(MAX(I1.GRD_DATE)) AS GRD_DATE, ";
        $query .= "     I1.NAME_SHOW, ";
        $query .= "     W2.GRADE, ";
        $query .= "     W2.HR_CLASS, ";
        $query .= "     W2.ATTENDNO, ";
        $query .= "     W2.HR_NAME ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT T1 ";
        $query .= " INNER JOIN ";
        $query .= "     SCHREG_BASE_MST I1 ";
        $query .= "     ON I1.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_DAT I2 ";
        $query .= "     ON I2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     AND I2.YEAR = T1.YEAR ";
        $query .= "     AND I2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " LEFT JOIN SCHREG_DAT W2 ";
        $query .= "     ON W2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     AND W2.YEAR = CASE WHEN I1.GRD_DATE IS NOT NULL ";
        $query .= "                        THEN FISCALYEAR(I1.GRD_DATE) ";
        $query .= "                        ELSE '' END ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SENKOU_KIND = '0' AND ";
        $query .= "     I2.SCHREGNO IS NULL ";
        $query .= " GROUP BY ";
        $query .= "     I1.SCHREGNO, ";
        $query .= "     I1.NAME_SHOW, ";
        $query .= "     W2.GRADE, ";
        $query .= "     W2.HR_CLASS, ";
        $query .= "     W2.ATTENDNO, ";
        $query .= "     W2.HR_NAME ";
        $query .= " ORDER BY ";
        $query .= "     I1.SCHREGNO ";

        return $query;
    }
}
?>
