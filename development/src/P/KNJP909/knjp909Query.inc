<?php

require_once('for_php7.php');

class knjp909Query extends Query {
    /********/
    /* 画面 */
    /********/
    // 学年進行処理確認
    function checkClassFormationDat($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CLASS_FORMATION_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".($model->year + 1)."' ";

        return $query;
    }

    // 学年、校種取得
    function getGdat($model, $grade="") {
        $query  = " SELECT ";
        if ($grade != '') {
            $query .= "     SCHOOL_KIND ";
        } else {
            $query .= "     GRADE as VALUE, ";
            $query .= "     GRADE_NAME1 as LABEL, ";
            $query .= "     SCHOOL_KIND ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        if ($grade != '') {
            $query .= "     AND GRADE = '{$grade}' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //清算済み生徒
    function setSeisanDone($db, $model) {

        //清算済み生徒テーブル削除
        $query  = " DELETE FROM LEVY_REQUEST_SEISAN_DONE_SCHREG_DAT ";
        $db->query($query);

        $query  = " INSERT INTO LEVY_REQUEST_SEISAN_DONE_SCHREG_DAT ";
        $query .= " WITH CARRY_OVER AS ( ";
        $query .= " SELECT ";
        $query .= "    T1.SCHREGNO, ";
        $query .= "    SUM(T1.CARRY_OVER_MONEY) AS CARRYOVER ";
        $query .= " FROM ";
        $query .= "    LEVY_CARRY_OVER_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND VALUE(T1.CARRY_CANCEL, '0') <> '1' ";
        $query .= " GROUP BY ";
        $query .= "    T1.SCHREGNO ";
        $query .= " ), HASUU_DATA AS ( ";
        $query .= " SELECT ";
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
        $query .= "     AND OSCH.YEAR           = '{$model->year}' ";
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
        $query .= "                                AND YEAR        = '{$model->year}' ";
        $query .= "                            ) ";
        $query .= " GROUP BY ";
        $query .= "     OSCH.SCHREGNO ";
        $query .= " ), HENKIN_T AS ( ";
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
        $query .= " WHERE ";
        $query .= "         OSCH.SCHOOLCD       = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND OSCH.YEAR           = '{$model->year}' ";
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
        $query .= " WHERE ";
        $query .= "         OSCH.SCHOOLCD       = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND OSCH.YEAR           = '{$model->year}' ";
        $query .= "     AND OUTG.OUTGO_L_CD    <> '99' "; // 返金伝票除く
        $query .= "     AND OUTG.INCOME_L_CD    = '98' "; // 返金伝票(給付)
        $query .= "     AND VALUE(OUTG.OUTGO_APPROVAL, '0')  = '1' ";// 決済済み
        $query .= "     AND VALUE(OUTG.OUTGO_CANCEL, '0')    = '0' ";// 未キャンセル
        $query .= "     AND VALUE(OUTG.HENKIN_APPROVAL, '0') = '1' ";// CSV取込で完了したデータのみ
        $query .= " GROUP BY ";
        $query .= "     OSCH.SCHREGNO ";
        $query .= " ), HENKIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         sum(value(HENKIN, 0)) AS HENKIN ";
        $query .= "     FROM ";
        $query .= "         HENKIN_T ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ), MAX_SEME_REGD AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         max(SEMESTER) MAX_SEM ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->year}' ";
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
        $query .= "             REGD.YEAR     = '{$model->year}' ";
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
        $query .= "               REGD.YEAR     = '{$model->year}' ";
        $query .= "           AND OUTGO.OUTGO_L_CD <> '99' "; // 返金伝票は除く（最後に計算する）
        $query .= "           AND OUTGO.INCOME_L_CD <> '98' "; // 給付伝票は除く（最後に計算する）
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
        $query .= "         T1.YEAR        = '{$model->year}' ";
        $query .= "     AND T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND VALUE(T1.OUTGO_CANCEL, '0')   = '0' ";
        $query .= "     AND T1.INCOME_L_CD = '98' "; // 給付
        $query .= " GROUP BY ";
        $query .= "     OUTGO_SCH.SCHREGNO ";
        $query .= " ), MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     CASE WHEN T1.SCHREGNO IS NOT NULL THEN T1.SCHREGNO ELSE T2.SCHREGNO END AS SCHREGNO, ";
        $query .= "     SUM(VALUE(T1.INCOME_MONEY, 0)) AS INCOME_MONEY, ";
        $query .= "     SUM(VALUE(T2.OUTGO_MONEY, 0)) AS OUTGO_MONEY ";
        $query .= " FROM ";
        $query .= "     INCOMETBL T1 ";
        $query .= "     LEFT JOIN OUTGOTBL T2 ";
        $query .= "          ON T2.SCHREGNO    = T1.SCHREGNO ";
        $query .= "         AND T2.GRADE       = T1.GRADE ";
        $query .= "         AND T2.HR_CLASS    = T1.HR_CLASS ";
        $query .= "         AND T2.HR_NAME     = T1.HR_NAME ";
        $query .= "         AND T2.ATTENDNO    = T1.ATTENDNO ";
        $query .= "         AND T2.INCOME_L_CD = T1.INCOME_L_CD ";
        $query .= " GROUP BY ";
        $query .= "     CASE WHEN T1.SCHREGNO IS NOT NULL THEN T1.SCHREGNO ELSE T2.SCHREGNO END ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     MAIN_T.SCHREGNO, ";
        $query .= "     VALUE(MAIN_T.INCOME_MONEY, 0) AS INCOME_MONEY, ";
        $query .= "     VALUE(MAIN_T.OUTGO_MONEY, 0) AS OUTGO_MONEY, ";
        $query .= "     VALUE(CARRY_OVER.CARRYOVER, 0) AS CARRYOVER, ";
        $query .= "     VALUE(KYUFU_DATA.KYUFU_MONEY, 0) AS KYUFU_MONEY, ";
        $query .= "     VALUE(HASUU_DATA.HASUU_HENKIN, 0) AS v, ";
        $query .= "     VALUE(HENKIN.HENKIN, 0) AS HENKIN, ";
        $query .= "     VALUE(MAIN_T.INCOME_MONEY, 0) ";
        $query .= "     - VALUE(MAIN_T.OUTGO_MONEY, 0) ";
        $query .= "     - VALUE(CARRY_OVER.CARRYOVER, 0) ";
        if ($model->Properties["useBenefit"] == "1") {
            $query .= "     + VALUE(KYUFU_DATA.KYUFU_MONEY, 0) ";
        }
        $query .= "     + VALUE(HASUU_DATA.HASUU_HENKIN, 0) ";
        $query .= "     - VALUE(HENKIN.HENKIN, 0) ";
        $query .= "     AS ZANDAKA, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= "     LEFT JOIN KYUFU_DATA ON MAIN_T.SCHREGNO = KYUFU_DATA.SCHREGNO ";
        $query .= "     LEFT JOIN CARRY_OVER ON MAIN_T.SCHREGNO = CARRY_OVER.SCHREGNO ";
        $query .= "     LEFT JOIN HASUU_DATA ON MAIN_T.SCHREGNO = HASUU_DATA.SCHREGNO ";
        $query .= "     LEFT JOIN HENKIN     ON MAIN_T.SCHREGNO = HENKIN.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     VALUE(MAIN_T.INCOME_MONEY, 0) ";
        $query .= "     - VALUE(MAIN_T.OUTGO_MONEY, 0) ";
        $query .= "     - VALUE(CARRY_OVER.CARRYOVER, 0) ";
        if ($model->Properties["useBenefit"] == "1") {
            $query .= "     + VALUE(KYUFU_DATA.KYUFU_MONEY, 0) ";
        }
        $query .= "     + VALUE(HASUU_DATA.HASUU_HENKIN, 0) ";
        $query .= "     - VALUE(HENKIN.HENKIN, 0) = 0 ";
        $query .= "     AND EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             SCHREG_BASE_MST BASE ";
        $query .= "         WHERE ";
        $query .= "             MAIN_T.SCHREGNO = BASE.SCHREGNO ";
        $query .= "             AND BASE.GRD_DIV IS NOT NULL ";
        $query .= "             AND BASE.GRD_DIV NOT IN ('4', '5') ";
        $query .= "             AND FISCALYEAR(BASE.GRD_DATE) <= '{$model->year}' ";
        $query .= "     ) ";
        $db->query($query);

        return ;
    }

    //端数項目取得
    function getHasuuMeisaiDat($model) {
        $query  = " WITH HIST_DAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         TO_REQUEST_NO, ";
        $query .= "         TO_LINE_NO, ";
        $query .= "         TO_OUTGO_L_CD, ";
        $query .= "         TO_OUTGO_M_CD, ";
        $query .= "         TO_OUTGO_S_CD, ";
        $query .= "         TO_REQUEST_NO || rtrim(cast(TO_LINE_NO as char(2))) || TO_OUTGO_L_CD || TO_OUTGO_M_CD || TO_OUTGO_S_CD as REQ_LINE_NO ";
        $query .= "     FROM ";
        $query .= "         LEVY_REQUEST_HASUU_MOVE_HIST_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND YEAR        = '{$model->year}' ";
        $query .= "     GROUP BY ";
        $query .= "         TO_REQUEST_NO, ";
        $query .= "         TO_LINE_NO, ";
        $query .= "         TO_OUTGO_L_CD, ";
        $query .= "         TO_OUTGO_M_CD, ";
        $query .= "         TO_OUTGO_S_CD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     MEISAI.SCHOOL_KIND || '-' || MEISAI.REQUEST_NO || '-' || rtrim(cast(MEISAI.LINE_NO as char(2))) || '-' || ";
        $query .= "     MEISAI.OUTGO_L_CD || '-' || MEISAI.OUTGO_M_CD || '-' || MEISAI.OUTGO_S_CD || ':' || rtrim(cast(MEISAI.HASUU as char(8))) AS VALUE, ";
        $query .= "     case ";
        $query .= "         when HIST.REQ_LINE_NO is not null then '●' ";
        $query .= "         else '' ";
        $query .= "     end || ";
        $query .= "     value(M_MST.LEVY_M_NAME, '') || '(' || MEISAI.REQUEST_NO || '-' || rtrim(cast(MEISAI.LINE_NO as char(2))) || ')' AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_MEISAI_DAT MEISAI ";
        $query .= "     LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTGO ON OUTGO.SCHOOLCD    = MEISAI.SCHOOLCD ";
        $query .= "                                           AND OUTGO.SCHOOL_KIND = MEISAI.SCHOOL_KIND ";
        $query .= "                                           AND OUTGO.YEAR        = MEISAI.YEAR ";
        $query .= "                                           AND OUTGO.OUTGO_L_CD  = MEISAI.OUTGO_L_CD ";
        $query .= "                                           AND OUTGO.OUTGO_M_CD  = MEISAI.OUTGO_M_CD ";
        $query .= "                                           AND OUTGO.REQUEST_NO  = MEISAI.REQUEST_NO ";
        $query .= "     LEFT JOIN LEVY_M_MST M_MST ON M_MST.SCHOOLCD    = MEISAI.SCHOOLCD ";
        $query .= "                               AND M_MST.SCHOOL_KIND = MEISAI.SCHOOL_KIND ";
        $query .= "                               AND M_MST.YEAR        = MEISAI.YEAR ";
        $query .= "                               AND M_MST.LEVY_L_CD   = MEISAI.OUTGO_L_CD ";
        $query .= "                               AND M_MST.LEVY_M_CD   = MEISAI.OUTGO_M_CD ";
        $query .= "     INNER JOIN HIST_DAT HIST ON HIST.TO_REQUEST_NO = MEISAI.REQUEST_NO ";
        $query .= "                            AND HIST.TO_LINE_NO    = MEISAI.LINE_NO ";
        $query .= "                            AND HIST.TO_OUTGO_L_CD = MEISAI.OUTGO_L_CD ";
        $query .= "                            AND HIST.TO_OUTGO_M_CD = MEISAI.OUTGO_M_CD ";
        $query .= "                            AND HIST.TO_OUTGO_S_CD = MEISAI.OUTGO_S_CD ";
        $query .= " WHERE ";
        $query .= "         MEISAI.SCHOOLCD      = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND MEISAI.YEAR          = '{$model->year}' ";
        $query .= "     AND OUTGO.OUTGO_APPROVAL = '1' ";
        $query .= "     AND OUTGO.OUTGO_CANCEL   is null ";
        $query .= "     AND MEISAI.HASUU         > 0 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    // 最終端数振替先項目取得
    function getHasuuLMcd($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND || '-' || LEVY_L_CD || '-' || LEVY_M_CD AS VALUE, ";
        $query .= "     SCHOOL_KIND || '-' || LEVY_L_CD || LEVY_M_CD || ':' || LEVY_M_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_M_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND YEAR            = '{$model->year}' ";
        $query .= "     AND LEVY_IN_OUT_DIV = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    // 実行履歴あるか
    function getLevyCloseGradeDat($model) {
        $query  = " SELECT ";
        $query .= "     CLOSE_FLG ";
        $query .= " FROM ";
        $query .= "     LEVY_CLOSE_GRADE_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND YEAR        = '{$model->year}' ";
        $query .= "     AND GRADE       = '{$model->grade}' ";

        return $query;
    }

    // 科目項目マスタ情報取得
    function getLevyLMdat($model) {
        $query  = " SELECT ";
        if ($model->grade == '99') {
            $query .= "     M_MST.SCHOOL_KIND || '-' || ";
        }
        $query .= "     M_MST.LEVY_L_CD || M_MST.LEVY_M_CD as LM_CD, ";
        $query .= "     L_MST.LEVY_L_NAME, ";
        $query .= "     L_MST.LEVY_L_ABBV, ";
        $query .= "     M_MST.LEVY_M_NAME, ";
        $query .= "     M_MST.LEVY_M_ABBV ";
        $query .= " FROM ";
        $query .= "     LEVY_M_MST M_MST ";
        $query .= "     LEFT JOIN LEVY_L_MST L_MST ON L_MST.SCHOOLCD    = M_MST.SCHOOLCD ";
        $query .= "                               AND L_MST.SCHOOL_KIND = M_MST.SCHOOL_KIND ";
        $query .= "                               AND L_MST.YEAR        = M_MST.YEAR ";
        $query .= "                               AND L_MST.LEVY_L_CD   = M_MST.LEVY_L_CD ";
        $query .= " WHERE ";
        $query .= "         M_MST.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        if ($model->grade != '99') {
            $query .= "     AND M_MST.SCHOOL_KIND = '{$model->schoolKind}' ";
        }
        $query .= "     AND M_MST.YEAR        = '{$model->year}' ";

        return $query;
    }

    // 前年度繰越情報取得
    function getCarryOverIncomeFlgLast($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     LEVY_CARRY_OVER_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD      = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND YEAR          = '{$model->lastYear}' ";
        $query .= "     AND value(TO_INCOME_FLG, '0') = '0' ";
        $query .= "     AND value(CARRY_CANCEL, '0')  = '0' ";

        return $query;
    }

    // 今年度繰越情報取得
    function getCarryOverIncomeFlg($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     LEVY_CARRY_OVER_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD      = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND YEAR          = '{$model->year}' ";
        $query .= "     AND value(TO_INCOME_FLG, '0') = '1' ";
        $query .= "     AND value(CARRY_CANCEL, '0')  = '0' ";

        return $query;
    }

    /************/
    /* 処理実行 */
    /************/
    //更新
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //精算済みの人は処理対象外とする
        knjp909Query::setSeisanDone($db, $model);

        //返金伝票キャンセル、繰越テーブル、収入伺、削除
        knjp909Query::updateCancelDelete($db, $model);

        //繰越、返金伝票 作成カウント
        $kCnt = $hCnt = 0;

        $query = knjp909Query::getIncomeOutgoData($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            //生徒毎に更新
            $requestDiv = knjp909Query::updateLevyOutgo($db, $model, $row);

            if ($requestDiv["KURI"] == '1') { //繰越
                $kCnt++;
            }
            if ($requestDiv["HEN"]  == '1') { //返金
                $hCnt++;
            }
        }

        $incomeMsg = '';
        // 返金端数項目（伝票）が指定されている時、
        if ($model->hasuuCd != '') {
            $requestArr = knjp909Query::InsertHasuuHenkinCarry($db, $model);
            if ($requestArr["INCOME"] == '1') {
                $incomeMsg = '、収入伺1件';
            }
            $hCnt += $requestArr["HEN"];
        }

        // LEVY_CLOSE_GRADE_DAT更新
        knjp909Query::updateLevyColseGradeDat($db, $model);

        $db->commit();
        Query::dbCheckIn($db);

        $setMassage = "繰越伝票{$kCnt}件、返金伝票{$hCnt}件{$incomeMsg}";

        return $setMassage;
    }

    // 返金伝票キャンセル、繰越テーブル、収入伺、削除
    function updateCancelDelete($db, $model) {
        //返金キャンセル
        $data = array();
        $data["OUTGO_CANCEL"][TEXT] = '1';
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "         SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "     AND YEAR            = '{$model->year}' ";
        $where .= "     AND OUTGO_L_CD      = '99' ";
        $where .= "     AND INCOME_L_CD    <> '98' ";
        $where .= "     AND value(OUTGO_CANCEL, '0')    = '0' ";
        $where .= "     AND value(HENKIN_APPROVAL, '0') = '0' ";
        if ($model->grade != '99') {
            $where .= "     AND SCHOOL_KIND     = '{$model->schoolKind}' ";
            $where .= "     AND REQUEST_NO in ( SELECT ";
            $where .= "                             REQUEST_NO ";
            $where .= "                         FROM ";
            $where .= "                             LEVY_REQUEST_OUTGO_SCHREG_DAT ";
            $where .= "                         WHERE ";
            $where .= "                             SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $where .= "                         AND SCHOOL_KIND = '{$model->schoolKind}' ";
            $where .= "                         AND YEAR        = '".$model->year."' ";
            $where .= "                         AND SCHREGNO    in (SELECT DISTINCT ";
            $where .= "                                                 SCHREGNO ";
            $where .= "                                             FROM ";
            $where .= "                                                 SCHREG_REGD_DAT ";
            $where .= "                                             WHERE ";
            $where .= "                                                     YEAR  = '{$model->year}' ";
            $where .= "                                                 AND GRADE = '{$model->grade}' ";
            $where .= "                                             ) ";
            $where .= "                         AND OUTGO_L_CD  = '99' ";
            $where .= "                        ) ";
        }

        $query = Query::updateSQL($data, "LEVY_REQUEST_OUTGO_DAT", $where);
        $db->query($query);

        //繰越テーブル削除
        $query  = " DELETE FROM LEVY_CARRY_OVER_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND YEAR        = '".$model->year."' ";
        $query .= "     AND SCHREGNO NOT IN ( ";
        $query .= "         SELECT ";
        $query .= "             SEISAN_DONE.SCHREGNO ";
        $query .= "         FROM ";
        $query .= "             LEVY_REQUEST_SEISAN_DONE_SCHREG_DAT SEISAN_DONE ";
        $query .= "     ) ";
        if ($model->grade != '99') {
            $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
            $query .= "     AND (   SCHREGNO in (SELECT DISTINCT ";
            $query .= "                              SCHREGNO ";
            $query .= "                          FROM ";
            $query .= "                              SCHREG_REGD_DAT ";
            $query .= "                          WHERE ";
            $query .= "                                  YEAR  = '{$model->year}' ";
            $query .= "                              AND GRADE = '{$model->grade}' ";
            $query .= "                          ) ";
            $query .= "          OR SCHREGNO in ('HASUU', 'NOT_SCH') ";
            $query .= "          ) ";
        }
        $db->query($query);

        // 収入伺削除
        if ($model->hasuuCd != '') {
            $query  = " DELETE FROM LEVY_REQUEST_INCOME_MEISAI_DAT ";
            $query .= " WHERE ";
            $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "     AND YEAR        = '{$model->year}' ";
            $query .= "     AND SCHOOL_KIND || REQUEST_NO in ( SELECT ";
            $query .= "                             SCHOOL_KIND || REQUEST_NO ";
            $query .= "                         FROM ";
            $query .= "                             LEVY_REQUEST_INCOME_DAT ";
            $query .= "                         WHERE ";
            $query .= "                                 SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "                             AND YEAR        = '{$model->year}' ";
            $query .= "                             AND COLLECT_DIV = '4' ";
            $query .= "                         ) ";
            $db->query($query);

            $query  = " DELETE FROM LEVY_REQUEST_INCOME_DAT ";
            $query .= " WHERE ";
            $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "     AND YEAR        = '{$model->year}' ";
            $query .= "     AND COLLECT_DIV = '4' ";
            $db->query($query);
        }

        return;
    }

    // 収入 - 支出 情報取得
    function getIncomeOutgoData($model) {
        //学籍情報
        $query  = " WITH MAX_SEME AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         max(SEMESTER) AS MAX_SEMESTER ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->year}' ";
        $query .= "         AND SCHREGNO NOT IN ( ";
        $query .= "             SELECT ";
        $query .= "                 SEISAN_DONE.SCHREGNO ";
        $query .= "             FROM ";
        $query .= "                 LEVY_REQUEST_SEISAN_DONE_SCHREG_DAT SEISAN_DONE ";
        $query .= "         ) ";
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
        if ($model->grade != '99') {
            $query .= "         AND T1.GRADE = '{$model->grade}' ";
        }
        //収入伝票
        $query .= " ), INCOME_DATA_0 AS ( ";
        $query .= "     SELECT ";
        $query .= "         MSAI.SCHOOLCD, ";
        $query .= "         MSAI.SCHOOL_KIND, ";
        $query .= "         MSAI.YEAR, ";
        $query .= "         MSAI.INCOME_L_CD, ";
        $query .= "         MSAI.INCOME_M_CD, ";
        $query .= "         case when ISCH.SCHREGNO is null then 'NOT_SCH' ";
        $query .= "              else ISCH.SCHREGNO ";
        $query .= "         end AS SCHREGNO, ";
        $query .= "         case when WARIHURI_DIV = '1' then sum(ISCH.INCOME_MONEY) ";
        $query .= "              else sum(TOTAL_PRICE) ";
        $query .= "         end AS TOTAL_INCOME_MONEY, ";
        $query .= "         MSAI.WARIHURI_DIV ";
        $query .= "     FROM ";
        $query .= "         LEVY_REQUEST_INCOME_MEISAI_DAT MSAI ";
        $query .= "         LEFT JOIN LEVY_REQUEST_INCOME_SCHREG_DAT ISCH ON ISCH.SCHOOLCD    = MSAI.SCHOOLCD ";
        $query .= "                                                      AND ISCH.SCHOOL_KIND = MSAI.SCHOOL_KIND ";
        $query .= "                                                      AND ISCH.YEAR        = MSAI.YEAR ";
        $query .= "                                                      AND ISCH.INCOME_L_CD = MSAI.INCOME_L_CD ";
        $query .= "                                                      AND ISCH.INCOME_M_CD = MSAI.INCOME_M_CD ";
        $query .= "                                                      AND ISCH.REQUEST_NO  = MSAI.REQUEST_NO ";
        $query .= "                                                      AND ISCH.INCOME_S_CD = MSAI.INCOME_S_CD ";
        $query .= "                                                      AND ISCH.LINE_NO     = MSAI.LINE_NO ";
        $query .= "         LEFT JOIN LEVY_REQUEST_INCOME_DAT INCO ON INCO.SCHOOLCD    = MSAI.SCHOOLCD ";
        $query .= "                                               AND INCO.SCHOOL_KIND = MSAI.SCHOOL_KIND ";
        $query .= "                                               AND INCO.YEAR        = MSAI.YEAR ";
        $query .= "                                               AND INCO.INCOME_L_CD = MSAI.INCOME_L_CD ";
        $query .= "                                               AND INCO.INCOME_M_CD = MSAI.INCOME_M_CD ";
        $query .= "                                               AND INCO.REQUEST_NO  = MSAI.REQUEST_NO ";
        $query .= "     WHERE ";
        $query .= "             MSAI.SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        if ($model->grade != '99') {
            $query .= "         AND MSAI.SCHOOL_KIND     = '{$model->schoolKind}' ";
        }
        $query .= "         AND MSAI.YEAR            = '{$model->year}' ";
        $query .= "         AND INCO.INCOME_APPROVAL = '1' ";
        $query .= "         AND INCO.INCOME_CANCEL   is null ";
        $query .= "         AND INCO.INCOME_L_CD not in ('99') ";
        $query .= "     GROUP BY ";
        $query .= "         MSAI.SCHOOLCD, ";
        $query .= "         MSAI.SCHOOL_KIND, ";
        $query .= "         MSAI.YEAR, ";
        $query .= "         MSAI.INCOME_L_CD, ";
        $query .= "         MSAI.INCOME_M_CD, ";
        $query .= "         MSAI.WARIHURI_DIV, ";
        $query .= "         ISCH.SCHREGNO ";
        $query .= " ), INCOME_DATA AS ( ";
        $query .= "     SELECT "; // (生徒)
        $query .= "         MAIN.SCHOOLCD, ";
        $query .= "         MAIN.SCHOOL_KIND, ";
        $query .= "         MAIN.YEAR, ";
        $query .= "         MAIN.INCOME_L_CD, ";
        $query .= "         MAIN.INCOME_M_CD, ";
        $query .= "         MAIN.SCHREGNO, ";
        $query .= "         value(MAIN.TOTAL_INCOME_MONEY, 0) - value(CARY.CARRY_OVER_MONEY, 0) as TOTAL_INCOME_MONEY, ";
        $query .= "         MAIN.WARIHURI_DIV ";
        $query .= "     FROM ";
        $query .= "         INCOME_DATA_0 MAIN ";
        $query .= "         INNER JOIN REGD_DATA REGD ON REGD.SCHREGNO = MAIN.SCHREGNO ";
        $query .= "         LEFT JOIN LEVY_CARRY_OVER_DAT CARY ON CARY.SCHOOLCD    = MAIN.SCHOOLCD ";
        $query .= "                                           AND CARY.SCHOOL_KIND = MAIN.SCHOOL_KIND ";
        $query .= "                                           AND CARY.YEAR        = MAIN.YEAR ";
        $query .= "                                           AND CARY.SCHREGNO    = MAIN.SCHREGNO ";
        $query .= "                                           AND CARY.INCOME_L_CD = MAIN.INCOME_L_CD ";
        $query .= "                                           AND CARY.INCOME_M_CD = MAIN.INCOME_M_CD ";
        $query .= "     UNION ";
        $query .= "     SELECT "; // (生徒以外)
        $query .= "         MAIN.SCHOOLCD, ";
        $query .= "         MAIN.SCHOOL_KIND, ";
        $query .= "         MAIN.YEAR, ";
        $query .= "         MAIN.INCOME_L_CD, ";
        $query .= "         MAIN.INCOME_M_CD, ";
        $query .= "         MAIN.SCHREGNO, ";
        $query .= "         value(MAIN.TOTAL_INCOME_MONEY, 0) - value(CARY.CARRY_OVER_MONEY, 0) as TOTAL_INCOME_MONEY, ";
        $query .= "         MAIN.WARIHURI_DIV ";
        $query .= "     FROM ";
        $query .= "         INCOME_DATA_0 MAIN ";
        $query .= "         LEFT JOIN LEVY_CARRY_OVER_DAT CARY ON CARY.SCHOOLCD    = MAIN.SCHOOLCD ";
        $query .= "                                           AND CARY.SCHOOL_KIND = MAIN.SCHOOL_KIND ";
        $query .= "                                           AND CARY.YEAR        = MAIN.YEAR ";
        $query .= "                                           AND CARY.SCHREGNO    = MAIN.SCHREGNO ";
        $query .= "                                           AND CARY.INCOME_L_CD = MAIN.INCOME_L_CD ";
        $query .= "                                           AND CARY.INCOME_M_CD = MAIN.INCOME_M_CD ";
        $query .= "     WHERE ";
        $query .= "         MAIN.SCHREGNO = 'NOT_SCH' ";
        //支出伝票
        $query .= " ), OUTGO_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         MSAI.SCHOOLCD, ";
        $query .= "         MSAI.SCHOOL_KIND, ";
        $query .= "         MSAI.YEAR, ";
        $query .= "         OUTG.INCOME_L_CD, ";
        $query .= "         OUTG.INCOME_M_CD, ";
        $query .= "         case when OSCH.SCHREGNO is null then 'NOT_SCH' ";
        $query .= "              else OSCH.SCHREGNO ";
        $query .= "         end AS SCHREGNO, ";
        $query .= "         case when MSAI.WARIHURI_DIV = '1' then sum(OSCH.OUTGO_MONEY) ";
        $query .= "              else sum(MSAI.TOTAL_PRICE) ";
        $query .= "         end AS TOTAL_OUTGO_MONEY, ";
        $query .= "         MSAI.WARIHURI_DIV ";
        $query .= "     FROM ";
        $query .= "         LEVY_REQUEST_OUTGO_MEISAI_DAT MSAI ";
        $query .= "         LEFT JOIN LEVY_REQUEST_OUTGO_SCHREG_DAT OSCH ON OSCH.SCHOOLCD    = MSAI.SCHOOLCD ";
        $query .= "                                                     AND OSCH.SCHOOL_KIND = MSAI.SCHOOL_KIND ";
        $query .= "                                                     AND OSCH.YEAR        = MSAI.YEAR ";
        $query .= "                                                     AND OSCH.OUTGO_L_CD  = MSAI.OUTGO_L_CD ";
        $query .= "                                                     AND OSCH.OUTGO_M_CD  = MSAI.OUTGO_M_CD ";
        $query .= "                                                     AND OSCH.REQUEST_NO  = MSAI.REQUEST_NO ";
        $query .= "                                                     AND OSCH.OUTGO_S_CD  = MSAI.OUTGO_S_CD ";
        $query .= "                                                     AND OSCH.LINE_NO     = MSAI.LINE_NO ";
        $query .= "         LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTG ON OUTG.SCHOOLCD    = MSAI.SCHOOLCD ";
        $query .= "                                              AND OUTG.SCHOOL_KIND = MSAI.SCHOOL_KIND ";
        $query .= "                                              AND OUTG.YEAR        = MSAI.YEAR ";
        $query .= "                                              AND OUTG.OUTGO_L_CD  = MSAI.OUTGO_L_CD ";
        $query .= "                                              AND OUTG.OUTGO_M_CD  = MSAI.OUTGO_M_CD ";
        $query .= "                                              AND OUTG.REQUEST_NO  = MSAI.REQUEST_NO ";
        $query .= "     WHERE ";
        $query .= "             MSAI.SCHOOLCD       = '".(sprintf("%012d", SCHOOLCD))."' ";
        if ($model->grade != '99') {
            $query .= "         AND MSAI.SCHOOL_KIND    = '{$model->schoolKind}' ";
        }
        $query .= "         AND MSAI.YEAR           = '{$model->year}' ";
        $query .= "         AND OUTG.OUTGO_APPROVAL = '1' ";
        $query .= "         AND OUTG.OUTGO_CANCEL   is null ";
        $query .= "     GROUP BY ";
        $query .= "         MSAI.SCHOOLCD, ";
        $query .= "         MSAI.SCHOOL_KIND, ";
        $query .= "         MSAI.YEAR, ";
        $query .= "         OUTG.INCOME_L_CD, ";
        $query .= "         OUTG.INCOME_M_CD, ";
        $query .= "         MSAI.WARIHURI_DIV, ";
        $query .= "         OSCH.SCHREGNO ";
        //端数伝票
        $query .= " ), HASUU_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         MSAI.SCHOOLCD, ";
        $query .= "         MSAI.SCHOOL_KIND, ";
        $query .= "         MSAI.YEAR, ";
        $query .= "         OUTG.INCOME_L_CD, ";
        $query .= "         OUTG.INCOME_M_CD, ";
        $query .= "         'HASUU' AS SCHREGNO, ";
        $query .= "         sum(value(MSAI.HASUU, 0)) AS TOTAL_HASUU ";
        $query .= "     FROM ";
        $query .= "         LEVY_REQUEST_OUTGO_MEISAI_DAT MSAI ";
        $query .= "         LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTG ON OUTG.SCHOOLCD    = MSAI.SCHOOLCD ";
        $query .= "                                              AND OUTG.SCHOOL_KIND = MSAI.SCHOOL_KIND ";
        $query .= "                                              AND OUTG.YEAR        = MSAI.YEAR ";
        $query .= "                                              AND OUTG.OUTGO_L_CD  = MSAI.OUTGO_L_CD ";
        $query .= "                                              AND OUTG.OUTGO_M_CD  = MSAI.OUTGO_M_CD ";
        $query .= "                                              AND OUTG.REQUEST_NO  = MSAI.REQUEST_NO ";
        $query .= "     WHERE ";
        $query .= "             MSAI.SCHOOLCD       = '".(sprintf("%012d", SCHOOLCD))."' ";
        if ($model->grade != '99') {
            $query .= "         AND MSAI.SCHOOL_KIND    = '{$model->schoolKind}' ";
        }
        $query .= "         AND MSAI.YEAR           = '{$model->year}' ";
        $query .= "         AND OUTG.OUTGO_APPROVAL = '1' ";
        $query .= "         AND OUTG.OUTGO_CANCEL   is null ";
        $query .= "         AND OUTG.HENKIN_FLG     is null ";
        $query .= "         AND value(MSAI.HASUU, 0) <> 0 ";
        // 返金端数項目（伝票）が指定されている時は、それを除く
        if ($model->hasuuCd != '') {
            list($tmpCd, $hasuu) = explode(':', $model->hasuuCd);
            $toLMcd = str_replace('-', '', $tmpCd);

            $query .= "     AND MSAI.SCHOOL_KIND || MSAI.REQUEST_NO || rtrim(cast(MSAI.LINE_NO as char(2))) || ";
            $query .= "         MSAI.OUTGO_L_CD || MSAI.OUTGO_M_CD || MSAI.OUTGO_S_CD <> '{$toLMcd}' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         MSAI.SCHOOLCD, ";
        $query .= "         MSAI.SCHOOL_KIND, ";
        $query .= "         MSAI.YEAR, ";
        $query .= "         OUTG.INCOME_L_CD, ";
        $query .= "         OUTG.INCOME_M_CD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     INCM.SCHOOLCD, ";
        $query .= "     INCM.SCHOOL_KIND, ";
        $query .= "     INCM.YEAR, ";
        $query .= "     INCM.INCOME_L_CD, ";
        $query .= "     INCM.INCOME_M_CD, ";
        $query .= "     INCM.SCHREGNO, ";
        $query .= "     INCM.WARIHURI_DIV, ";
        $query .= "     INCM.TOTAL_INCOME_MONEY, ";
        $query .= "     OUTG.TOTAL_OUTGO_MONEY, ";
        $query .= "     value(INCM.TOTAL_INCOME_MONEY, 0) - value(OUTG.TOTAL_OUTGO_MONEY, 0) as RESULT_MONEY, ";
        $query .= "     MMST.KURIKOSI_FLG, ";
        $query .= "     MMST.KURIKOSI_ALL, ";
        $query .= "     MMST.KURIKOSI_MONEY ";
        $query .= " FROM ";
        $query .= "     INCOME_DATA INCM ";
        $query .= "     LEFT JOIN OUTGO_DATA OUTG ON OUTG.SCHOOLCD    = INCM.SCHOOLCD ";
        $query .= "                              AND OUTG.SCHOOL_KIND = INCM.SCHOOL_KIND ";
        $query .= "                              AND OUTG.YEAR        = INCM.YEAR ";
        $query .= "                              AND OUTG.INCOME_L_CD = INCM.INCOME_L_CD ";
        $query .= "                              AND OUTG.INCOME_M_CD = INCM.INCOME_M_CD ";
        $query .= "                              AND OUTG.SCHREGNO    = INCM.SCHREGNO ";
        $query .= "     LEFT JOIN LEVY_M_MST MMST ON MMST.SCHOOLCD    = INCM.SCHOOLCD ";
        $query .= "                              AND MMST.SCHOOL_KIND = INCM.SCHOOL_KIND ";
        $query .= "                              AND MMST.YEAR        = INCM.YEAR ";
        $query .= "                              AND MMST.LEVY_L_CD   = INCM.INCOME_L_CD ";
        $query .= "                              AND MMST.LEVY_M_CD   = INCM.INCOME_M_CD ";
        $query .= " WHERE ";
        $query .= "     INCM.SCHREGNO NOT IN ( ";
        $query .= "         SELECT ";
        $query .= "             SEISAN_DONE.SCHREGNO ";
        $query .= "         FROM ";
        $query .= "             LEVY_REQUEST_SEISAN_DONE_SCHREG_DAT SEISAN_DONE ";
        $query .= "     ) ";
        $query .= " UNION ";
        $query .= " SELECT "; // 端数伝票
        $query .= "     SCHOOLCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     YEAR, ";
        $query .= "     INCOME_L_CD, ";
        $query .= "     INCOME_M_CD, ";
        $query .= "     'HASUU' AS SCHREGNO, ";
        $query .= "     '2' as WARIHURI_DIV, ";
        $query .= "     0 as TOTAL_INCOME_MONEY, ";
        $query .= "     0 as TOTAL_OUTGO_MONEY, ";
        $query .= "     TOTAL_HASUU as RESULT_MONEY, ";
        $query .= "     '1' as KURIKOSI_FLG, ";
        $query .= "     '1' as KURIKOSI_ALL, ";
        $query .= "     0 as KURIKOSI_MONEY ";
        $query .= " FROM ";
        $query .= "     HASUU_DATA ";
        $query .= " ORDER BY ";
        $query .= "     SCHREGNO ";

        return $query;
    }

    //更新（返金実行）
    function updateLevyOutgo($db, $model, $row) {
        //項目セット
        $schoolCd    = $row["SCHOOLCD"];
        $schoolKind  = $row["SCHOOL_KIND"];
        $incomeLcd   = $row["INCOME_L_CD"];
        $incomeMcd   = $row["INCOME_M_CD"];
        $resultMoney = $row["RESULT_MONEY"];
        $schregNo    = $row["SCHREGNO"];
        $wariaiDiv   = $row["WARIHURI_DIV"]; // 1:生徒 2:生徒以外
        $kuriFlg     = $row["KURIKOSI_FLG"];
        $kuriAll     = $row["KURIKOSI_ALL"];
        $kuriMoney   = $row["KURIKOSI_MONEY"];

        $retDiv = array(); // 作成伝票種別([key = "HEN", "KURI"]  作成伝票に'1'をセットする)

        if ($resultMoney != 0) { //収入 - 支出 が 0 じゃない時
            //繰越伝票作成【繰越時の項目がセットされているもののみ（繰越金額以上の差分は返金伝票を作成）】
            if ($kuriFlg != '') {
                //生徒
                if ($wariaiDiv == '1') {
                    // 繰越設定金額 <（収入 - 支出）は繰越設定金額分を繰越
                    $setMoney = ($kuriMoney < $resultMoney) ? $kuriMoney: $resultMoney;

                    //繰越設定金額より大きい時の、差額は返金伝票を作成
                    $diffMoney = $resultMoney - $kuriMoney;

                    //繰越全額フラグがある時は、全額繰越、返金は作成しない
                    if ($kuriAll == '1') {
                        $setMoney  = $resultMoney;
                        $kuriMoney = $resultMoney;
                    }

                    //繰越伝票作成
                    $retDiv["KURI"] = knjp909Query::getUpdateKurikosi($db, $model, $schoolCd, $schoolKind, $incomeLcd, $incomeMcd, $setMoney, $kuriMoney, $schregNo);

                    //返金伝票作成（繰越全額フラグがない時）
                    if ($diffMoney > 0 && $kuriAll != '1') {
                        $retDiv["HEN"] = knjp909Query::getUpdateHenkin($db, $model, $schoolCd, $schoolKind, $incomeLcd, $incomeMcd, $diffMoney, $schregNo, "");
                    }
                } else {
                    if ($kuriAll == '1') {
                        $kuriMoney = $resultMoney;
                    }

                    //繰越伝票作成
                    $retDiv["KURI"] = knjp909Query::getUpdateKurikosi($db, $model, $schoolCd, $schoolKind, $incomeLcd, $incomeMcd, $resultMoney, $kuriMoney, $schregNo);
                }
            } else {
                //生徒
                if ($wariaiDiv == '1') {
                    //返金伝票作成
                    $retDiv["HEN"]  = knjp909Query::getUpdateHenkin($db, $model, $schoolCd, $schoolKind, $incomeLcd, $incomeMcd, $resultMoney, $schregNo, "");
                } else {
                    if ($kuriAll == '1') {
                        $kuriMoney = $resultMoney;
                    }

                    //繰越伝票作成(生徒以外)
                    $retDiv["KURI"] = knjp909Query::getUpdateKurikosi($db, $model, $schoolCd, $schoolKind, $incomeLcd, $incomeMcd, $resultMoney, $kuriMoney, $schregNo);
                }
            }
        }

        return $retDiv;
    }

    //max伝票番号を取得
    function getMaxRequestNo($model, $schoolKind, $henkin='') {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     MAX(REQUEST_NO) AS NO ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '".$schoolKind."'";
        $query .= "     AND YEAR        = '{$model->year}' ";

        return $query;
    }

    //繰越伝票作成
    function getUpdateKurikosi($db, $model, $schoolCd, $schoolKind, $lcd, $mcd, $setMoney, $kuriMoney, $schregNo) {
        $retNo = '1';

        //LEVY_CARRY_OVER_DAT(繰越テーブル)
        $setAttachedYear = $db->getOne(knjp909Query::getAttachedYear($schregNo));
        $attachedYear    = ($setAttachedYear == '') ? $model->year: $setAttachedYear;

        $carryCnt = $db->getOne(knjp909Query::getLevyCarryOverDat($schoolCd, $schoolKind, $model->year, $schregNo, $lcd, $mcd, "CNT"));
        $carryOverMoney = $db->getOne(knjp909Query::getLevyCarryOverDat($schoolCd, $schoolKind, $model->year, $schregNo, $lcd, $mcd));

        //端数、生徒以外は名称変更
        $endStr = "";
        if ($schregNo == "HASUU") {
            $endStr = "（端数）";
        } else if ($schregNo == "NOT_SCH") {
            $endStr = "（生徒以外）";
        }

        $data = array();
        if ($model->grade != '99') {
            $data["LEVY_L_NAME"][TEXT]          = $model->levyLMdat[$lcd.$mcd]["LEVY_L_NAME"].$endStr;
            $data["LEVY_L_ABBV"][TEXT]          = $model->levyLMdat[$lcd.$mcd]["LEVY_L_ABBV"].$endStr;
            $data["LEVY_M_NAME"][TEXT]          = $model->levyLMdat[$lcd.$mcd]["LEVY_M_NAME"].$endStr;
            $data["LEVY_M_ABBV"][TEXT]          = $model->levyLMdat[$lcd.$mcd]["LEVY_M_ABBV"].$endStr;
        } else {
            $data["LEVY_L_NAME"][TEXT]          = $model->levyLMdat[$schoolKind.'-'.$lcd.$mcd]["LEVY_L_NAME"].$endStr;
            $data["LEVY_L_ABBV"][TEXT]          = $model->levyLMdat[$schoolKind.'-'.$lcd.$mcd]["LEVY_L_ABBV"].$endStr;
            $data["LEVY_M_NAME"][TEXT]          = $model->levyLMdat[$schoolKind.'-'.$lcd.$mcd]["LEVY_M_NAME"].$endStr;
            $data["LEVY_M_ABBV"][TEXT]          = $model->levyLMdat[$schoolKind.'-'.$lcd.$mcd]["LEVY_M_ABBV"].$endStr;
        }
        $data["CARRY_OVER_MONEY"][NUMBER]   = $setMoney + $carryOverMoney;
        $data["DIFFERENCE_MONEY"][NUMBER]   = $setMoney - $kuriMoney;

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        if ($carryCnt == 0) {
            $data["SCHOOLCD"][TEXT]             = $schoolCd;
            $data["SCHOOL_KIND"][TEXT]          = $schoolKind;
            $data["YEAR"][TEXT]                 = $model->year;
            $data["SCHREGNO"][TEXT]             = $schregNo;
            $data["INCOME_L_CD"][DATE]          = $lcd;
            $data["INCOME_M_CD"][DATE]          = $mcd;
            $data["ATTACHED_YEAR"][TEXT]        = $attachedYear;

            $query = Query::insertSQL($data, "LEVY_CARRY_OVER_DAT");
        } else {
            $where  = " WHERE ";
            $where .= "         SCHOOLCD        = '{$schoolCd}' ";
            $where .= "     AND SCHOOL_KIND     = '{$schoolKind}' ";
            $where .= "     AND YEAR            = '{$model->year}' ";
            $where .= "     AND SCHREGNO        = '{$schregNo}' ";
            $where .= "     AND INCOME_L_CD     = '{$lcd}' ";
            $where .= "     AND INCOME_M_CD     = '{$mcd}' ";

            $query = Query::updateSQL($data, "LEVY_CARRY_OVER_DAT", $where);
            $retNo = '0';
        }
        $db->query($query);

        return $retNo;
    }

    //繰越テーブル情報取得(LEVY_CARRY_OVER_DAT)
    function getLevyCarryOverDat($schoolCd, $schoolKind, $year, $schregNo, $incomeLcd, $incomeMcd, $cnt="") {
        $query  = " SELECT ";
        if ($cnt != '') {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     CARRY_OVER_MONEY ";
        }
        $query .= " FROM ";
        $query .= "     LEVY_CARRY_OVER_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD        = '{$schoolCd}' ";
        $query .= "     AND SCHOOL_KIND     = '{$schoolKind}' ";
        $query .= "     AND YEAR            = '{$year}' ";
        $query .= "     AND SCHREGNO        = '{$schregNo}' ";
        $query .= "     AND INCOME_L_CD     = '{$incomeLcd}' ";
        $query .= "     AND INCOME_M_CD     = '{$incomeMcd}' ";

        return $query;
    }

    //入学年度取得(SCHREG_BASE_MST)
    function getAttachedYear($schregNo) {
        $query  = " SELECT ";
        $query .= "     FISCALYEAR(ENT_DATE) AS ENT_YEAR ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregNo}' ";

        return $query;
    }

    //返金伝票作成
    function getUpdateHenkin($db, $model, $schoolCd, $schoolKind, $incomeLcd, $incomeMcd, $setMoney, $schregNo, $work="") {
        $getMaxRequestNo = $db->getOne(knjp909Query::getMaxRequestNo($model, $schoolKind));
        //対象年度の伝票番号がない場合
        if (!$getMaxRequestNo) {
            $setRequestNo = $model->year.'B00001';
        } else {
            $setNo = sprintf("%05d", intval(substr($getMaxRequestNo, 5, 5)) + 1);
            $setRequestNo = $model->year.'B'.$setNo;
        }

        //LEVY_REQUEST_OUTGO_DAT
        $data = array();
        $data["SCHOOLCD"][TEXT]         = $schoolCd;
        $data["SCHOOL_KIND"][TEXT]      = $schoolKind;
        $data["YEAR"][TEXT]             = $model->year;
        $data["OUTGO_L_CD"][TEXT]       = '99';
        $data["OUTGO_M_CD"][TEXT]       = '99';
        $data["REQUEST_NO"][TEXT]       = $setRequestNo;
        $data["REQUEST_DATE"][DATE]     = str_replace('/', '-', CTRL_DATE);
        $data["REQUEST_STAFF"][TEXT]    = STAFFCD;
        $data["INCOME_L_CD"][TEXT]      = $incomeLcd;
        $data["INCOME_M_CD"][TEXT]      = $incomeMcd;
        $data["PAY_DIV"][TEXT]          = '1'; // 固定
        $data["REQUEST_GK"][NUMBER]     = $setMoney;
        $data["OUTGO_APPROVAL"][TEXT]   = '1';
        $data["HENKIN_FLG"][TEXT]       = '1';

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($data, "LEVY_REQUEST_OUTGO_DAT");
        $db->query($query);

        //LEVY_REQUEST_OUTGO_MEISAI_DAT
        $data = array();
        $data["SCHOOLCD"][TEXT]                 = $schoolCd;
        $data["SCHOOL_KIND"][TEXT]              = $schoolKind;
        $data["YEAR"][TEXT]                     = $model->year;
        $data["REQUEST_NO"][TEXT]               = $setRequestNo;
        $data["LINE_NO"][NUMBER]                = 1;
        $data["OUTGO_L_CD"][TEXT]               = '99';
        $data["OUTGO_M_CD"][TEXT]               = '99';
        $data["OUTGO_S_CD"][TEXT]               = '999';
        $data["COMMODITY_PRICE"][NUMBER]        = $setMoney;
        $data["COMMODITY_CNT"][NUMBER]          = 1;
        $data["TOTAL_PRICE_ZEINUKI"][NUMBER]    = $setMoney;
        $data["TOTAL_TAX"][NUMBER]              = 0;
        $data["TOTAL_PRICE"][NUMBER]            = $setMoney;
        $data["SCH_PRICE"][NUMBER]              = $setMoney;
        $data["SCH_CNT"][NUMBER]                = 1;
        $data["HASUU"][NUMBER]                  = 0;
        $data["WARIHURI_DIV"][TEXT]             = '1';

        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "SYSDATE()";

        $query = Query::insertSQL($data, "LEVY_REQUEST_OUTGO_MEISAI_DAT");
        $db->query($query);

        //LEVY_REQUEST_OUTGO_SCHREG_DAT
        $data = array();
        $data["SCHOOLCD"][TEXT]                 = $schoolCd;
        $data["SCHOOL_KIND"][TEXT]              = $schoolKind;
        $data["YEAR"][TEXT]                     = $model->year;
        $data["REQUEST_NO"][TEXT]               = $setRequestNo;
        $data["SCHREGNO"][TEXT]                 = $schregNo;
        $data["LINE_NO"][NUMBER]                = 1;
        $data["OUTGO_L_CD"][TEXT]               = '99';
        $data["OUTGO_M_CD"][TEXT]               = '99';
        $data["OUTGO_S_CD"][TEXT]               = '999';
        $data["OUTGO_DATE"][DATE]               = str_replace('/', '-', CTRL_DATE);
        $data["OUTGO_MONEY"][NUMBER]            = $setMoney;

        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "SYSDATE()";

        $query = Query::insertSQL($data, "LEVY_REQUEST_OUTGO_SCHREG_DAT");
        $db->query($query);

        if ($model->hasuuCd != '' && $work == "WORK") {
            list($tmpCd, $hasuu) = explode(':', $model->hasuuCd);
            list($hasuuSchoolKind, $reqNo, $lineNo, $lcd, $mcd, $scd) = explode('-', $tmpCd);
            //作業テーブル更新
            $data = array();
            $data["SCHOOLCD"][TEXT]                 = $schoolCd;
            $data["SCHOOL_KIND"][TEXT]              = $hasuuSchoolKind;
            $data["YEAR"][TEXT]                     = $model->year;
            $data["REQUEST_NO"][TEXT]               = $reqNo;
            $data["LINE_NO"][NUMBER]                = $lineNo;
            $data["OUTGO_L_CD"][TEXT]               = $lcd;
            $data["OUTGO_M_CD"][TEXT]               = $mcd;
            $data["OUTGO_S_CD"][TEXT]               = $scd;
            $data["INS_REQUEST_NO"][TEXT]           = $setRequestNo;

            $data["REGISTERCD"][TEXT]               = STAFFCD;
            $data["UPDATED"][FUNC]                  = "SYSDATE()";

            $query = Query::insertSQL($data, "LEVY_REQUEST_HASUU_WORK_DAT");
            $db->query($query);
        }

        return '1';
    }

    // 端数項目（伝票）処理
    function InsertHasuuHenkinCarry($db, $model) {
        $retArray = array();

        $schoolCd = sprintf("%012d", SCHOOLCD);

        list($tmpCd, $hasuu) = explode(':', $model->hasuuCd);
        list($schoolKind, $reqNo, $lineNo, $lcd, $mcd, $scd) = explode('-', $tmpCd);

        //収入項目(INCOME_L_CD, INCOME_M_CD)取得
        $query = knjp909Query::getIncomeLMcd($model, $schoolKind, $reqNo, $lcd, $mcd);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $incomeLcd = $row["INCOME_L_CD"];
            $incomeMcd = $row["INCOME_M_CD"];
        }

        //返金伝票キャンセル処理
        knjp909Query::getHasuuHenkinCancel($db, $model, $schoolKind, $reqNo, $lineNo, $lcd, $mcd, $scd);

        //作業テーブル削除
        $query  = " DELETE FROM LEVY_REQUEST_HASUU_WORK_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$schoolKind}' ";
        $query .= "     AND YEAR        = '{$model->year}' ";
        $query .= "     AND REQUEST_NO  = '{$reqNo}' ";
        $query .= "     AND LINE_NO     =  {$lineNo} ";
        $query .= "     AND OUTGO_L_CD  = '{$lcd}' ";
        $query .= "     AND OUTGO_M_CD  = '{$mcd}' ";
        $query .= "     AND OUTGO_S_CD  = '{$scd}' ";
        $query .= "     AND INS_REQUEST_NO not in ( SELECT ";
        $query .= "                                     REQUEST_NO ";
        $query .= "                                 FROM ";
        $query .= "                                     LEVY_REQUEST_OUTGO_DAT ";
        $query .= "                                 WHERE ";
        $query .= "                                         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "                                     AND SCHOOL_KIND = '{$schoolKind}' ";
        $query .= "                                     AND YEAR        = '{$model->year}' ";
        $query .= "                                     AND OUTGO_L_CD  = '99' ";
        $query .= "                                     AND OUTGO_M_CD  = '99' ";
        $query .= "                                     AND value(HENKIN_APPROVAL, '0') = '1' ";
        $query .= "                                 ) ";
        $db->query($query);

        //対象収入項目に紐づく生徒のカウント
        $query = knjp909Query::getHasuuOutgoSchreg('CNT', $model, $schoolKind, $incomeLcd, $incomeMcd);
        $schCount = $db->getOne($query);

        //返金金額セット
        $setHenkinMoney = floor($hasuu / $schCount);
        $setAmariMoney  = $hasuu % $schCount;

        // 返金伝票作成
        if ($setHenkinMoney > 0) {
            $query = knjp909Query::getHasuuOutgoSchreg('SCH', $model, $schoolKind, $incomeLcd, $incomeMcd);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //返金伝票
                $retArray["HEN"] += knjp909Query::getUpdateHenkin($db, $model, $schoolCd, $schoolKind, $incomeLcd, $incomeMcd, $setHenkinMoney, $row["SCHREGNO"], "WORK");
            }
        }

        // 余りがある時は、今年度の収入伺を作成（最終端数振替先で指定した項目で）
        if ($setAmariMoney > 0) {
            $retArray["INCOME"] = knjp909Query::getUpdateIncomeDat($db, $model, $schoolCd, $schoolKind, $lcd, $mcd, $scd, $setAmariMoney);
        }
        return $retArray;
    }

