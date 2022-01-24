<?php

require_once('for_php7.php');

class knjp829query extends Query {

    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER != '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ出力で使用  **/
    /************************/
    //CSVデータ出力
    function selectMainQuery($model) {
        $query .= " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     CASE WHEN REDUC_SCHOOL_P.SCHOOL_KIND = 'J' ";
        $query .= "          THEN CASE WHEN SIGEL_SCH.SCHREGNO_J is null ";
        $query .= "                    THEN REDUC_SCHOOL_P.SCHREGNO ";
        $query .= "                    ELSE SIGEL_SCH.SCHREGNO_J ";
        $query .= "               END ";
        $query .= "          ELSE CASE WHEN SIGEL_SCH.SCHREGNO_H is null ";
        $query .= "                    THEN 'H' || REDUC_SCHOOL_P.SCHREGNO ";
        $query .= "                    ELSE SIGEL_SCH.SCHREGNO_H ";
        $query .= "               END ";
        $query .= "     END AS SCHREGNO, ";
        $query .= "     REDUC_SCHOOL_P.YEAR, ";
        $query .= "     SEME.SEMESTER, ";
        $query .= "     REDUC_SCHOOL_P.REDUCTION_TARGET, ";
        $query .= "     REDUC_SCHOOL_P.PLAN_YEAR, ";
        $query .= "     REDUC_SCHOOL_P.PLAN_MONTH, ";
        $query .= "     COL_SLIP_LIMIT.PAID_LIMIT_DATE, ";
        $query .= "     SUM(REDUC_SCHOOL_P.DECISION_MONEY) AS DECISION_MONEY ";
        $query .= " FROM ";
        $query .= "     REDUCTION_SCHOOL_PLAN_DAT REDUC_SCHOOL_P ";
        $query .= "     INNER JOIN COLLECT_SLIP_PLAN_LIMITDATE_DAT COL_SLIP_LIMIT ON REDUC_SCHOOL_P.SCHOOLCD = COL_SLIP_LIMIT.SCHOOLCD ";
        $query .= "           AND REDUC_SCHOOL_P.SCHOOL_KIND = COL_SLIP_LIMIT.SCHOOL_KIND ";
        $query .= "           AND REDUC_SCHOOL_P.YEAR = COL_SLIP_LIMIT.YEAR ";
        $query .= "           AND REDUC_SCHOOL_P.SCHREGNO = COL_SLIP_LIMIT.SCHREGNO ";
        $query .= "           AND REDUC_SCHOOL_P.SLIP_NO = COL_SLIP_LIMIT.SLIP_NO ";
        $query .= "           AND REDUC_SCHOOL_P.PLAN_YEAR = COL_SLIP_LIMIT.PLAN_YEAR ";
        $query .= "           AND REDUC_SCHOOL_P.PLAN_MONTH = COL_SLIP_LIMIT.PLAN_MONTH ";
        $query .= "           AND CASE WHEN MONTH(COL_SLIP_LIMIT.PAID_LIMIT_DATE) <= 3 ";
        $query .= "                    THEN MONTH(COL_SLIP_LIMIT.PAID_LIMIT_DATE) + 12 ";
        $query .= "                    ELSE MONTH(COL_SLIP_LIMIT.PAID_LIMIT_DATE) ";
        $query .= "               END ";
        $query .= "               BETWEEN {$model->field["FROM_MONTH"]} AND {$model->field["TO_MONTH"]} ";
        $query .= "     LEFT JOIN SEMESTER_MST SEME ON COL_SLIP_LIMIT.YEAR = SEME.YEAR ";
        $query .= "          AND SEME.SEMESTER < '9' ";
        $query .= "          AND COL_SLIP_LIMIT.PAID_LIMIT_DATE BETWEEN SEME.SDATE AND SEME.EDATE ";
        $query .= "     LEFT JOIN COLLECT_SGL_SCHREGNO_MAPPING_DAT SIGEL_SCH ";
        $query .= "       ON SIGEL_SCH.SCHREGNO = REDUC_SCHOOL_P.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     REDUC_SCHOOL_P.SCHOOLCD = '".SCHOOLCD."' ";
        $query .= "     AND REDUC_SCHOOL_P.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND (VALUE(REDUC_SCHOOL_P.OFFSET_FLG, '0') = '1' OR VALUE(REDUC_SCHOOL_P.REFUND_FLG, '0') = '1') ";
        $query .= "     AND VALUE(REDUC_SCHOOL_P.PLAN_CANCEL_FLG, '0') = '0' ";
        $query .= "     AND VALUE(REDUC_SCHOOL_P.PLAN_LOCK_FLG, '0') = '0' ";
        $query .= "     AND VALUE(REDUC_SCHOOL_P.DECISION_MONEY, 0) > 0 ";
        $query .= " GROUP BY ";
        $query .= "     CASE WHEN REDUC_SCHOOL_P.SCHOOL_KIND = 'J' ";
        $query .= "          THEN CASE WHEN SIGEL_SCH.SCHREGNO_J is null ";
        $query .= "                    THEN REDUC_SCHOOL_P.SCHREGNO ";
        $query .= "                    ELSE SIGEL_SCH.SCHREGNO_J ";
        $query .= "               END ";
        $query .= "          ELSE CASE WHEN SIGEL_SCH.SCHREGNO_H is null ";
        $query .= "                    THEN 'H' || REDUC_SCHOOL_P.SCHREGNO ";
        $query .= "                    ELSE SIGEL_SCH.SCHREGNO_H ";
        $query .= "               END ";
        $query .= "     END, ";
        $query .= "     REDUC_SCHOOL_P.YEAR, ";
        $query .= "     SEME.SEMESTER, ";
        $query .= "     REDUC_SCHOOL_P.REDUCTION_TARGET, ";
        $query .= "     REDUC_SCHOOL_P.PLAN_YEAR, ";
        $query .= "     REDUC_SCHOOL_P.PLAN_MONTH, ";
        $query .= "     COL_SLIP_LIMIT.PAID_LIMIT_DATE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     DENSE_RANK() OVER(PARTITION BY SCHREGNO, REDUCTION_TARGET ORDER BY PLAN_YEAR || PLAN_MONTH) AS BUNNOU_NO, ";
        $query .= "     CASE WHEN REDUCTION_TARGET = '1' ";
        $query .= "          THEN '128' ";
        $query .= "          ELSE '127' ";
        $query .= "     END AS HIMOKU_CD, ";
        $query .= "     DECISION_MONEY AS SYOGAKU_MONEY, ";
        $query .= "     CASE WHEN REDUCTION_TARGET = '1' ";
        $query .= "          THEN '040' ";
        $query .= "          ELSE '046' ";
        $query .= "     END AS SYOGAKU_CD, ";
        $query .= "     '' AS GEN_MONEY, ";
        $query .= "     '' AS GEN_CD, ";
        $query .= "     PAID_LIMIT_DATE AS PAY_DATE, ";
        $query .= "     '6' AS NOUNYU_CD, ";
        $query .= "     '' AS KYOUIKU_CD ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " ORDER BY ";
        $query .= "     SCHREGNO, ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     BUNNOU_NO ";

        return $query;
    }

}
?>
