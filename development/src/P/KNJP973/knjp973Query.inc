<?php

require_once('for_php7.php');

class knjp973Query extends Query {
    //学年取得
    function getGrade($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //クラス取得
    function getHrClass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     HDAT.GRADE || '-' || HDAT.HR_CLASS AS VALUE, ";
        $query .= "     HDAT.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT HDAT ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = HDAT.YEAR ";
        $query .= "                                    AND GDAT.GRADE = HDAT.GRADE ";
        $query .= " WHERE ";
        $query .= "         HDAT.YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND HDAT.SEMESTER    = '".CTRL_SEMESTER."' ";
        $query .= "     AND GDAT.SCHOOL_KIND = '".$model->schoolKind."' ";
        if ($model->field["GRADE"] != '99') {
            $query .= "     AND HDAT.GRADE       = '".$model->field["GRADE"]."' ";
        }
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND ";
            $query .= "    (HDAT.TR_CD1      = '".STAFFCD."' OR ";
            $query .= "     HDAT.TR_CD2      = '".STAFFCD."' OR ";
            $query .= "     HDAT.TR_CD3      = '".STAFFCD."' OR ";
            $query .= "     HDAT.SUBTR_CD1   = '".STAFFCD."' OR ";
            $query .= "     HDAT.SUBTR_CD2   = '".STAFFCD."' OR ";
            $query .= "     HDAT.SUBTR_CD3   = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /***********/
    /* CSV出力 */
    /***********/
    //科目名称取得
    function getLmst($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.INCOME_L_CD, ";
        $query .= "     L1.LEVY_L_NAME ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT T1 ";
        $query .= "     LEFT JOIN LEVY_L_MST L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.SCHOOLCD = L1.SCHOOLCD ";
        $query .= "          AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ";
        $query .= "          AND T1.INCOME_L_CD = L1.LEVY_L_CD ";
        $query .= "     INNER JOIN LEVY_REQUEST_INCOME_SCHREG_DAT INCOME_SCH ON T1.YEAR = INCOME_SCH.YEAR ";
        $query .= "          AND T1.SCHOOLCD    = INCOME_SCH.SCHOOLCD ";
        $query .= "          AND T1.SCHOOL_KIND = INCOME_SCH.SCHOOL_KIND ";
        $query .= "          AND T1.INCOME_L_CD = INCOME_SCH.INCOME_L_CD ";
        $query .= "          AND T1.INCOME_M_CD = INCOME_SCH.INCOME_M_CD ";
        $query .= "          AND T1.REQUEST_NO = INCOME_SCH.REQUEST_NO ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON T1.YEAR = REGD.YEAR ";
        if ($model->field["GRADE"] != "99") {
            $query .= "          AND REGD.GRADE = '".$model->field["GRADE"]."' ";
        }
        $query .= "          AND REGD.SCHREGNO = INCOME_SCH.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "     AND VALUE(T1.INCOME_APPROVAL, '0') = '1' ";
        $query .= "     AND VALUE(T1.INCOME_CANCEL, '0')   = '0' ";
        $query .= " ORDER BY ";
        $query .= "     T1.INCOME_L_CD ";

        return $query;
    }

    //生徒基本情報＋【科目コード、納入額、支出額、給付金（その他返金額）】
    function getHrClassCsv($model) {
        $selected = "('";
        if (is_array($model->select_data["selectdata"])) {
            foreach ($model->select_data["selectdata"] as $d) {
                $selected .= $comma . $d;
                $comma = "', '";
            }
        }
        $selected .= "')";

        $query  = " WITH MAX_SEME_REGD AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         max(SEMESTER) MAX_SEM ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ), INCOMETBL AS ( ";
        $query .= "     SELECT ";
        $query .= "         REGD.SCHREGNO, ";
        $query .= "         REGD.GRADE, ";
        $query .= "         REGD.HR_CLASS, ";
        $query .= "         REGDH.HR_NAME, ";
        $query .= "         REGD.ATTENDNO, ";
        $query .= "         BASE.NAME AS SCH_NAME, ";
        $query .= "         STAFF.STAFFNAME, ";
        $query .= "         INCOME.INCOME_L_CD, ";
        $query .= "         SUM(VALUE(INCOME_SCH.INCOME_MONEY, 0)) AS INCOME_MONEY ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT REGD ";
        $query .= "         LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ";
        $query .= "              AND REGD.SEMESTER = REGDH.SEMESTER ";
        $query .= "              AND REGD.GRADE = REGDH.GRADE ";
        $query .= "              AND REGD.HR_CLASS = REGDH.HR_CLASS ";
        $query .= "         LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "         LEFT JOIN LEVY_REQUEST_INCOME_DAT INCOME ON REGD.YEAR = INCOME.YEAR ";
        $query .= "              AND INCOME.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "              AND INCOME.SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "              AND VALUE(INCOME.INCOME_APPROVAL, '0') = '1' ";
        $query .= "              AND VALUE(INCOME.INCOME_CANCEL, '0') = '0' ";
        $query .= "         LEFT JOIN LEVY_REQUEST_INCOME_SCHREG_DAT INCOME_SCH ON INCOME.YEAR = INCOME_SCH.YEAR ";
        $query .= "              AND INCOME.SCHOOLCD    = INCOME_SCH.SCHOOLCD ";
        $query .= "              AND INCOME.SCHOOL_KIND = INCOME_SCH.SCHOOL_KIND ";
        $query .= "              AND INCOME.INCOME_L_CD = INCOME_SCH.INCOME_L_CD ";
        $query .= "              AND INCOME.INCOME_M_CD = INCOME_SCH.INCOME_M_CD ";
        $query .= "              AND INCOME.REQUEST_NO  = INCOME_SCH.REQUEST_NO ";
        $query .= "              AND REGD.SCHREGNO = INCOME_SCH.SCHREGNO ";
        $query .= "         LEFT JOIN STAFF_MST STAFF ON REGDH.TR_CD1 = STAFF.STAFFCD ";
        $query .= "         INNER JOIN MAX_SEME_REGD SEME ON SEME.SCHREGNO = REGD.SCHREGNO ";
        $query .= "                                      AND SEME.MAX_SEM  = REGD.SEMESTER ";
        $query .= "     WHERE ";
        $query .= "             REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "         AND REGD.GRADE || '-' || REGD.HR_CLASS IN {$selected} ";
        $query .= "     GROUP BY ";
        $query .= "         REGD.SCHREGNO, ";
        $query .= "         REGD.GRADE, ";
        $query .= "         REGD.HR_CLASS, ";
        $query .= "         REGDH.HR_NAME, ";
        $query .= "         REGD.ATTENDNO, ";
        $query .= "         BASE.NAME, ";
        $query .= "         STAFF.STAFFNAME, ";
        $query .= "         INCOME.INCOME_L_CD ";
        $query .= "     ORDER BY ";
        $query .= "         REGD.GRADE, ";
        $query .= "         REGD.HR_CLASS, ";
        $query .= "         REGD.ATTENDNO ";
        $query .= " ), OUTGOTBL AS ( ";
        $query .= "     SELECT ";
        $query .= "         REGD.SCHREGNO, ";
        $query .= "         REGD.GRADE, ";
        $query .= "         REGD.HR_CLASS, ";
        $query .= "         REGDH.HR_NAME, ";
        $query .= "         REGD.ATTENDNO, ";
        $query .= "         BASE.NAME AS SCH_NAME, ";
        $query .= "         STAFF.STAFFNAME, ";
        $query .= "         OUTGO.INCOME_L_CD, ";
        $query .= "         SUM(VALUE(OUTGO_SCH.OUTGO_MONEY, 0)) AS OUTGO_MONEY ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT REGD ";
        $query .= "         LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ";
        $query .= "              AND REGD.SEMESTER = REGDH.SEMESTER ";
        $query .= "              AND REGD.GRADE = REGDH.GRADE ";
        $query .= "              AND REGD.HR_CLASS = REGDH.HR_CLASS ";
        $query .= "         LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "         LEFT JOIN STAFF_MST STAFF ON REGDH.TR_CD1 = STAFF.STAFFCD ";
        $query .= "         LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTGO ON OUTGO.YEAR = REGD.YEAR ";
        $query .= "              AND OUTGO.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "              AND OUTGO.SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "              AND VALUE(OUTGO.OUTGO_APPROVAL, '0') = '1' ";
        $query .= "              AND VALUE(OUTGO.OUTGO_CANCEL, '0') = '0' ";
        $query .= "         LEFT JOIN LEVY_REQUEST_OUTGO_SCHREG_DAT OUTGO_SCH ON OUTGO_SCH.YEAR = OUTGO.YEAR ";
        $query .= "               AND OUTGO_SCH.SCHOOLCD    = OUTGO.SCHOOLCD ";
        $query .= "               AND OUTGO_SCH.SCHOOL_KIND = OUTGO.SCHOOL_KIND ";
        $query .= "               AND OUTGO_SCH.OUTGO_L_CD  = OUTGO.OUTGO_L_CD ";
        $query .= "               AND OUTGO_SCH.OUTGO_M_CD  = OUTGO.OUTGO_M_CD ";
        $query .= "               AND OUTGO_SCH.REQUEST_NO  = OUTGO.REQUEST_NO ";
        $query .= "               AND OUTGO_SCH.SCHREGNO = REGD.SCHREGNO ";
        $query .= "         INNER JOIN MAX_SEME_REGD SEME ON SEME.SCHREGNO = REGD.SCHREGNO ";
        $query .= "                                      AND SEME.MAX_SEM  = REGD.SEMESTER ";
        $query .= "     WHERE ";
        $query .= "               REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "           AND REGD.GRADE || '-' || REGD.HR_CLASS IN {$selected} ";
        $query .= "           AND OUTGO.OUTGO_L_CD <> '99' ";// 返金伝票は除く（最後に計算する）
        $query .= "     GROUP BY ";
        $query .= "         REGD.SCHREGNO, ";
        $query .= "         REGD.GRADE, ";
        $query .= "         REGD.HR_CLASS, ";
        $query .= "         REGDH.HR_NAME, ";
        $query .= "         REGD.ATTENDNO, ";
        $query .= "         BASE.NAME, ";
        $query .= "         STAFF.STAFFNAME, ";
        $query .= "         OUTGO.INCOME_L_CD ";
        $query .= "     ORDER BY ";
        $query .= "         REGD.GRADE, ";
        $query .= "         REGD.HR_CLASS, ";
        $query .= "         REGD.ATTENDNO ";
        $query .= " ), KYUFU_DATA as ( ";
        $query .= " SELECT ";
        $query .= "     OUTGO_SCH.SCHREGNO, ";
        $query .= "     sum(VALUE(OUTGO_SCH.OUTGO_MONEY, 0)) AS KYUFU_MONEY ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_SCHREG_DAT OUTGO_SCH ";
        $query .= "     INNER JOIN LEVY_REQUEST_OUTGO_DAT T1 ON T1.YEAR        = OUTGO_SCH.YEAR ";
        $query .= "                                         AND T1.SCHOOLCD    = OUTGO_SCH.SCHOOLCD ";
        $query .= "                                         AND T1.SCHOOL_KIND = OUTGO_SCH.SCHOOL_KIND ";
        $query .= "                                         AND T1.OUTGO_L_CD  = OUTGO_SCH.OUTGO_L_CD ";
        $query .= "                                         AND T1.OUTGO_M_CD  = OUTGO_SCH.OUTGO_M_CD ";
        $query .= "                                         AND T1.REQUEST_NO  = OUTGO_SCH.REQUEST_NO ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "     AND VALUE(T1.OUTGO_CANCEL, '0')   = '0' ";
        $query .= "     AND T1.INCOME_L_CD = '98' "; // 給付
        $query .= " GROUP BY ";
        $query .= "     OUTGO_SCH.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     CASE WHEN T1.SCHREGNO IS NOT NULL THEN T1.SCHREGNO ELSE T2.SCHREGNO END AS SCHREGNO, ";
        $query .= "     CASE WHEN T1.SCHREGNO IS NOT NULL THEN T1.GRADE ELSE T2.GRADE END AS GRADE, ";
        $query .= "     CASE WHEN T1.SCHREGNO IS NOT NULL THEN T1.HR_CLASS ELSE T2.HR_CLASS END AS HR_CLASS, ";
        $query .= "     CASE WHEN T1.SCHREGNO IS NOT NULL THEN T1.HR_NAME ELSE T2.HR_NAME END AS HR_NAME, ";
        $query .= "     CASE WHEN T1.SCHREGNO IS NOT NULL THEN T1.ATTENDNO ELSE T2.ATTENDNO END AS ATTENDNO, ";
        $query .= "     CASE WHEN T1.SCHREGNO IS NOT NULL THEN T1.SCH_NAME ELSE T2.SCH_NAME END AS SCH_NAME, ";
        $query .= "     CASE WHEN T1.SCHREGNO IS NOT NULL THEN T1.STAFFNAME ELSE T2.STAFFNAME END AS STAFFNAME, ";
        $query .= "     CASE WHEN T1.INCOME_L_CD IS NOT NULL THEN T1.INCOME_L_CD ELSE T2.INCOME_L_CD END AS INCOME_L_CD, ";
        $query .= "     T1.INCOME_MONEY, ";
        $query .= "     T2.OUTGO_MONEY, ";
        $query .= "     T3.KYUFU_MONEY ";
        $query .= " FROM ";
        $query .= "     INCOMETBL T1 ";
        $query .= "     LEFT JOIN OUTGOTBL T2 ";
        $query .= "          ON T2.SCHREGNO    = T1.SCHREGNO ";
        $query .= "         AND T2.GRADE       = T1.GRADE ";
        $query .= "         AND T2.HR_CLASS    = T1.HR_CLASS ";
        $query .= "         AND T2.HR_NAME     = T1.HR_NAME ";
        $query .= "         AND T2.ATTENDNO    = T1.ATTENDNO ";
        $query .= "         AND T2.INCOME_L_CD = T1.INCOME_L_CD ";
        $query .= "     LEFT JOIN KYUFU_DATA T3 ON T1.SCHREGNO = T3.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     ATTENDNO ";

        return $query;
    }

    //繰越金取得
    function getCarryOver($model) {
        $selected = "('";
        if (is_array($model->select_data["selectdata"])) {
            foreach ($model->select_data["selectdata"] as $d) {
                $selected .= $comma . $d;
                $comma = "', '";
            }
        }
        $selected .= "')";

        $query  = " WITH MAX_SEME_REGD AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         max(SEMESTER) MAX_SEM ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(T2.CARRY_OVER_MONEY) AS CARRYOVER ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN LEVY_CARRY_OVER_DAT T2 ON T2.YEAR     = T1.YEAR ";
        $query .= "                                     AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                     AND VALUE(T2.CARRY_CANCEL, '0') <> '1' ";
        $query .= "     INNER JOIN MAX_SEME_REGD SEME ON SEME.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                  AND SEME.MAX_SEM  = T1.SEMESTER ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.GRADE || '-' || T1.HR_CLASS IN {$selected} ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //端数返金取得
    function getHasuuHenkin($model) {
        $query  = " SELECT ";
        $query .= "     OSCH.SCHREGNO, ";
        $query .= "     sum(value(OSCH.OUTGO_MONEY, 0)) AS HASUU_HENKIN ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_SCHREG_DAT OSCH ";
        $query .= "     LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTG ON OUTG.SCHOOLCD    = OSCH.SCHOOLCD ";
        $query .= "                                          AND OUTG.SCHOOL_KIND = OSCH.SCHOOL_KIND ";
        $query .= "                                          AND OUTG.YEAR        = OSCH.YEAR ";
        $query .= "                                          AND OUTG.OUTGO_L_CD  = OSCH.OUTGO_L_CD ";
        $query .= "                                          AND OUTG.OUTGO_M_CD  = OSCH.OUTGO_M_CD ";
        $query .= "                                          AND OUTG.REQUEST_NO  = OSCH.REQUEST_NO ";
        $query .= " WHERE ";
        $query .= "         OSCH.SCHOOLCD       = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND OSCH.SCHOOL_KIND    = '".$model->schoolKind."' ";
        $query .= "     AND OSCH.YEAR           = '".CTRL_YEAR."' ";
        $query .= "     AND OUTG.OUTGO_L_CD     = '99' "; // 返金伝票
        $query .= "     AND VALUE(OUTG.OUTGO_APPROVAL, '0')  = '1' ";// 決済済み
        $query .= "     AND VALUE(OUTG.OUTGO_CANCEL, '0')    = '0' ";// 未キャンセル
        $query .= "     AND OUTG.REQUEST_NO in ( ";
        $query .= "                            SELECT ";
        $query .= "                                INS_REQUEST_NO ";
        $query .= "                            FROM ";
        $query .= "                                LEVY_REQUEST_HASUU_WORK_DAT ";
        $query .= "                            WHERE ";
        $query .= "                                    SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "                                AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "                                AND YEAR        = '".CTRL_YEAR."' ";
        $query .= "                            ) ";
        $query .= " GROUP BY ";
        $query .= "     OSCH.SCHREGNO ";

        return $query;
    }

    //返金取得
    function getHenkin($model) {
        $selected = "('";
        if (is_array($model->select_data["selectdata"])) {
            foreach ($model->select_data["selectdata"] as $d) {
                $selected .= $comma . $d;
                $comma = "', '";
            }
        }
        $selected .= "')";

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
        $query .= "         AND T1.GRADE || '-' || T1.HR_CLASS in {$selected} ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     OSCH.SCHREGNO, ";
        $query .= "     sum(value(OSCH.OUTGO_MONEY, 0)) AS HENKIN ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_SCHREG_DAT OSCH ";
        $query .= "     LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTG ON OUTG.SCHOOLCD    = OSCH.SCHOOLCD ";
        $query .= "                                          AND OUTG.SCHOOL_KIND = OSCH.SCHOOL_KIND ";
        $query .= "                                          AND OUTG.YEAR        = OSCH.YEAR ";
        $query .= "                                          AND OUTG.OUTGO_L_CD  = OSCH.OUTGO_L_CD ";
        $query .= "                                          AND OUTG.OUTGO_M_CD  = OSCH.OUTGO_M_CD ";
        $query .= "                                          AND OUTG.REQUEST_NO  = OSCH.REQUEST_NO ";
        $query .= "     INNER JOIN REGD_DATA REGD ON REGD.SCHREGNO = OSCH.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         OSCH.SCHOOLCD       = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND OSCH.SCHOOL_KIND    = '".$model->schoolKind."' ";
        $query .= "     AND OSCH.YEAR           = '".CTRL_YEAR."' ";
        $query .= "     AND OUTG.OUTGO_L_CD     = '99' "; // 返金伝票
        $query .= "     AND VALUE(OUTG.OUTGO_APPROVAL, '0')  = '1' ";// 決済済み
        $query .= "     AND VALUE(OUTG.OUTGO_CANCEL, '0')    = '0' ";// 未キャンセル
        $query .= "     AND VALUE(OUTG.HENKIN_APPROVAL, '0') = '1' ";// CSV取込で完了したデータのみ
        $query .= " GROUP BY ";
        $query .= "     OSCH.SCHREGNO ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     OSCH.SCHREGNO, ";
        $query .= "     sum(value(OSCH.OUTGO_MONEY, 0)) AS HENKIN ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_SCHREG_DAT OSCH ";
        $query .= "     LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTG ON OUTG.SCHOOLCD    = OSCH.SCHOOLCD ";
        $query .= "                                          AND OUTG.SCHOOL_KIND = OSCH.SCHOOL_KIND ";
        $query .= "                                          AND OUTG.YEAR        = OSCH.YEAR ";
        $query .= "                                          AND OUTG.OUTGO_L_CD  = OSCH.OUTGO_L_CD ";
        $query .= "                                          AND OUTG.OUTGO_M_CD  = OSCH.OUTGO_M_CD ";
        $query .= "                                          AND OUTG.REQUEST_NO  = OSCH.REQUEST_NO ";
        $query .= "     INNER JOIN REGD_DATA REGD ON REGD.SCHREGNO = OSCH.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         OSCH.SCHOOLCD       = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND OSCH.SCHOOL_KIND    = '".$model->schoolKind."' ";
        $query .= "     AND OSCH.YEAR           = '".CTRL_YEAR."' ";
        $query .= "     AND OUTG.OUTGO_L_CD    <> '99' "; // 返金伝票除く
        $query .= "     AND OUTG.INCOME_L_CD    = '98' "; // 返金伝票(給付)
        $query .= "     AND VALUE(OUTG.OUTGO_APPROVAL, '0')  = '1' ";// 決済済み
        $query .= "     AND VALUE(OUTG.OUTGO_CANCEL, '0')    = '0' ";// 未キャンセル
        $query .= "     AND VALUE(OUTG.HENKIN_APPROVAL, '0') = '1' ";// CSV取込で完了したデータのみ
        $query .= " GROUP BY ";
        $query .= "     OSCH.SCHREGNO ";

        return $query;
    }

    //手数料取得
    function getTesuryo($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     BANK_TRANSFER_FEE ";
        $query .= " FROM ";
        $query .= "     COLLECT_SCHOOL_BANK_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND IN ('".$model->schoolKind."', '99') ";
        $query .= "     AND YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND FORMAT_DIV  = '2' ";   // 1:引落 2:返金
        $query .= "     AND SEQ         = '001' "; // 固定

        return $query;
    }

    //端数取得
    function getHasuu($model) {
        $selected = "('";
        if (is_array($model->select_data["selectdata"])) {
            foreach ($model->select_data["selectdata"] as $d) {
                $selected .= $comma . $d;
                $comma = "', '";
            }
        }
        $selected .= "')";

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
        $query .= "         AND T1.GRADE || '-' || T1.HR_CLASS in {$selected} ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     OUTG.INCOME_L_CD, ";
        $query .= "     sum(value(MSAI.HASUU, 0)) AS HASUU ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_MEISAI_DAT MSAI ";
        $query .= "     LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTG ON OUTG.SCHOOLCD    = MSAI.SCHOOLCD ";
        $query .= "                                          AND OUTG.SCHOOL_KIND = MSAI.SCHOOL_KIND ";
        $query .= "                                          AND OUTG.YEAR        = MSAI.YEAR ";
        $query .= "                                          AND OUTG.OUTGO_L_CD  = MSAI.OUTGO_L_CD ";
        $query .= "                                          AND OUTG.OUTGO_M_CD  = MSAI.OUTGO_M_CD ";
        $query .= "                                          AND OUTG.REQUEST_NO  = MSAI.REQUEST_NO ";
        $query .= " WHERE ";
        $query .= "         MSAI.SCHOOLCD       = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND MSAI.SCHOOL_KIND    = '".$model->schoolKind."' ";
        $query .= "     AND MSAI.YEAR           = '".CTRL_YEAR."' ";
        $query .= "     AND VALUE(OUTG.OUTGO_APPROVAL, '0') = '1' ";// 決済済み
        $query .= "     AND VALUE(OUTG.OUTGO_CANCEL, '0')   = '0' ";
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
        $query .= " GROUP BY ";
        $query .= "     OUTG.INCOME_L_CD ";

        return $query;
    }
}
?>
