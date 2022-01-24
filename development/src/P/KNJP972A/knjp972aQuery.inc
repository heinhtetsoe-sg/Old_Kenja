<?php

require_once('for_php7.php');

class knjp972aQuery extends Query
{
    //名称マスタ
    public function getVNameP009Mst($model)
    {
        $query  = "    SELECT ";
        $query .= "        NAMESPARE1 AS LMCD, ";
        $query .= "        NAME1 AS SYMBOL";
        $query .= "    FROM ";
        $query .= "        V_NAME_MST ";
        $query .= "    WHERE ";
        $query .= "        YEAR = '{$model->year}' ";
        $query .= "        AND NAMECD1 = 'P009' ";
        $query .= "        AND NAMESPARE2 = '".$model->field["SCHOOL_KIND"]."' ";

        return $query;
    }

    //学期取得
    public function getSemester($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    public function getGrade($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種
    public function getSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND GRADE = '".$model->field["GRADE"]."' ";

        return $query;
    }

    //クラス情報
    public function getSchregHdat($model)
    {
        $query  = "    SELECT ";
        $query .= "        HDAT.*, ";
        $query .= "        STF.* ";
        $query .= "    FROM ";
        $query .= "        SCHREG_REGD_HDAT HDAT ";
        $query .= "        LEFT JOIN STAFF_MST STF ";
        $query .= "            ON STF.STAFFCD = HDAT.TR_CD1 ";
        $query .= "    WHERE ";
        $query .= "        YEAR         = '{$model->year}' ";
        $query .= "        AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     ORDER BY ";
        $query .= "         HDAT.GRADE, ";
        $query .= "         HDAT.HR_CLASS ";

        return $query;
    }

    //項目名取得
    public function getLevyMMst($model)
    {
        $query  = "    SELECT ";
        $query .= "        LEVY_L_CD, ";
        $query .= "        LEVY_M_CD, ";
        $query .= "        LEVY_M_NAME ";
        $query .= "    FROM ";
        $query .= "        LEVY_M_MST ";
        $query .= "    WHERE ";
        $query .= "            YEAR          = '{$model->year}' ";
        $query .= "        AND SCHOOLCD      = '{$model->schoolcd}' ";
        $query .= "        AND SCHOOL_KIND   = '{$model->field["SCHOOL_KIND"]}' ";

        return $query;
    }

    //細目名取得
    public function getLevySMst($model)
    {
        $query  = "    SELECT ";
        $query .= "        LEVY_L_CD, ";
        $query .= "        LEVY_M_CD, ";
        $query .= "        LEVY_S_CD, ";
        $query .= "        LEVY_S_NAME ";
        $query .= "    FROM ";
        $query .= "        LEVY_S_MST ";
        $query .= "    WHERE ";
        $query .= "            YEAR          = '{$model->year}' ";
        $query .= "        AND SCHOOLCD      = '{$model->schoolcd}' ";
        $query .= "        AND SCHOOL_KIND   = '{$model->field["SCHOOL_KIND"]}' ";

        return $query;
    }

    //収入科目取得
    public function getIncomeLMst($model)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.INCOME_L_CD AS VALUE, ";
        $query .= "     T1.INCOME_L_CD || ':' || L1.LEVY_L_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT T1 ";
        $query .= "     LEFT JOIN LEVY_L_MST L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.SCHOOLCD = L1.SCHOOLCD ";
        $query .= "          AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ";
        $query .= "          AND T1.INCOME_L_CD = L1.LEVY_L_CD ";
        $query .= "     INNER JOIN LEVY_REQUEST_INCOME_SCHREG_DAT INCOME_SCH ON T1.YEAR = INCOME_SCH.YEAR ";
        $query .= "          AND T1.SCHOOLCD = INCOME_SCH.SCHOOLCD ";
        $query .= "          AND T1.SCHOOL_KIND = INCOME_SCH.SCHOOL_KIND ";
        $query .= "          AND T1.INCOME_L_CD = INCOME_SCH.INCOME_L_CD ";
        $query .= "          AND T1.INCOME_M_CD = INCOME_SCH.INCOME_M_CD ";
        $query .= "          AND T1.REQUEST_NO = INCOME_SCH.REQUEST_NO ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON T1.YEAR = REGD.YEAR ";
        $query .= "          AND REGD.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "          AND REGD.GRADE = '{$model->field["GRADE"]}' ";
        $query .= "          AND REGD.SCHREGNO = INCOME_SCH.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SCHOOLCD = '{$model->schoolcd}' ";
        $query .= "     AND T1.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND VALUE(T1.INCOME_APPROVAL, '0') = '1' ";
        $query .= "     AND VALUE(T1.INCOME_CANCEL, '0') = '0' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //クラス取得
    public function getHrClass($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE || '-' || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->year}' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     GRADE       = '".$model->field["GRADE"]."' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND ";
            $query .= "    (TR_CD1      = '".STAFFCD."' OR ";
            $query .= "     TR_CD2      = '".STAFFCD."' OR ";
            $query .= "     TR_CD3      = '".STAFFCD."' OR ";
            $query .= "     SUBTR_CD1   = '".STAFFCD."' OR ";
            $query .= "     SUBTR_CD2   = '".STAFFCD."' OR ";
            $query .= "     SUBTR_CD3   = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    public function getLList($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.INCOME_L_CD, ";
        $query .= "     L1.LEVY_L_NAME ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT T1 ";
        $query .= "     LEFT JOIN LEVY_L_MST L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.SCHOOLCD = L1.SCHOOLCD ";
        $query .= "          AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ";
        $query .= "          AND T1.INCOME_L_CD = L1.LEVY_L_CD ";
        if ($model->field["INCOME_L_CD"] == "99") {
            $query .= " INNER JOIN LEVY_REQUEST_INCOME_SCHREG_DAT INCOME_SCH ON T1.YEAR = INCOME_SCH.YEAR ";
            $query .= "      AND T1.SCHOOLCD    = INCOME_SCH.SCHOOLCD ";
            $query .= "      AND T1.SCHOOL_KIND = INCOME_SCH.SCHOOL_KIND ";
            $query .= "      AND T1.INCOME_L_CD = INCOME_SCH.INCOME_L_CD ";
            $query .= "      AND T1.INCOME_M_CD = INCOME_SCH.INCOME_M_CD ";
            $query .= "      AND T1.REQUEST_NO = INCOME_SCH.REQUEST_NO ";
            $query .= " INNER JOIN SCHREG_REGD_DAT REGD ON T1.YEAR = REGD.YEAR ";
            $query .= "      AND REGD.SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= "      AND REGD.GRADE = '".$model->field["GRADE"]."' ";
            $query .= "      AND REGD.SCHREGNO = INCOME_SCH.SCHREGNO ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SCHOOLCD = '{$model->schoolcd}' ";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     AND VALUE(T1.INCOME_APPROVAL, '0') = '1' ";
        $query .= "     AND VALUE(T1.INCOME_CANCEL, '0') = '0' ";
        if ($model->field["INCOME_L_CD"] != "99") {
            $query .= " AND T1.INCOME_L_CD = '".$model->field["INCOME_L_CD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.INCOME_L_CD ";

        return $query;
    }

    public function getSchregSql($model, $incomeLcd, $gradeHrClass)
    {
        $query  = " SELECT ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGDH.HR_NAME, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.NAME AS NAME, ";
        $query .= "     STAFF.STAFFNAME, ";
        $query .= "     SUM(VALUE(INCOME_SCH.INCOME_MONEY, 0)) AS INCOME_MONEY ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ";
        $query .= "          AND REGD.SEMESTER = REGDH.SEMESTER ";
        $query .= "          AND REGD.GRADE = REGDH.GRADE ";
        $query .= "          AND REGD.HR_CLASS = REGDH.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN LEVY_REQUEST_INCOME_DAT INCOME ON REGD.YEAR = INCOME.YEAR ";
        $query .= "          AND INCOME.SCHOOLCD    = '{$model->schoolcd}' ";
        $query .= "          AND INCOME.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "          AND INCOME.INCOME_L_CD = '".$incomeLcd."' ";
        $query .= "          AND VALUE(INCOME.INCOME_CANCEL, '0') = '0' ";
        $query .= "     LEFT JOIN LEVY_REQUEST_INCOME_SCHREG_DAT INCOME_SCH ON INCOME.YEAR = INCOME_SCH.YEAR ";
        $query .= "          AND INCOME.SCHOOLCD    = INCOME_SCH.SCHOOLCD ";
        $query .= "          AND INCOME.SCHOOL_KIND = INCOME_SCH.SCHOOL_KIND ";
        $query .= "          AND INCOME.INCOME_L_CD = INCOME_SCH.INCOME_L_CD ";
        $query .= "          AND INCOME.INCOME_M_CD = INCOME_SCH.INCOME_M_CD ";
        $query .= "          AND INCOME.REQUEST_NO  = INCOME_SCH.REQUEST_NO ";
        $query .= "          AND REGD.SCHREGNO = INCOME_SCH.SCHREGNO ";
        $query .= "     LEFT JOIN STAFF_MST STAFF ON REGDH.TR_CD1 = STAFF.STAFFCD ";
        $query .= " WHERE ";
        $query .= "          REGD.YEAR = '{$model->year}' ";
        $query .= "          AND REGD.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "          AND REGD.GRADE || '-' || REGD.HR_CLASS = '".$gradeHrClass."' ";
        $query .= " GROUP BY ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGDH.HR_NAME, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     STAFF.STAFFNAME ";
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }

    public function getIncomeSql($model, $incomeLcd, $gradeHrClass, $div = "")
    {
        $query .= " WITH SCH_INCOME_SUM AS ( ";
        $query .= "    SELECT ";
        $query .= "        INCOME_SCH.SCHREGNO, ";
        $query .= "        VALUE(INCOME.COLLECT_DIV, '999') AS COLLECT_DIV, ";
        $query .= "        VALUE(INCOME.INCOME_L_CD, '999') AS INCOME_L_CD, ";
        $query .= "        VALUE(INCOME.INCOME_M_CD, '999') AS INCOME_M_CD, ";
        $query .= "        SUM(INCOME_SCH.INCOME_MONEY) AS INCOME_MONEY ";
        $query .= "    FROM ";
        $query .= "        LEVY_REQUEST_INCOME_SCHREG_DAT INCOME_SCH ";
        $query .= "        INNER JOIN LEVY_REQUEST_INCOME_DAT INCOME ";
        $query .= "            ON INCOME.SCHOOLCD = INCOME_SCH.SCHOOLCD ";
        $query .= "            AND INCOME.SCHOOL_KIND   = INCOME_SCH.SCHOOL_KIND ";
        $query .= "            AND INCOME.INCOME_L_CD   = INCOME_SCH.INCOME_L_CD ";
        $query .= "            AND INCOME.INCOME_M_CD   = INCOME_SCH.INCOME_M_CD ";
        $query .= "            AND INCOME.REQUEST_NO    = INCOME_SCH.REQUEST_NO ";
        $query .= "        INNER JOIN SCHREG_REGD_DAT REGD ";
        $query .= "            ON REGD.SCHREGNO     = INCOME_SCH.SCHREGNO ";
        $query .= "            AND REGD.YEAR        = INCOME.YEAR ";
        $query .= "            AND REGD.SEMESTER    = '{$model->field["SEMESTER"]}' ";
        $query .= "            AND REGD.GRADE || '-' || REGD.HR_CLASS = '{$gradeHrClass}' ";
        $query .= "    WHERE ";
        $query .= "            INCOME_SCH.SCHOOLCD      = '{$model->schoolcd}' ";
        $query .= "        AND INCOME_SCH.SCHOOL_KIND   = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= "        AND INCOME_SCH.YEAR          = '{$model->year}' ";
        $query .= "        AND INCOME_SCH.INCOME_L_CD   = '{$incomeLcd}' ";
        $query .= "        AND VALUE (INCOME.INCOME_CANCEL, '') <> '1' ";
        $query .= "    GROUP BY ";
        $query .= "        GROUPING SETS ( ";
        $query .= "            (INCOME_SCH.SCHREGNO, INCOME.COLLECT_DIV, INCOME.INCOME_L_CD, INCOME.INCOME_M_CD), ";
        $query .= "            (INCOME_SCH.SCHREGNO, INCOME.INCOME_L_CD, INCOME.INCOME_M_CD), ";
        $query .= "            (INCOME_SCH.SCHREGNO) ";
        $query .= "        ) ";
        $query .= "    ORDER BY ";
        $query .= "        INCOME_SCH.SCHREGNO, ";
        $query .= "        INCOME.COLLECT_DIV, ";
        $query .= "        INCOME.INCOME_L_CD, ";
        $query .= "        INCOME.INCOME_M_CD ";
        $query .= " ), SCH_INCOME AS ( ";
        $query .= "     SELECT ";
        $query .= "         SIS.*, ";
        $query .= "         COLLECT_DIV || INCOME_L_CD || INCOME_M_CD AS INCOME_KEY, ";
        $query .= "         CASE WHEN COLLECT_DIV = '999' AND INCOME_L_CD = '999' AND INCOME_M_CD = '999' THEN 'INCOME_TOTAL' ";
        $query .= "              WHEN COLLECT_DIV = '999' AND INCOME_L_CD <> '999' AND INCOME_M_CD <> '999' THEN 'INCOME_UTIWAKE' ";
        $query .= "              ELSE 'INCOME_MEISAI' ";
        $query .= "         END AS INCOME_DIV ";
        $query .= "     FROM ";
        $query .= "         SCH_INCOME_SUM SIS ";
        $query .= " ), HR_INCOME AS ( ";
        $query .= "     SELECT ";
        $query .= "         INCOME_KEY, ";
        $query .= "         INCOME_DIV, ";
        $query .= "         ROW_NUMBER() OVER(PARTITION BY INCOME_DIV) AS INCOME_DIV_NO, ";
        $query .= "         COLLECT_DIV, ";
        $query .= "         INCOME_L_CD, ";
        $query .= "         INCOME_M_CD, ";
        $query .= "         COUNT(*) AS SCH_CNT, ";
        $query .= "         SUM(INCOME_MONEY) AS INCOME_MONEY ";
        $query .= "     FROM ";
        $query .= "         SCH_INCOME ";
        $query .= "     GROUP BY ";
        $query .= "         INCOME_KEY, ";
        $query .= "         COLLECT_DIV, ";
        $query .= "         INCOME_L_CD, ";
        $query .= "         INCOME_M_CD, ";
        $query .= "         INCOME_DIV ";
        $query .= " ) ";
        if ($div == "SCHREGNO") {
            $query .= " SELECT ";
            $query .= "     SCH.SCHREGNO, ";
            $query .= "     SCH.INCOME_KEY, ";
            $query .= "     SCH.INCOME_DIV, ";
            $query .= "     SCH.COLLECT_DIV, ";
            $query .= "     SCH.INCOME_L_CD, ";
            $query .= "     SCH.INCOME_M_CD, ";
            $query .= "     SCH.INCOME_MONEY, ";
            $query .= "     HR.INCOME_DIV || HR.INCOME_DIV_NO AS CSV_HEADER_KEY ";
            $query .= " FROM ";
            $query .= "     SCH_INCOME SCH";
            $query .= " LEFT JOIN HR_INCOME HR";
            $query .= "     ON HR.INCOME_KEY = SCH.INCOME_KEY ";
        } else {
            $query .= " SELECT ";
            $query .= "     INCOME_DIV || INCOME_DIV_NO AS CSV_HEADER_KEY, ";
            $query .= "     HIN.* ";
            $query .= " FROM ";
            $query .= "     HR_INCOME HIN ";
            $query .= " ORDER BY ";
            $query .= "     CASE INCOME_DIV ";
            $query .= "         WHEN 'INCOME_MEISAI'  THEN 1 ";
            $query .= "         WHEN 'INCOME_UTIWAKE' THEN 2 ";
            $query .= "         WHEN 'INCOME_TOTAL'   THEN 3 ";
            $query .= "     END, ";
            $query .= "     INCOME_DIV_NO ";
        }

        return $query;
    }

    public function getOutgoSql($model, $incomeLcd, $gradeHrClass, $div = "")
    {
        $query .= " WITH REGD_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         REGD.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT REGD ";
        $query .= "     WHERE ";
        $query .= "         REGD.YEAR           = '{$model->year}' ";
        $query .= "         AND REGD.SEMESTER   = '{$model->field["SEMESTER"]}' ";
        $query .= "         AND REGD.GRADE || '-' || REGD.HR_CLASS = '{$gradeHrClass}' ";
        $query .= " ), SCH_OUTGO_BASE AS ( ";
        $query .= "     SELECT ";
        $query .= "         OUTGO_SCH.SCHREGNO, ";
        $query .= "         OUTGO_SCH.REQUEST_NO, ";
        $query .= "         OUTGO_SCH.LINE_NO, ";
        $query .= "         OUTGO_SCH.OUTGO_L_CD, ";
        $query .= "         OUTGO_SCH.OUTGO_M_CD, ";
        $query .= "         OUTGO_SCH.OUTGO_S_CD, ";
        $query .= "         VALUE (T1.OUTGO_APPROVAL, '2') AS OUTGO_APPROVAL, ";
        $query .= "         CASE ";
        $query .= "             WHEN VALUE (T1.OUTGO_APPROVAL, '0') = '1' ";
        $query .= "                 THEN T1.OUTGO_DATE ";
        $query .= "             ELSE T1.REQUEST_DATE ";
        $query .= "             END AS SORT_DATE, ";
        $query .= "         VALUE (OUTGO_SCH.OUTGO_MONEY, 0) AS OUTGO_MONEY, ";
        $query .= "         T1.INCOME_L_CD, ";
        $query .= "         T1.INCOME_M_CD ";
        $query .= "     FROM ";
        $query .= "         LEVY_REQUEST_OUTGO_DAT T1 ";
        $query .= "         INNER JOIN LEVY_REQUEST_OUTGO_SCHREG_DAT OUTGO_SCH ";
        $query .= "             ON T1.YEAR = OUTGO_SCH.YEAR ";
        $query .= "             AND T1.SCHOOLCD = OUTGO_SCH.SCHOOLCD ";
        $query .= "             AND T1.SCHOOL_KIND = OUTGO_SCH.SCHOOL_KIND ";
        $query .= "             AND T1.OUTGO_L_CD = OUTGO_SCH.OUTGO_L_CD ";
        $query .= "             AND T1.OUTGO_M_CD = OUTGO_SCH.OUTGO_M_CD ";
        $query .= "             AND T1.REQUEST_NO = OUTGO_SCH.REQUEST_NO ";
        $query .= "         INNER JOIN REGD_DATA REGD ";
        $query .= "             ON REGD.SCHREGNO = OUTGO_SCH.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR             = '{$model->year}' ";
        $query .= "         AND T1.SCHOOLCD     = '{$model->schoolcd}' ";
        $query .= "         AND T1.SCHOOL_KIND  = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= "         AND T1.INCOME_L_CD  = '{$incomeLcd}' ";
        $query .= "         AND VALUE (T1.OUTGO_CANCEL, '') <> '1' ";
        $query .= "         AND OUTGO_SCH.OUTGO_L_CD <> '99' ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         OUTGO_SCH.SCHREGNO, ";
        $query .= "         'HENKIN' AS REQUEST_NO, ";
        $query .= "         OUTGO_SCH.LINE_NO, ";
        $query .= "         OUTGO_SCH.OUTGO_L_CD, ";
        $query .= "         OUTGO_SCH.OUTGO_M_CD, ";
        $query .= "         OUTGO_SCH.OUTGO_S_CD, ";
        $query .= "         VALUE (T1.OUTGO_APPROVAL, '2') AS OUTGO_APPROVAL, ";
        $query .= "         CASE ";
        $query .= "           WHEN VALUE (T1.OUTGO_APPROVAL, '0') = '1' ";
        $query .= "               THEN T1.OUTGO_DATE ";
        $query .= "           ELSE T1.REQUEST_DATE ";
        $query .= "           END AS SORT_DATE, ";
        $query .= "         SUM(VALUE (OUTGO_SCH.OUTGO_MONEY, 0)) AS OUTGO_MONEY, ";
        $query .= "         T1.INCOME_L_CD, ";
        $query .= "         T1.INCOME_M_CD ";
        $query .= "     FROM ";
        $query .= "         LEVY_REQUEST_OUTGO_DAT T1 ";
        $query .= "         INNER JOIN LEVY_REQUEST_OUTGO_SCHREG_DAT OUTGO_SCH ";
        $query .= "             ON T1.YEAR = OUTGO_SCH.YEAR ";
        $query .= "             AND T1.SCHOOLCD = OUTGO_SCH.SCHOOLCD ";
        $query .= "             AND T1.SCHOOL_KIND = OUTGO_SCH.SCHOOL_KIND ";
        $query .= "             AND T1.OUTGO_L_CD = OUTGO_SCH.OUTGO_L_CD ";
        $query .= "             AND T1.OUTGO_M_CD = OUTGO_SCH.OUTGO_M_CD ";
        $query .= "             AND T1.REQUEST_NO = OUTGO_SCH.REQUEST_NO ";
        $query .= "         INNER JOIN REGD_DATA REGD ";
        $query .= "             ON REGD.SCHREGNO = OUTGO_SCH.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR             = '{$model->year}' ";
        $query .= "         AND T1.SCHOOLCD     = '{$model->schoolcd}' ";
        $query .= "         AND T1.SCHOOL_KIND  = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= "         AND T1.INCOME_L_CD  = '{$incomeLcd}' ";
        $query .= "         AND VALUE (T1.OUTGO_APPROVAL, '0') = '1' ";
        $query .= "         AND VALUE (T1.HENKIN_APPROVAL, '0') = '1' ";
        $query .= "         AND VALUE (T1.OUTGO_CANCEL, '') <> '1' ";
        $query .= "         AND OUTGO_SCH.LINE_NO = '1' ";
        $query .= "         AND OUTGO_SCH.OUTGO_L_CD = '99' ";
        $query .= "     GROUP BY ";
        $query .= "         OUTGO_SCH.SCHREGNO, ";
        $query .= "         OUTGO_SCH.LINE_NO, ";
        $query .= "         OUTGO_SCH.OUTGO_L_CD, ";
        $query .= "         OUTGO_SCH.OUTGO_M_CD, ";
        $query .= "         OUTGO_SCH.OUTGO_S_CD, ";
        $query .= "         VALUE (T1.OUTGO_APPROVAL, '2'), ";
        $query .= "         CASE ";
        $query .= "           WHEN VALUE (T1.OUTGO_APPROVAL, '0') = '1' ";
        $query .= "               THEN T1.OUTGO_DATE ";
        $query .= "           ELSE T1.REQUEST_DATE ";
        $query .= "           END, ";
        $query .= "         T1.INCOME_L_CD, ";
        $query .= "         T1.INCOME_M_CD ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "             CARRY.SCHREGNO, ";
        $query .= "             'CARRY_OVER' AS REQUEST_NO, ";
        $query .= "             1 AS LINE_NO, ";
        $query .= "             '88' AS OUTGO_L_CD, ";
        $query .= "             '88' AS OUTGO_M_CD, ";
        $query .= "             '888' AS OUTGO_S_CD, ";
        $query .= "             '1' AS OUTGO_APPROVAL, ";
        $query .= "             '9999-12-31' AS SORT_DATE, ";
        $query .= "             SUM(VALUE (CARRY_OVER_MONEY, 0)) AS OUTGO_MONEY, ";
        $query .= "             CARRY.INCOME_L_CD, ";
        $query .= "             CARRY.INCOME_M_CD ";
        $query .= "         FROM ";
        $query .= "             LEVY_CARRY_OVER_DAT CARRY ";
        $query .= "         INNER JOIN REGD_DATA REGD ";
        $query .= "             ON REGD.SCHREGNO = CARRY.SCHREGNO ";
        $query .= "         WHERE ";
        $query .= "             CARRY.YEAR             = '{$model->year}' ";
        $query .= "             AND CARRY.SCHOOLCD     = '{$model->schoolcd}' ";
        $query .= "             AND CARRY.SCHOOL_KIND  = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= "             AND CARRY.INCOME_L_CD  = '{$incomeLcd}' ";
        $query .= "             AND VALUE (CARRY.CARRY_CANCEL, '') <> '1' ";
        $query .= "         GROUP BY ";
        $query .= "             CARRY.SCHREGNO, ";
        $query .= "             CARRY.INCOME_L_CD, ";
        $query .= "             CARRY.INCOME_M_CD ";
        $query .= " ), SCH_OUTGO_0 AS ( ";
        $query .= "     SELECT ";
        $query .= "         SOB.*, ";
        $query .= "         (CASE REQUEST_NO ";
        $query .= "             WHEN 'CARRY_OVER' THEN REQUEST_NO || INCOME_L_CD || INCOME_M_CD ";
        $query .= "             WHEN 'HENKIN' THEN REQUEST_NO || LINE_NO ";
        $query .= "             ELSE OUTGO_APPROVAL || VALUE(SORT_DATE, '') || REQUEST_NO || LINE_NO ";
        $query .= "          END) AS OUTGO_KEY ";
        $query .= "     FROM ";
        $query .= "         SCH_OUTGO_BASE SOB";
        $query .= " ), SCH_OUTGO_SUM AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         VALUE (OUTGO_KEY,   '999') AS OUTGO_KEY, ";
        $query .= "         VALUE (INCOME_L_CD, '999') AS INCOME_L_CD, ";
        $query .= "         VALUE (INCOME_M_CD, '999') AS INCOME_M_CD, ";
        $query .= "         SUM(OUTGO_MONEY) AS OUTGO_MONEY ";
        $query .= "     FROM ";
        $query .= "         SCH_OUTGO_0 ";
        $query .= "     GROUP BY GROUPING SETS ( ";
        $query .= "             (SCHREGNO, OUTGO_KEY, INCOME_L_CD, INCOME_M_CD), ";
        $query .= "             (SCHREGNO, INCOME_L_CD, INCOME_M_CD), ";
        $query .= "             (SCHREGNO) ";
        $query .= "     ) ";
        $query .= " ), SCH_OUTGO AS ( ";
        $query .= "     SELECT ";
        $query .= "         SOS.*, ";
        $query .= "         CASE WHEN SOS.OUTGO_KEY = '999' AND SOS.INCOME_L_CD = '999' AND SOS.INCOME_M_CD = '999' THEN 'OUTGO_TOTAL' ";
        $query .= "              WHEN SOS.OUTGO_KEY = '999' AND SOS.INCOME_L_CD <> '999' AND SOS.INCOME_M_CD <> '999' THEN 'OUTGO_UTIWAKE' ";
        $query .= "              ELSE 'OUTGO_MEISAI' ";
        $query .= "         END AS OUTGO_DIV, ";
        $query .= "         SO0.REQUEST_NO, ";
        $query .= "         SO0.OUTGO_L_CD, ";
        $query .= "         SO0.OUTGO_M_CD, ";
        $query .= "         SO0.OUTGO_S_CD, ";
        $query .= "         SO0.OUTGO_APPROVAL, ";
        $query .= "         SO0.SORT_DATE ";
        $query .= "     FROM ";
        $query .= "         SCH_OUTGO_SUM SOS";
        $query .= "     LEFT JOIN SCH_OUTGO_0 SO0 ";
        $query .= "         ON SO0.SCHREGNO = SOS.SCHREGNO ";
        $query .= "         AND SO0.OUTGO_KEY = SOS.OUTGO_KEY ";
        $query .= " ), HR_OUTGO AS ( ";
        $query .= "     SELECT ";
        $query .= "         OUTGO_KEY, ";
        $query .= "         OUTGO_DIV, ";
        $query .= "         ROW_NUMBER() OVER(PARTITION BY OUTGO_DIV ORDER BY OUTGO_KEY, INCOME_L_CD, INCOME_M_CD) AS OUTGO_DIV_NO, ";
        $query .= "         COUNT(*) AS SCH_CNT, ";
        $query .= "         SUM(OUTGO_MONEY) AS OUTGO_MONEY, ";
        $query .= "         INCOME_L_CD, ";
        $query .= "         INCOME_M_CD, ";
        $query .= "         REQUEST_NO, ";
        $query .= "         OUTGO_APPROVAL, ";
        $query .= "         SORT_DATE, ";
        $query .= "         OUTGO_L_CD, ";
        $query .= "         OUTGO_M_CD, ";
        $query .= "         OUTGO_S_CD ";
        $query .= "     FROM ";
        $query .= "         SCH_OUTGO ";
        $query .= "     GROUP BY ";
        $query .= "         OUTGO_KEY, ";
        $query .= "         INCOME_L_CD, ";
        $query .= "         INCOME_M_CD, ";
        $query .= "         OUTGO_DIV, ";
        $query .= "         REQUEST_NO, ";
        $query .= "         OUTGO_APPROVAL, ";
        $query .= "         SORT_DATE, ";
        $query .= "         OUTGO_L_CD, ";
        $query .= "         OUTGO_M_CD, ";
        $query .= "         OUTGO_S_CD ";
        $query .= " ) ";
        if ($div == "SCHREGNO") {
            $query .= " SELECT ";
            $query .= "     SCH.SCHREGNO, ";
            $query .= "     SCH.OUTGO_KEY, ";
            $query .= "     SCH.OUTGO_DIV, ";
            $query .= "     SCH.OUTGO_MONEY, ";
            $query .= "     SCH.INCOME_L_CD, ";
            $query .= "     SCH.INCOME_M_CD, ";
            $query .= "     SCH.OUTGO_L_CD, ";
            $query .= "     SCH.OUTGO_M_CD, ";
            $query .= "     SCH.OUTGO_S_CD, ";
            $query .= "     HR.OUTGO_DIV || HR.OUTGO_DIV_NO AS CSV_HEADER_KEY ";
            $query .= " FROM ";
            $query .= "     SCH_OUTGO SCH ";
            $query .= " LEFT JOIN HR_OUTGO HR";
            $query .= "     ON HR.OUTGO_KEY = SCH.OUTGO_KEY ";
            $query .= "     AND HR.INCOME_L_CD = SCH.INCOME_L_CD ";
            $query .= "     AND HR.INCOME_M_CD = SCH.INCOME_M_CD ";
        } else {
            $query .= " SELECT ";
            $query .= "     OUTGO_DIV || OUTGO_DIV_NO AS CSV_HEADER_KEY, ";
            $query .= "     HO.* ";
            $query .= " FROM ";
            $query .= "     HR_OUTGO HO ";
            $query .= " ORDER BY ";
            $query .= "     CASE OUTGO_DIV ";
            $query .= "         WHEN 'OUTGO_MEISAI'  THEN 1 ";
            $query .= "         WHEN 'OUTGO_UTIWAKE' THEN 3 ";
            $query .= "         WHEN 'OUTGO_TOTAL'   THEN 2 ";
            $query .= "     END, ";
            $query .= "     OUTGO_DIV_NO ";
        }

        return $query;
    }

    public function getHasuu($model)
    {
        $query  = " WITH MAX_SEME AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         max(SEMESTER) AS MAX_SEMESTER ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->year}' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ), REGD_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN MAX_SEME T2 ON T2.SCHREGNO     = T1.SCHREGNO ";
        $query .= "                               AND T2.MAX_SEMESTER = T1.SEMESTER ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR  = '{$model->year}' ";
        $query .= "         AND T1.GRADE || '-' || T1.HR_CLASS IN ('".implode("','", $model->select_data["selectdata"])."') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     OUTG.INCOME_L_CD, ";
        $query .= "     VALUE(OUTG.OUTGO_APPROVAL, '2') AS OUTGO_APPROVAL, ";
        $query .= "     CASE WHEN VALUE(OUTG.OUTGO_APPROVAL, '0') = '1' THEN OUTG.OUTGO_DATE ELSE OUTG.REQUEST_DATE END AS SORT_DATE, ";
        $query .= "     MSAI.REQUEST_NO, ";
        $query .= "     MSAI.LINE_NO, ";
        $query .= "     value(MSAI.HASUU, 0) AS HASUU ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_MEISAI_DAT MSAI ";
        $query .= "     LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTG ON OUTG.SCHOOLCD    = MSAI.SCHOOLCD ";
        $query .= "                                          AND OUTG.SCHOOL_KIND = MSAI.SCHOOL_KIND ";
        $query .= "                                          AND OUTG.YEAR        = MSAI.YEAR ";
        $query .= "                                          AND OUTG.OUTGO_L_CD  = MSAI.OUTGO_L_CD ";
        $query .= "                                          AND OUTG.OUTGO_M_CD  = MSAI.OUTGO_M_CD ";
        $query .= "                                          AND OUTG.REQUEST_NO  = MSAI.REQUEST_NO ";
        $query .= " WHERE ";
        $query .= "         MSAI.SCHOOLCD       = '{$model->schoolcd}' ";
        $query .= "     AND MSAI.SCHOOL_KIND    = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     AND MSAI.YEAR           = '{$model->year}' ";
        $query .= "     AND OUTG.OUTGO_CANCEL   is null ";
        $query .= "     AND value(MSAI.HASUU, 0) <> 0 ";
        $query .= "     AND MSAI.REQUEST_NO     in (SELECT ";
        $query .= "                                     REQUEST_NO ";
        $query .= "                                 FROM ";
        $query .= "                                     LEVY_REQUEST_OUTGO_SCHREG_DAT SCHD ";
        $query .= "                                     INNER JOIN REGD_DATA REGD ON REGD.SCHREGNO = SCHD.SCHREGNO ";
        $query .= "                                 WHERE ";
        $query .= "                                         SCHD.SCHOOLCD    = MSAI.SCHOOLCD ";
        $query .= "                                     AND SCHD.SCHOOL_KIND = MSAI.SCHOOL_KIND ";
        $query .= "                                     AND SCHD.YEAR        = MSAI.YEAR ";
        $query .= "                                     AND SCHD.REQUEST_NO  = MSAI.REQUEST_NO ";
        $query .= "                                     AND SCHD.LINE_NO     = MSAI.LINE_NO ";
        $query .= "                                     AND SCHD.OUTGO_L_CD  = MSAI.OUTGO_L_CD ";
        $query .= "                                     AND SCHD.OUTGO_M_CD  = MSAI.OUTGO_M_CD ";
        $query .= "                                     AND SCHD.OUTGO_S_CD  = MSAI.OUTGO_S_CD ";
        $query .= "                                 ) ";

        return $query;
    }
}
