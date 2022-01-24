<?php

require_once('for_php7.php');

class knjp972Query extends Query {
    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade() {
        $query  = " SELECT DISTINCT ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '".$model->field["GRADE"]."' ";

        return $query;
    }

    //収入科目取得
    function getIncomeLMst($model) {
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
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCHOOLCD = '".SCHOOLCD."' ";
        $query .= "     AND T1.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND VALUE(T1.INCOME_APPROVAL, '0') = '1' ";
        $query .= "     AND VALUE(T1.INCOME_CANCEL, '0') = '0' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //クラス取得
    function getHrClass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE || '-' || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
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

    function getLList($model) {
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
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCHOOLCD = '".SCHOOLCD."' ";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     AND VALUE(T1.INCOME_CANCEL, '0') = '0' ";
        if ($model->field["INCOME_L_CD"] != "99") {
            $query .= " AND T1.INCOME_L_CD = '".$model->field["INCOME_L_CD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.INCOME_L_CD ";

        return $query;
    }

    function getSchregSql($model, $lListRow) {
        $query  = " SELECT ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGDH.HR_NAME, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.NAME AS SCH_NAME, ";
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
        $query .= "          AND INCOME.SCHOOLCD    = '".SCHOOLCD."' ";
        $query .= "          AND INCOME.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "          AND INCOME.INCOME_L_CD = '".$lListRow["INCOME_L_CD"]."' ";
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
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "           AND REGD.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "           AND REGD.GRADE || '-' || REGD.HR_CLASS IN ('".implode("','", $this->select_data["selectdata"])."') ";
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

    function getIncomeSql($model, $lListRow, $schregRow) {
        $query  = " SELECT ";
        $query .= "     INCOME.COLLECT_DIV, ";
        $query .= "     INCOME.INCOME_L_CD, ";
        $query .= "     INCOME.INCOME_M_CD, ";
        $query .= "     MMST.LEVY_M_NAME, ";
        $query .= "     SUM(INCOME_SCH.INCOME_MONEY) AS INCOME_MONEY ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT INCOME ";
        $query .= "     INNER JOIN LEVY_REQUEST_INCOME_SCHREG_DAT INCOME_SCH ON INCOME.YEAR = INCOME_SCH.YEAR ";
        $query .= "          AND INCOME.SCHOOLCD    = INCOME_SCH.SCHOOLCD ";
        $query .= "          AND INCOME.SCHOOL_KIND = INCOME_SCH.SCHOOL_KIND ";
        $query .= "          AND INCOME.INCOME_L_CD = INCOME_SCH.INCOME_L_CD ";
        $query .= "          AND INCOME.INCOME_M_CD = INCOME_SCH.INCOME_M_CD ";
        $query .= "          AND INCOME.REQUEST_NO  = INCOME_SCH.REQUEST_NO ";
        $query .= "          AND INCOME_SCH.SCHREGNO    = '".$schregRow["SCHREGNO"]."' ";
        $query .= "     LEFT JOIN LEVY_M_MST MMST ON INCOME.YEAR = MMST.YEAR ";
        $query .= "          AND INCOME.SCHOOLCD    = MMST.SCHOOLCD ";
        $query .= "          AND INCOME.SCHOOL_KIND = MMST.SCHOOL_KIND ";
        $query .= "          AND INCOME.INCOME_L_CD  = MMST.LEVY_L_CD ";
        $query .= "          AND INCOME.INCOME_M_CD  = MMST.LEVY_M_CD ";
        $query .= " WHERE ";
        $query .= "     INCOME.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND INCOME.SCHOOLCD    = '".SCHOOLCD."' ";
        $query .= "     AND INCOME.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     AND INCOME.INCOME_L_CD = '".$lListRow["INCOME_L_CD"]."' ";
        $query .= "     AND VALUE(INCOME.INCOME_CANCEL, '0') = '0' ";
        $query .= " GROUP BY ";
        $query .= "     INCOME.COLLECT_DIV, ";
        $query .= "     INCOME.INCOME_L_CD, ";
        $query .= "     INCOME.INCOME_M_CD, ";
        $query .= "     MMST.LEVY_M_NAME ";
        $query .= " ORDER BY ";
        $query .= "     INCOME.COLLECT_DIV, ";
        $query .= "     INCOME.INCOME_M_CD ";

        return $query;
    }

    function getOutgoSql($model, $lListRow, $schregRow) {
        $query  = " SELECT ";
        $query .= "     OUTGO_SCH.REQUEST_NO, ";
        $query .= "     OUTGO_SCH.LINE_NO, ";
        $query .= "     OUTGO_SCH.OUTGO_L_CD, ";
        $query .= "     OUTGO_SCH.OUTGO_M_CD, ";
        $query .= "     OUTGO_SCH.OUTGO_S_CD, ";
        $query .= "     VALUE(T1.OUTGO_APPROVAL, '2') AS OUTGO_APPROVAL, ";
        $query .= "     CASE WHEN VALUE(T1.OUTGO_APPROVAL, '0') = '1' THEN T1.OUTGO_DATE ELSE T1.REQUEST_DATE END AS SORT_DATE, ";
        $query .= "     SMST.LEVY_S_NAME, ";
        $query .= "     VALUE(OUTGO_SCH.OUTGO_MONEY, 0) AS OUTGO_MONEY ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT T1 ";
        $query .= "     INNER JOIN LEVY_REQUEST_OUTGO_SCHREG_DAT OUTGO_SCH ON T1.YEAR = OUTGO_SCH.YEAR ";
        $query .= "           AND T1.SCHOOLCD    = OUTGO_SCH.SCHOOLCD ";
        $query .= "           AND T1.SCHOOL_KIND = OUTGO_SCH.SCHOOL_KIND ";
        $query .= "           AND T1.OUTGO_L_CD  = OUTGO_SCH.OUTGO_L_CD ";
        $query .= "           AND T1.OUTGO_M_CD  = OUTGO_SCH.OUTGO_M_CD ";
        $query .= "           AND T1.REQUEST_NO  = OUTGO_SCH.REQUEST_NO ";
        $query .= "           AND OUTGO_SCH.SCHREGNO = '".$schregRow["SCHREGNO"]."' ";
        $query .= "     LEFT JOIN LEVY_S_MST SMST ON OUTGO_SCH.YEAR = SMST.YEAR ";
        $query .= "          AND OUTGO_SCH.SCHOOLCD    = SMST.SCHOOLCD ";
        $query .= "          AND OUTGO_SCH.SCHOOL_KIND = SMST.SCHOOL_KIND ";
        $query .= "          AND OUTGO_SCH.OUTGO_L_CD  = SMST.LEVY_L_CD ";
        $query .= "          AND OUTGO_SCH.OUTGO_M_CD  = SMST.LEVY_M_CD ";
        $query .= "          AND OUTGO_SCH.OUTGO_S_CD  = SMST.LEVY_S_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCHOOLCD = '".SCHOOLCD."' ";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     AND VALUE(T1.OUTGO_CANCEL, '0') = '0' ";
        $query .= "     AND T1.INCOME_L_CD = '".$lListRow["INCOME_L_CD"]."' ";
        $query .= "     AND OUTGO_SCH.OUTGO_L_CD <> '99' ";
        //返金伝票（合算で出力する）
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     'HENKIN' as REQUEST_NO, ";
        $query .= "     OUTGO_SCH.LINE_NO, ";
        $query .= "     OUTGO_SCH.OUTGO_L_CD, ";
        $query .= "     OUTGO_SCH.OUTGO_M_CD, ";
        $query .= "     OUTGO_SCH.OUTGO_S_CD, ";
        $query .= "     VALUE(T1.OUTGO_APPROVAL, '2') AS OUTGO_APPROVAL, ";
        $query .= "     CASE WHEN VALUE(T1.OUTGO_APPROVAL, '0') = '1' THEN T1.OUTGO_DATE ELSE T1.REQUEST_DATE END AS SORT_DATE, ";
        $query .= "     SMST.LEVY_S_NAME, ";
        $query .= "     sum(VALUE(OUTGO_SCH.OUTGO_MONEY, 0)) AS OUTGO_MONEY ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT T1 ";
        $query .= "     INNER JOIN LEVY_REQUEST_OUTGO_SCHREG_DAT OUTGO_SCH ON T1.YEAR = OUTGO_SCH.YEAR ";
        $query .= "           AND T1.SCHOOLCD    = OUTGO_SCH.SCHOOLCD ";
        $query .= "           AND T1.SCHOOL_KIND = OUTGO_SCH.SCHOOL_KIND ";
        $query .= "           AND T1.OUTGO_L_CD  = OUTGO_SCH.OUTGO_L_CD ";
        $query .= "           AND T1.OUTGO_M_CD  = OUTGO_SCH.OUTGO_M_CD ";
        $query .= "           AND T1.REQUEST_NO  = OUTGO_SCH.REQUEST_NO ";
        $query .= "           AND OUTGO_SCH.SCHREGNO = '".$schregRow["SCHREGNO"]."' ";
        $query .= "     LEFT JOIN LEVY_S_MST SMST ON OUTGO_SCH.YEAR = SMST.YEAR ";
        $query .= "          AND OUTGO_SCH.SCHOOLCD    = SMST.SCHOOLCD ";
        $query .= "          AND OUTGO_SCH.SCHOOL_KIND = SMST.SCHOOL_KIND ";
        $query .= "          AND OUTGO_SCH.OUTGO_L_CD  = SMST.LEVY_L_CD ";
        $query .= "          AND OUTGO_SCH.OUTGO_M_CD  = SMST.LEVY_M_CD ";
        $query .= "          AND OUTGO_SCH.OUTGO_S_CD  = SMST.LEVY_S_CD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     AND VALUE(T1.OUTGO_APPROVAL, '0') = '1' ";
        $query .= "     AND VALUE(T1.HENKIN_APPROVAL, '0') = '1' ";
        $query .= "     AND VALUE(T1.OUTGO_CANCEL, '0')   = '0' ";
        $query .= "     AND T1.INCOME_L_CD       = '".$lListRow["INCOME_L_CD"]."' ";
        $query .= "     AND OUTGO_SCH.OUTGO_L_CD = '99' ";
        $query .= " GROUP BY ";
        $query .= "     OUTGO_SCH.LINE_NO, ";
        $query .= "     OUTGO_SCH.OUTGO_L_CD, ";
        $query .= "     OUTGO_SCH.OUTGO_M_CD, ";
        $query .= "     OUTGO_SCH.OUTGO_S_CD, ";
        $query .= "     VALUE(T1.OUTGO_APPROVAL, '2'), ";
        $query .= "     CASE WHEN VALUE(T1.OUTGO_APPROVAL, '0') = '1' THEN T1.OUTGO_DATE ELSE T1.REQUEST_DATE END, ";
        $query .= "     SMST.LEVY_S_NAME ";
        // 繰越伝票
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     'CARRY_OVER' as REQUEST_NO, ";
        $query .= "     1 as LINE_NO, ";
        $query .= "     '88' as OUTGO_L_CD, ";
        $query .= "     '88' as OUTGO_M_CD, ";
        $query .= "     '888' as OUTGO_S_CD, ";
        $query .= "     '1' AS OUTGO_APPROVAL, ";
        $query .= "     '9999-12-31' AS SORT_DATE, ";
        $query .= "     '繰越' as LEVY_S_NAME, ";
        $query .= "     sum(value(CARRY_OVER_MONEY, 0)) AS OUTGO_MONEY ";
        $query .= " FROM ";
        $query .= "     LEVY_CARRY_OVER_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".SCHOOLCD."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     AND YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO    = '".$schregRow["SCHREGNO"]."' ";
        $query .= "     AND value(CARRY_CANCEL, '0') = '0' ";
        $query .= "     AND INCOME_L_CD = '".$lListRow["INCOME_L_CD"]."' ";
        $query .= " GROUP BY ";
        $query .= "     INCOME_L_CD ";
        $query .= " ORDER BY ";
        $query .= "     SORT_DATE, ";
        $query .= "     OUTGO_L_CD, ";
        $query .= "     OUTGO_M_CD, ";
        $query .= "     OUTGO_S_CD ";

        return $query;
    }

        function getHasuu($model) {
            $query  = " WITH MAX_SEME AS ( ";
            $query .= "     SELECT ";
            $query .= "         SCHREGNO, ";
            $query .= "         max(SEMESTER) AS MAX_SEMESTER ";
            $query .= "     FROM ";
            $query .= "         SCHREG_REGD_DAT ";
            $query .= "     WHERE ";
            $query .= "         YEAR = '".CTRL_YEAR."' ";
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
            $query .= "             T1.YEAR  = '".CTRL_YEAR."' ";
            $query .= "         AND T1.GRADE || '-' || T1.HR_CLASS IN ('".implode("','", $this->select_data["selectdata"])."') ";
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
            $query .= "         MSAI.SCHOOLCD       = '".SCHOOLCD."' ";
            $query .= "     AND MSAI.SCHOOL_KIND    = '".$model->field["SCHOOL_KIND"]."' ";
            $query .= "     AND MSAI.YEAR           = '".CTRL_YEAR."' ";
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
?>