    // 返金伝票キャンセル処理
    function getHasuuHenkinCancel($db, $model, $schoolKind, $reqNo, $lineNo, $lcd, $mcd, $scd) {
        //返金キャンセル
        $data = array();
        $data["OUTGO_CANCEL"][TEXT] = '1';
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "         SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "     AND SCHOOL_KIND     = '{$schoolKind}' ";
        $where .= "     AND YEAR            = '{$model->year}' ";
        $where .= "     AND OUTGO_L_CD      = '99' ";
        $where .= "     AND INCOME_L_CD    <> '98' ";
        $where .= "     AND value(OUTGO_CANCEL, '0')    = '0' ";
        $where .= "     AND value(HENKIN_APPROVAL, '0') = '0' ";
        $where .= "     AND REQUEST_NO in ( SELECT ";
        $where .= "                             INS_REQUEST_NO ";
        $where .= "                         FROM ";
        $where .= "                             LEVY_REQUEST_HASUU_WORK_DAT ";
        $where .= "                         WHERE ";
        $where .= "                             SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "                         AND SCHOOL_KIND = '{$schoolKind}' ";
        $where .= "                         AND YEAR        = '{$model->year}' ";
        $where .= "                         AND REQUEST_NO  = '{$reqNo}' ";
        $where .= "                         AND LINE_NO     =  {$lineNo} ";
        $where .= "                         AND OUTGO_L_CD  = '{$lcd}' ";
        $where .= "                         AND OUTGO_M_CD  = '{$mcd}' ";
        $where .= "                         AND OUTGO_S_CD  = '{$scd}' ";
        $where .= "                        ) ";

        $query = Query::updateSQL($data, "LEVY_REQUEST_OUTGO_DAT", $where);
        $db->query($query);
    }

