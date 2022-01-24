<?php

require_once('for_php7.php');

class knjp081kQuery extends Query {

    //学年クラス一覧取得
    function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS ";
        return $query;
    }

    //生徒一覧取得
    function selectSchregQuery($model, $selectLeft)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     L2.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.NAME, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT L2 ON T1.YEAR = L2.YEAR ";
        $query .= "          AND T1.SEMESTER = L2.SEMESTER ";
        $query .= "          AND T1.GRADE = L2.GRADE ";
        $query .= "          AND T1.HR_CLASS = L2.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        if (get_count($selectLeft) > 0) {
            $query .= "     AND T1.GRADE || '-' || T1.HR_CLASS IN ('" .implode($selectLeft,"','") ."') ";
        } else {
            $query .= "     AND T1.GRADE || '-' || T1.HR_CLASS IN ('') ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";
        return $query;
    }

    //CSVファイル取得(一年生用）
    function selectExecQuery1(&$model, $schoolcd)
    {
        $query = " SELECT ";
        $query .= "     T1.SCHREGNO,";
        $query .= "     T5.BANK_MAJORCD,";
        $query .= "     T1.GRADE,";
        $query .= "     T5.BANK_HR_CLASS,";
        $query .= "     T2.NAME_KANA,";
        $query .= "     T2.NAME,";
        $query .= "     T2.SEX,";
        $query .= "     T2.BIRTHDAY,";
        $query .= "     T3.ZIPCD,";
        $query .= "     T3.ADDR1,";
        $query .= "     T3.ADDR2,";
        $query .= "     T3.TELNO,";
        $query .= "     T4.GUARANTOR_KANA  AS GUARD_KANA,";
        $query .= "     T4.GUARANTOR_NAME  AS GUARD_NAME,";
        $query .= "     T4.GUARANTOR_ZIPCD AS GUARD_ZIPCD,";
        $query .= "     T4.GUARANTOR_ADDR1 AS GUARD_ADDR1,";
        $query .= "     T4.GUARANTOR_ADDR2 AS GUARD_ADDR2,";
        $query .= "     T4.GUARANTOR_TELNO AS GUARD_TELNO,";
        $query .= "     T7.REMAINGRADE_FLG,";
        $query .= "     T7.OLD_SCHREGNO,";
        $query .= "     CASE WHEN T1.GRADE = '01' AND VALUE(T7.REMAINGRADE_FLG, '0') != '1' ";
        $query .= "          THEN COALESCE(T8.BANK_MAJORCD, '  ') ";
        $query .= "          ELSE COALESCE(T6.BANK_MAJORCD2, '  ') ";
        $query .= "     END AS BANK_MAJORCD2,";
        $query .= "     CASE WHEN T1.GRADE = '01' AND VALUE(T7.REMAINGRADE_FLG, '0') != '1' ";
        $query .= "          THEN '03' ";
        $query .= "          ELSE COALESCE(T6.GRADE2, '  ') ";
        $query .= "     END AS GRADE2,";
        $query .= "     CASE WHEN T1.GRADE = '01' AND VALUE(T7.REMAINGRADE_FLG, '0') != '1' ";
        $query .= "          THEN COALESCE(T8.BANK_HR_CLASS, '  ') ";
        $query .= "          ELSE COALESCE(T6.BANK_HR_CLASS2, '  ') ";
        $query .= "     END AS BANK_HR_CLASS2,";
        $query .= "     CASE WHEN T1.GRADE = '01' AND VALUE(T7.REMAINGRADE_FLG, '0') != '1' ";
        $query .= "          THEN COALESCE(T7.OLD_SCHREGNO, '        ') ";
        $query .= "          ELSE T1.SCHREGNO ";
        $query .= "     END AS SCHREGNO_OLD, ";
        $query .= "     CASE WHEN T1.GRADE = '01' AND VALUE(T7.REMAINGRADE_FLG, '0') != '1' ";
        $query .= "          THEN '08407' ";
        $query .= "          ELSE '".$schoolcd."' ";
        $query .= "     END AS SCHOOLCD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT OUTER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT OUTER JOIN SCHREG_ADDRESS_DAT T3 ON T1.SCHREGNO = T3.SCHREGNO ";
        $query .= "          AND '".CTRL_DATE."' BETWEEN T3.ISSUEDATE ";
        $query .= "          AND COALESCE(EXPIREDATE,'9999-12-31') ";
        $query .= "     LEFT OUTER JOIN GUARDIAN_DAT T4 ON T1.SCHREGNO = T4.SCHREGNO ";
        $query .= "     LEFT OUTER JOIN BANK_CLASS_MST T5 ON T1.YEAR = T5.YEAR ";
        $query .= "          AND T1.GRADE = T5.GRADE ";
        $query .= "          AND T1.HR_CLASS = T5.HR_CLASS ";
        $query .= "     LEFT OUTER JOIN (SELECT ";
        $query .= "         ST1.SCHREGNO, ";
        $query .= "         ST1.YEAR, ";
        $query .= "         MAX(ST1.SEMESTER), ";
        $query .= "         ST2.BANK_MAJORCD AS BANK_MAJORCD2, ";
        $query .= "         ST2.GRADE AS GRADE2, ";
        $query .= "         ST2.BANK_HR_CLASS AS BANK_HR_CLASS2 ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ST1 ";
        $query .= "         LEFT OUTER JOIN BANK_CLASS_MST ST2 ON ST1.YEAR = ST2.YEAR ";
        $query .= "              AND ST1.GRADE = ST2.GRADE ";
        $query .= "              AND ST1.HR_CLASS = ST2.HR_CLASS ";
        $query .= "     GROUP BY ";
        $query .= "         ST1.SCHREGNO, ";
        $query .= "         ST1.YEAR, ";
        $query .= "         ST2.BANK_MAJORCD, ";
        $query .= "         ST2.GRADE, ";
        $query .= "         ST2.BANK_HR_CLASS ";
        $query .= "     ) T6 ON T1.SCHREGNO = T6.SCHREGNO AND  ";
        $query .= "             INTEGER(T1.YEAR)-1 = INTEGER(T6.YEAR) ";
        $query .= "     LEFT OUTER JOIN CLASS_FORMATION_DAT T7 ON T1.SCHREGNO = T7.SCHREGNO ";
        $query .= "          AND T1.YEAR = T7.YEAR ";
        $query .= "          AND T7.SEMESTER = '1' ";
        $query .= "     LEFT OUTER JOIN JBANK_CLASS_MST T8 ON INTEGER(T7.YEAR)-1 = INTEGER(T8.YEAR) ";
        $query .= "          AND T7.OLD_GRADE = T8.GRADE ";
        $query .= "          AND T7.OLD_HR_CLASS = T8.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        if (get_count($model->schData) > 0) {
            $query .= "     AND T1.SCHREGNO NOT IN ('" .implode($model->schData,"','") ."') ";
        } else {
            $query .= "     AND T1.SCHREGNO NOT IN ('') ";
        }
        $query .= "     AND T1.GRADE || '-' || T1.HR_CLASS IN ('" .implode($model->hr_class,"','") ."') ";
        if ($model->outformat == 2) {
            $query .= "     AND ((T1.GRADE != '01') ";
            $query .= "          OR ";
            $query .= "          (T1.GRADE = '01' AND T7.OLD_SCHREGNO IS NOT NULL) ";
            $query .= "         ) ";
        }
        //留年生を省く
        if ($model->radio == 2) {
            $query .= "     AND NOT EXISTS(SELECT ";
            $query .= "                 YEAR, ";
            $query .= "                 SEMESTER, ";
            $query .= "                 GRADE, ";
            $query .= "                 SCHREGNO ";
            $query .= "             FROM ";
            $query .= "                 SCHREG_REGD_DAT ST1 ";
            $query .= "             WHERE ";
            $query .= "                 ST1.YEAR < T1.YEAR ";
            $query .= "                 AND ST1.SCHREGNO = T1.SCHREGNO ";
            $query .= "                 AND ST1.GRADE = T1.GRADE ";
            $query .= "             ) ";
        }
        return $query;
    }
    //学校コード取得
    function selectQueryIntData1(&$model)
    {
        $query = " SELECT ";
        $query .= "     VAR1 ";
        $query .= " FROM ";
        $query .= "     SCHOOL_EXPENSES_SYS_INI ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJP000K' AND ";
        $query .= "     DIV = '0001' ";

        return $query;
    }
    //元号取得
    function selectQueryGengou(&$model)
    {
        $query = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'G210' ";

        return $query;
    }

    //中高判定フラグ
    function getschool()
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOLNAME1 like '%近畿%中学%' ";

        return $query;
    }
}
?>