    // 端数項目更新対象者取得
    function getIncomeLMcd($model, $schoolKind, $reqNo, $lcd, $mcd) {
        $query  = " SELECT ";
        $query .= "     INCOME_L_CD, ";
        $query .= "     INCOME_M_CD ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$schoolKind}' ";
        $query .= "     AND YEAR        = '{$model->year}' ";
        $query .= "     AND OUTGO_L_CD  = '{$lcd}' ";
        $query .= "     AND OUTGO_M_CD  = '{$mcd}' ";
        $query .= "     AND REQUEST_NO  = '{$reqNo}' ";

        return $query;
    }

    // 端数項目更新対象者取得
    function getHasuuOutgoSchreg($cnt, $model, $schoolKind, $incomeLcd, $incomeMcd) {
        $query  = " SELECT ";
        if ($cnt == 'CNT') {
            $query .= "     COUNT(distinct SCHREGNO) ";
        } else {
            $query .= "     SCHREGNO ";
        }
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_SCHREG_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$schoolKind}' ";
        $query .= "     AND YEAR        = '{$model->year}' ";
        $query .= "     AND INCOME_L_CD = '{$incomeLcd}' ";
        $query .= "     AND INCOME_M_CD = '{$incomeMcd}' ";
        $query .= "     AND SCHREGNO not in ( SELECT "; // 返金済みがある人は除く
        $query .= "                               SCH.SCHREGNO ";
        $query .= "                           FROM ";
        $query .= "                               LEVY_REQUEST_OUTGO_SCHREG_DAT SCH ";
        $query .= "                               LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTG ON OUTG.SCHOOLCD    = SCH.SCHOOLCD ";
        $query .= "                                                                    AND OUTG.SCHOOL_KIND = SCH.SCHOOL_KIND ";
        $query .= "                                                                    AND OUTG.YEAR        = SCH.YEAR ";
        $query .= "                                                                    AND OUTG.OUTGO_L_CD  = SCH.OUTGO_L_CD ";
        $query .= "                                                                    AND OUTG.OUTGO_M_CD  = SCH.OUTGO_M_CD ";
        $query .= "                                                                    AND OUTG.REQUEST_NO  = SCH.REQUEST_NO ";
        $query .= "                           WHERE ";
        $query .= "                                   SCH.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "                               AND SCH.SCHOOL_KIND = '{$schoolKind}' ";
        $query .= "                               AND SCH.YEAR        = '{$model->year}' ";
        $query .= "                               AND SCH.OUTGO_L_CD  = '99' ";
        $query .= "                               AND OUTG.OUTGO_CANCEL is null ";
        $query .= "                               AND value(OUTG.HENKIN_APPROVAL, '0') = '1' ";
        $query .= "                         ) ";
        $query .= "     AND SCHREGNO NOT IN ( ";
        $query .= "         SELECT ";
        $query .= "             SEISAN_DONE.SCHREGNO ";
        $query .= "         FROM ";
        $query .= "             LEVY_REQUEST_SEISAN_DONE_SCHREG_DAT SEISAN_DONE ";
        $query .= "     ) ";
        if ($cnt == 'CNT') {
        } else {
            $query .= " GROUP BY ";
            $query .= "     SCHREGNO ";
            $query .= " ORDER BY ";
            $query .= "     SCHREGNO ";
        }

        return $query;
    }

    // 収入伝票作成
    function getUpdateIncomeDat($db, $model, $schoolCd, $schoolKind, $lcd, $mcd, $scd, $setMoney) {
        list($schoolKind, $incomeLcd, $incomeMcd) = explode('-', $model->hasuuFrikaeCd);

        // 最大伝票番号(収入)取得
        $getMaxRequestNo = $db->getOne(knjp909Query::getMaxRequestNoIncome($this, "LEVY_REQUEST_INCOME_DAT", $schoolCd, $schoolKind));
        $setNoIncome     = sprintf("%05d", intval(substr($getMaxRequestNo, 5, 5)) + 1);
        $setIncomeRequestNo = $this->year.'A'.$setNoIncome;

        $setScd = '001';

        // LEVY_REQUEST_INCOME_MEISAI_DAT
        $data2 = array();
        $data2["SCHOOLCD"][TEXT]            = $schoolCd;
        $data2["SCHOOL_KIND"][TEXT]         = $schoolKind;
        $data2["YEAR"][TEXT]                = $model->year;
        $data2["INCOME_L_CD"][TEXT]         = $incomeLcd;
        $data2["INCOME_M_CD"][TEXT]         = $incomeMcd;
        $data2["REQUEST_NO"][TEXT]          = $setIncomeRequestNo;
        $data2["INCOME_S_CD"][TEXT]         = $setScd;
        $data2["LINE_NO"][NUMBER]           = 1;
        $data2["COMMODITY_NAME"][TEXT]      = $setCommodityName;
        $data2["COMMODITY_PRICE"][NUMBER]   = $setMoney;
        $data2["COMMODITY_CNT"][NUMBER]     = 1;
        $data2["TOTAL_PRICE"][NUMBER]       = $setMoney;
        $data2["WARIHURI_DIV"][TEXT]        = '2'; // 1;生徒 2:生徒以外
        $data2["REGISTERCD"][TEXT]          = STAFFCD;
        $data2["UPDATED"][NUMBER]           = "sysdate()";

        $query = Query::insertSQL($data2, "LEVY_REQUEST_INCOME_MEISAI_DAT");
        $db->query($query);

        // LEVY_REQUEST_INCOME_DAT
        $data3 = array();
        $data3["SCHOOLCD"][TEXT]                = $schoolCd;
        $data3["SCHOOL_KIND"][TEXT]             = $schoolKind;
        $data3["YEAR"][TEXT]                    = $model->year;
        $data3["INCOME_L_CD"][TEXT]             = $incomeLcd;
        $data3["INCOME_M_CD"][TEXT]             = $incomeMcd;
        $data3["REQUEST_NO"][TEXT]              = $setIncomeRequestNo;
        $data3["REQUEST_DATE"][DATE]            = CTRL_DATE;
        $data3["REQUEST_STAFF"][TEXT]           = STAFFCD;
        $data3["REQUEST_GK"][NUMBER]            = $setMoney;
        $data3["COLLECT_DIV"][TEXT]             = '4'; // 1:入金項目 2:繰越項目 3:振替処理項目 4:最終端数振替
        $data3["COLLECT_WARIHURI_DIV"][TEXT]    = '3'; // 1:生徒 2:生徒以外 3:端数
        $data3["COLLECT_L_CD"][TEXT]            = $lcd;
        $data3["COLLECT_M_CD"][TEXT]            = $mcd;
        $data3["COLLECT_S_CD"][TEXT]            = $scd;
        $data3["INCOME_APPROVAL"][TEXT]         = '1'; // 決済済みにする
        $data3["INCOME_DATE"][TEXT]             = CTRL_DATE;
        $data3["INCOME_STAFF"][TEXT]            = STAFFCD;
        $data3["REGISTERCD"][TEXT]              = STAFFCD;
        $data3["UPDATED"][NUMBER]               = "sysdate()";

        $query = Query::insertSQL($data3, "LEVY_REQUEST_INCOME_DAT");
        $db->query($query);

        return '1';
    }

    // MAX伝票番号取得
    function getMaxRequestNoIncome($model, $table, $schoolCd, $schoolKind) {
        $query  = " SELECT ";
        $query .= "     MAX(REQUEST_NO) AS NO ";
        $query .= " FROM ";
        $query .= "     {$table} ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '{$schoolCd}' ";
        $query .= "     AND SCHOOL_KIND = '{$schoolKind}' ";
        $query .= "     AND YEAR        = '{$model->year}' ";

        return $query;
    }

    /********************/
    /* 締め履歴更新処理 */
    /********************/
    // 更新 --LEVY_CLOSE_GRADE_DAT--
    function updateLevyColseGradeDat($db, $model) {
        $query  = " DELETE FROM ";
        $query .= "     LEVY_CLOSE_GRADE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= " AND YEAR        = '{$model->year}' ";
        if ($model->grade != '99') {
            $query .= " AND SCHOOL_KIND = '{$model->schoolKind}' ";
            $query .= " AND GRADE       = '{$model->grade}' ";
        }
        $db->query($query);

        if ($model->grade != '99') {
            $data = array();
            $data["SCHOOLCD"][TEXT]     = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]  = $model->schoolKind;
            $data["YEAR"][TEXT]         = $model->year;
            $data["GRADE"][TEXT]        = $model->grade;
            $data["CLOSE_FLG"][TEXT]    = '1';

            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";

            $query = Query::insertSQL($data, "LEVY_CLOSE_GRADE_DAT");
            $db->query($query);
        } else {
            $query = knjp909Query::getGdat($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $data = array();
                $data["SCHOOLCD"][TEXT]     = sprintf("%012d", SCHOOLCD);
                $data["SCHOOL_KIND"][TEXT]  = $row["SCHOOL_KIND"];
                $data["YEAR"][TEXT]         = $model->year;
                $data["GRADE"][TEXT]        = $row["VALUE"];
                $data["CLOSE_FLG"][TEXT]    = '1';

                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "SYSDATE()";

                $query = Query::insertSQL($data, "LEVY_CLOSE_GRADE_DAT");
                $db->query($query);
            }
        }

        return;
    }

}
?>