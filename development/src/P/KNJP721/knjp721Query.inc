<?php

require_once('for_php7.php');

class knjp721Query extends Query
{
    public function getRegdWith($model, $flg = "")
    {
        $query  = " WITH REGD_MAX_SEM AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     MAX(SEMESTER) AS SEMESTER, ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO ";
        $query .= " ), REGD_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     REGD_MAX_SEM T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR ";
        if ($flg) {
            $query .= "     AND T1.GRADE    = '".$model->search["GRADE"]."' ";
        } else {
            $query .= "     AND T1.GRADE || '-' || T1.HR_CLASS = '".$model->search["HR_CLASS"]."' ";
        }
        $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ), SLIP_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     REGD_T.YEAR, ";
        $query .= "     REGD_T.SEMESTER, ";
        $query .= "     REGD_T.SCHREGNO, ";
        $query .= "     REGD_T.GRADE, ";
        $query .= "     REGD_T.HR_CLASS, ";
        $query .= "     REGD_T.ATTENDNO, ";
        $query .= "     SLIP_D.SLIP_NO ";
        $query .= " FROM ";
        $query .= "     REGD_T, ";
        $query .= "     COLLECT_SLIP_DAT SLIP_D ";
        $query .= "     INNER JOIN COLLECT_SLIP_M_DAT SLIP_MD ON SLIP_MD.SCHOOLCD = SLIP_D.SCHOOLCD ";
        $query .= "           AND SLIP_MD.SCHOOL_KIND   = SLIP_D.SCHOOL_KIND ";
        $query .= "           AND SLIP_MD.YEAR          = SLIP_D.YEAR ";
        $query .= "           AND SLIP_MD.SLIP_NO       = SLIP_D.SLIP_NO ";
        $query .= "     INNER JOIN COLLECT_M_MST COL_M ON COL_M.SCHOOLCD = SLIP_MD.SCHOOLCD ";
        $query .= "           AND COL_M.SCHOOL_KIND     = SLIP_MD.SCHOOL_KIND ";
        $query .= "           AND COL_M.YEAR            = SLIP_MD.YEAR ";
        $query .= "           AND COL_M.COLLECT_L_CD    = SLIP_MD.COLLECT_L_CD ";
        $query .= "           AND COL_M.COLLECT_M_CD    = SLIP_MD.COLLECT_M_CD ";
        $query .= "           AND VALUE(COL_M.GAKUNOKIN_DIV, '') != '2' ";
        $query .= "           AND (VALUE(COL_M.REDUCTION_DIV, '') IN ('1','2') OR VALUE(COL_M.IS_REDUCTION_SCHOOL, '') = '1') ";
        $query .= " WHERE ";
        $query .= "     SLIP_D.SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SLIP_D.SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND SLIP_D.CANCEL_DATE IS NULL ";
        $query .= "     AND REGD_T.YEAR = SLIP_D.YEAR ";
        $query .= "     AND REGD_T.SCHREGNO = SLIP_D.SCHREGNO ";
        $query .= " ) ";

        return $query;
    }

    public function selectQuery($model, $flg = "")
    {
        //変数
        $sdate =  CTRL_YEAR      ."-04-01";
        $edate = (CTRL_YEAR + 1) ."-03-31";
        //SQL
        $query  = knjp721Query::getRegdWith($model, $flg);
        $query .= " SELECT ";
        $query .= "     STD_INFO.SCHREGNO, ";
        $query .= "     STD_INFO.NAME_SHOW, ";
        $query .= "     STD_INFO.GRADE, ";
        $query .= "     STD_INFO.HR_CLASS, ";
        $query .= "     STD_INFO.HR_NAMEABBV, ";
        $query .= "     STD_INFO.ATTENDNO, ";
        $query .= "     STD_INFO.SLIP_NO, ";
        $query .= "     PAID_D.PAID_FLG1, ";
        $query .= "     PAID_D.PAID_FLG2, ";
        $query .= "     SCH_PAID_D.SCH_PAID_FLG1, ";
        $query .= "     SCH_PAID_D.SCH_PAID_FLG2, ";

        $query .= "     COUNTRY_D.SEQ_1 AS REDUCTION_SEQ_1, ";
        $query .= "     INCOME_D.INCOME_1 AS REDUC_INCOME_1, ";
        $query .= "     INCOME_D.INCOME_1_1 AS REDUC_INCOME_1_1, ";
        $query .= "     INCOME_D.INCOME_1_2 AS REDUC_INCOME_1_2, ";
        $query .= "     INCOME_D.INCOME_1_3 AS REDUC_INCOME_1_3, ";
        $query .= "     INCOME_D.INCOME_1_4 AS REDUC_INCOME_1_4, ";
        $query .= "     COUNTRY_D.SEQ_2 AS REDUCTION_SEQ_2, ";
        $query .= "     INCOME_D.INCOME_2 AS REDUC_INCOME_2, ";
        $query .= "     INCOME_D.INCOME_2_1 AS REDUC_INCOME_2_1, ";
        $query .= "     INCOME_D.INCOME_2_2 AS REDUC_INCOME_2_2, ";
        $query .= "     INCOME_D.INCOME_2_3 AS REDUC_INCOME_2_3, ";
        $query .= "     INCOME_D.INCOME_2_4 AS REDUC_INCOME_2_4, ";

        $query .= "     COUNTRY_D.RANK_1 AS COUNTRY_RANK_1, ";
        $query .= "     COUNTRY_D.MONEY_1 AS COUNTRY_MONEY_1, ";
        $query .= "     COUNTRY_D.DEC_FLG_1 AS COUNTRY_BASE_FLG_1, ";
        $query .= "     COUNTRY_D.ADD_MONEY_1 AS COUNTRY_ADD_MONEY_1, ";
        $query .= "     COUNTRY_D.ADD_DEC_FLG_1 AS COUNTRY_ADD_FLG_1, ";
        $query .= "     COUNTRY_D.RANK_2 AS COUNTRY_RANK_2, ";
        $query .= "     COUNTRY_D.MONEY_2 AS COUNTRY_MONEY_2, ";
        $query .= "     COUNTRY_D.DEC_FLG_2 AS COUNTRY_BASE_FLG_2, ";
        $query .= "     COUNTRY_D.ADD_MONEY_2 AS COUNTRY_ADD_MONEY_2, ";
        $query .= "     COUNTRY_D.ADD_DEC_FLG_2 AS COUNTRY_ADD_FLG_2, ";
        $query .= "     VALUE(COUNTRY_D.MONEY_1, 0) + VALUE(COUNTRY_D.MONEY_2, 0) AS TOTAL_COUNTRY_MONEY, ";

        $query .= "     GUARDIAN.PREF AS G_PREF, ";
        $query .= "     GUARDIAN.PREFECTURESCD AS G_PREF_CD, ";
        $query .= "     REDUCTION_D.RANK_1 AS PREF_RANK_1, ";
        $query .= "     REDUCTION_D.MONEY_1 AS PREF_MONEY_1, ";
        $query .= "     REDUCTION_D.DEC_FLG_1 AS PREF_FLG_1, ";
        $query .= "     REDUCTION_D.RANK_2 AS PREF_RANK_2, ";
        $query .= "     REDUCTION_D.MONEY_2 AS PREF_MONEY_2, ";
        $query .= "     REDUCTION_D.DEC_FLG_2 AS PREF_FLG_2, ";
        $query .= "     REDUCTION_D.PREFECTURESCD AS REDUC_PREF_CD, ";
        $query .= "     NM1.NAME1 AS REDUC_PREF, ";
        $query .= "     PREF_GRADE.PREFECTURESCD AS PREF_GRADE, ";

        $query .= "     value(TRANSFER.COUNTTRANSFER,0) + value(GRD.COUNTTRANSFER,0) + value(ENT.COUNTTRANSFER,0) AS COUNTTRANSFER, ";
        $query .= "     value(GENMEN.COUNTGENMEN, 0) AS COUNTGENMEN, ";
        $query .= "     COUNTRY_PLAN.ZENKI_CNT, ";
        $query .= "     COUNTRY_PLAN.KOUKI_CNT, ";
        $query .= "     COUNTRY_PLAN.ZENKI_ADD_CNT, ";
        $query .= "     COUNTRY_PLAN.KOUKI_ADD_CNT, ";

        $query .= "     PREF_PLAN.ZENKI_PREF_CNT, ";
        $query .= "     PREF_PLAN.KOUKI_PREF_CNT, ";

        $query .= "     BURDEN_CHARGE.BURDEN_CHARGE1, ";
        $query .= "     BURDEN_CHARGE.BURDEN_CHARGE2, ";
        $query .= "     BURDEN_CHARGE.TOTAL_BURDEN_CHARGE, ";

        $query .= "     COUNTRY_D.REDUCTION_JUGYOURYOU, ";
        $query .= "     REDUCTION_SCHOOL_D.DEC_FLG_1 AS REDUC_SCHOOL_FLG_1, ";
        $query .= "     REDUCTION_SCHOOL_D.MONEY_1 AS REDUCTION_JUGYOURYOU1, ";
        $query .= "     REDUCTION_SCHOOL_D.DEC_FLG_2 AS REDUC_SCHOOL_FLG_2, ";
        $query .= "     REDUCTION_SCHOOL_D.MONEY_2 AS REDUCTION_JUGYOURYOU2, ";
        $query .= "     REDUCTION_SCHOOL_D.SCHREGNO AS REDUCTION_SCHREGNO, ";

        $query .= "     CASE WHEN COUNTRY_D.REMARK IS NOT NULL ";
        $query .= "          THEN COUNTRY_D.REMARK ";
        $query .= "          ELSE REDUCTION_D.REMARK ";
        $query .= "     END AS REMARK ";
        $query .= " FROM ";
        $query .= "    (SELECT ";
        $query .= "         ST2.YEAR, ";
        $query .= "         ST2.SEMESTER, ";
        $query .= "         ST1.SCHREGNO, ";
        $query .= "         ST1.NAME_SHOW, ";
        $query .= "         ST2.GRADE, ";
        $query .= "         ST2.HR_CLASS, ";
        $query .= "         ST2.ATTENDNO, ";
        $query .= "         ST3.HR_NAMEABBV, ";
        $query .= "         ST2.SLIP_NO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_BASE_MST ST1, ";
        $query .= "         SLIP_T ST2 ";
        $query .= "         LEFT JOIN SCHREG_REGD_HDAT ST3 ";
        $query .= "              ON ST2.YEAR        = ST3.YEAR ";
        $query .= "             AND ST2.SEMESTER    = ST3.SEMESTER ";
        $query .= "             AND ST2.GRADE       = ST3.GRADE ";
        $query .= "             AND ST2.HR_CLASS    = ST3.HR_CLASS ";
        $query .= "     WHERE ";
        $query .= "         ST1.SCHREGNO = ST2.SCHREGNO ";
        $query .= "   ) STD_INFO ";
        if ($model->Properties["reductionPrefCd"]) {
            $query .= "     LEFT JOIN (SELECT DISTINCT ";
            $query .= "                     T1.SCHREGNO, ";
            $query .= "                     T2.NAMECD2 AS PREFECTURESCD, ";
            $query .= "                     T2.NAME1 AS PREF ";
            $query .= "                 FROM ";
            $query .= "                     SLIP_T T1, ";
            $query .= "                     NAME_MST T2 ";
            $query .= "                 WHERE ";
            $query .= "                     T2.NAMECD1 = 'G202' AND ";
            $query .= "                     T2.NAMECD2 = '".$model->Properties["reductionPrefCd"]."' ";
            $query .= "                 ) GUARDIAN ON STD_INFO.SCHREGNO = GUARDIAN.SCHREGNO ";
        } else {
            //保護者
            $query .= "     LEFT JOIN (SELECT DISTINCT ";
            $query .= "                     ST1.SCHREGNO, ";
            $query .= "                     SUBSTR(ST2.CITYCD,1,2) AS PREFECTURESCD, ";
            $query .= "                     ST3.NAME1 AS PREF ";
            $query .= "                 FROM ";
            $query .= "                     GUARDIAN_DAT ST1 ";
            $query .= "                     LEFT JOIN ZIPCD_MST ST2  ";
            $query .= "                          ON ST2.NEW_ZIPCD = (CASE WHEN LENGTH(ST1.GUARANTOR_ZIPCD) > 0 THEN ST1.GUARANTOR_ZIPCD ELSE ST1.GUARD_ZIPCD END) ";
            $query .= "                     LEFT JOIN NAME_MST ST3  ";
            $query .= "                          ON ST3.NAMECD1 = 'G202' ";
            $query .= "                         AND ST3.NAMECD2 = SUBSTR(ST2.CITYCD,1,2) ";
            $query .= "                 ) GUARDIAN ON STD_INFO.SCHREGNO = GUARDIAN.SCHREGNO ";
        }
        //所得割
        $query .= "     LEFT JOIN REDUCTION_INCOME_DAT INCOME_D ";
        $query .= "          ON INCOME_D.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "         AND INCOME_D.SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "         AND INCOME_D.YEAR        = '".CTRL_YEAR."' ";
        $query .= "         AND INCOME_D.SCHREGNO    = STD_INFO.SCHREGNO ";
        //支援金
        $query .= "     LEFT JOIN REDUCTION_COUNTRY_DAT COUNTRY_D ";
        $query .= "          ON COUNTRY_D.SCHOOLCD       = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "         AND COUNTRY_D.SCHOOL_KIND    = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "         AND COUNTRY_D.YEAR           = '".CTRL_YEAR."' ";
        $query .= "         AND STD_INFO.SLIP_NO         = COUNTRY_D.SLIP_NO ";
        //補助金
        $query .= "     LEFT JOIN REDUCTION_DAT REDUCTION_D ";
        $query .= "          ON REDUCTION_D.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "         AND REDUCTION_D.SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "         AND STD_INFO.YEAR           = REDUCTION_D.YEAR ";
        $query .= "         AND STD_INFO.SLIP_NO        = REDUCTION_D.SLIP_NO ";
        $query .= "         AND REDUCTION_D.REDUCTION_TARGET = '1' ";
        $query .= "     LEFT JOIN NAME_MST NM1 ";
        $query .= "          ON NM1.NAMECD1     = 'G202' ";
        $query .= "         AND NM1.NAMECD2     = REDUCTION_D.PREFECTURESCD ";
        //減免
        $query .= "     LEFT JOIN REDUCTION_SCHOOL_DAT REDUCTION_SCHOOL_D ";
        $query .= "          ON REDUCTION_SCHOOL_D.SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "         AND REDUCTION_SCHOOL_D.SCHOOL_KIND  = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "         AND REDUCTION_SCHOOL_D.REDUCTION_TARGET = '1' ";
        $query .= "         AND REDUCTION_SCHOOL_D.YEAR         = STD_INFO.YEAR ";
        $query .= "         AND REDUCTION_SCHOOL_D.SLIP_NO      = STD_INFO.SLIP_NO ";
        //減免
        $query .= "     LEFT JOIN (SELECT ";
        $query .= "                     COUNT(*) AS COUNTGENMEN, ";
        $query .= "                     SCHREGNO ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_SCHOLARSHIP_HIST_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "                     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "                     AND ('".CTRL_YEAR."-04-01' BETWEEN FROM_DATE AND TO_DATE ";
        $query .= "                          OR ";
        $query .= "                          '".(CTRL_YEAR + 1)."-03-31' BETWEEN FROM_DATE AND TO_DATE ";
        $query .= "                          OR ";
        $query .= "                          FROM_DATE BETWEEN '".CTRL_YEAR."-04-01' AND '".(CTRL_YEAR + 1)."-03-31' ";
        $query .= "                         ) ";
        $query .= "                 GROUP BY ";
        $query .= "                     SCHREGNO ";
        $query .= "                 ) GENMEN ON STD_INFO.SCHREGNO = GENMEN.SCHREGNO ";
        //異動
        $query .= "     LEFT JOIN (SELECT ";
        $query .= "                     COUNT(*) AS COUNTTRANSFER, ";
        $query .= "                     SCHREGNO ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_TRANSFER_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     TRANSFERCD IN ('1','2') ";
        $query .= "                     AND ((TRANSFER_SDATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}')) ";
        $query .= "                      OR  (TRANSFER_EDATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}'))) ";
        $query .= "                 GROUP BY ";
        $query .= "                     SCHREGNO ";
        $query .= "                 ) TRANSFER ON STD_INFO.SCHREGNO = TRANSFER.SCHREGNO ";
        $query .= "     LEFT JOIN (SELECT ";
        $query .= "                     COUNT(*) AS COUNTTRANSFER, ";
        $query .= "                     SCHREGNO ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_BASE_MST ";
        $query .= "                 WHERE ";
        $query .= "                     GRD_DIV > '1' ";
        $query .= "                     AND GRD_DATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}') ";
        $query .= "                 GROUP BY ";
        $query .= "                     SCHREGNO ";
        $query .= "                 ) GRD ON STD_INFO.SCHREGNO = GRD.SCHREGNO ";
        $query .= "     LEFT JOIN (SELECT ";
        $query .= "                     COUNT(*) AS COUNTTRANSFER, ";
        $query .= "                     SCHREGNO ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_BASE_MST ";
        $query .= "                 WHERE ";
        $query .= "                     ENT_DIV > '3' ";
        $query .= "                     AND ENT_DATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}') ";
        $query .= "                 GROUP BY ";
        $query .= "                     SCHREGNO ";
        $query .= "                 ) ENT ON STD_INFO.SCHREGNO = ENT.SCHREGNO ";
        //支援金計画カウント
        $query .= "     LEFT JOIN (SELECT ";
        $query .= "                     SLIP_NO, ";
        $query .= "                     SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH < '".CTRL_YEAR."07' AND PLAN_MONEY IS NOT NULL ";
        $query .= "                              THEN 1 ";
        $query .= "                              ELSE 0 ";
        $query .= "                         END) AS ZENKI_CNT, ";
        $query .= "                     SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH >= '".CTRL_YEAR."07' AND PLAN_MONEY IS NOT NULL ";
        $query .= "                              THEN 1 ";
        $query .= "                              ELSE 0 ";
        $query .= "                         END) AS KOUKI_CNT, ";
        $query .= "                     SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH < '".CTRL_YEAR."07' AND ADD_PLAN_MONEY IS NOT NULL ";
        $query .= "                              THEN 1 ";
        $query .= "                              ELSE 0 ";
        $query .= "                         END) AS ZENKI_ADD_CNT, ";
        $query .= "                     SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH >= '".CTRL_YEAR."07' AND ADD_PLAN_MONEY IS NOT NULL ";
        $query .= "                              THEN 1 ";
        $query .= "                              ELSE 0 ";
        $query .= "                         END) AS KOUKI_ADD_CNT ";
        $query .= "                 FROM ";
        $query .= "                     REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "                     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "                     AND YEAR        = '".CTRL_YEAR."' ";
        $query .= "                 GROUP BY ";
        $query .= "                     SLIP_NO ";
        $query .= "                 ) COUNTRY_PLAN ON STD_INFO.SLIP_NO = COUNTRY_PLAN.SLIP_NO ";
        //補助金計画カウント
        $query .= "     LEFT JOIN (SELECT ";
        $query .= "                     SLIP_NO, ";
        $query .= "                     SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH < '".CTRL_YEAR."07' AND PLAN_MONEY IS NOT NULL ";
        $query .= "                              THEN 1 ";
        $query .= "                              ELSE 0 ";
        $query .= "                         END ";
        $query .= "                     ) AS ZENKI_PREF_CNT, ";
        $query .= "                     SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH >= '".CTRL_YEAR."07' AND PLAN_MONEY IS NOT NULL ";
        $query .= "                              THEN 1 ";
        $query .= "                              ELSE 0 ";
        $query .= "                         END ";
        $query .= "                     ) AS KOUKI_PREF_CNT ";
        $query .= "                 FROM ";
        $query .= "                     REDUCTION_PLAN_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     SCHOOLCD                = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "                     AND SCHOOL_KIND         = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "                     AND YEAR                = '".CTRL_YEAR."' ";
        $query .= "                     AND REDUCTION_TARGET    = '1' ";
        $query .= "                 GROUP BY ";
        $query .= "                     SLIP_NO ";
        $query .= "                 ) PREF_PLAN ON STD_INFO.SLIP_NO = PREF_PLAN.SLIP_NO ";
        $query .= "     LEFT JOIN (SELECT ";
        $query .= "                     PREF_GRADE.* ";
        $query .= "                 FROM ";
        $query .= "                     REDUCTION_PREF_GRADE_MST PREF_GRADE ";
        $query .= "                     INNER JOIN NAME_MST N1 ";
        $query .= "                          ON N1.NAMECD1  = 'G202' ";
        $query .= "                         AND N1.NAMECD2  = PREF_GRADE.PREFECTURESCD ";
        $query .= "                 WHERE ";
        $query .= "                     PREF_GRADE.YEAR = '".CTRL_YEAR."' ";
        $query .= "                 ) PREF_GRADE ON STD_INFO.GRADE = PREF_GRADE.GRADE AND GUARDIAN.PREFECTURESCD = PREF_GRADE.PREFECTURESCD  ";
        //負担金
        $query .= "     LEFT JOIN REDUCTION_SLIP_BURDEN_CHARGE_DAT BURDEN_CHARGE ";
        $query .= "          ON BURDEN_CHARGE.SCHOOLCD      = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "         AND BURDEN_CHARGE.SCHOOL_KIND   = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "         AND BURDEN_CHARGE.YEAR          = STD_INFO.YEAR ";
        $query .= "         AND BURDEN_CHARGE.SLIP_NO       = STD_INFO.SLIP_NO ";
        $query .= "         AND BURDEN_CHARGE.REDUCTION_TARGET = '1' ";
        //入金済み
        $query .= "     LEFT JOIN (SELECT ";
        $query .= "                     SLIP_NO, ";
        $query .= "                     CASE WHEN SUM(CASE WHEN PLAN_MONTH IN ('04','05','06') THEN 1 ELSE 0 END) > 0 THEN 1 ELSE 0 END AS PAID_FLG1, ";
        $query .= "                     CASE WHEN SUM(CASE WHEN PLAN_MONTH IN ('04','05','06') THEN 0 ELSE 1 END) > 0 THEN 1 ELSE 0 END AS PAID_FLG2 ";
        $query .= "                 FROM ";
        $query .= "                     COLLECT_SLIP_PLAN_PAID_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     SCHOOLCD            = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "                     AND SCHOOL_KIND     = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "                     AND YEAR            = '".CTRL_YEAR."' ";
        $query .= "                     AND PLAN_PAID_MONEY_DATE IS NOT NULL ";
        $query .= "                 GROUP BY ";
        $query .= "                     SLIP_NO ";
        $query .= "                 ) PAID_D ON STD_INFO.SLIP_NO = PAID_D.SLIP_NO ";
        //入金済み（生徒）
        $query .= "     LEFT JOIN (SELECT ";
        $query .= "                     SCHREGNO, ";
        $query .= "                     CASE WHEN SUM(CASE WHEN PLAN_MONTH IN ('04','05','06') THEN 1 ELSE 0 END) > 0 THEN 1 ELSE 0 END AS SCH_PAID_FLG1, ";
        $query .= "                     CASE WHEN SUM(CASE WHEN PLAN_MONTH IN ('04','05','06') THEN 0 ELSE 1 END) > 0 THEN 1 ELSE 0 END AS SCH_PAID_FLG2 ";
        $query .= "                 FROM ";
        $query .= "                     COLLECT_SLIP_PLAN_PAID_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     SCHOOLCD            = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "                     AND SCHOOL_KIND     = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "                     AND YEAR            = '".CTRL_YEAR."' ";
        $query .= "                     AND PLAN_PAID_MONEY_DATE IS NOT NULL ";
        $query .= "                 GROUP BY ";
        $query .= "                     SCHREGNO ";
        $query .= "                 ) SCH_PAID_D ON STD_INFO.SCHREGNO = SCH_PAID_D.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     ATTENDNO, ";
        $query .= "     STD_INFO.SLIP_NO ";

        return $query;
    }

    //年組
    public function selectQueryGrade(&$model)
    {
        $query  = " SELECT ";
        $query .= "     REGD_H.GRADE, ";
        $query .= "     REGD_H.HR_CLASS, ";
        $query .= "     REGD_H.HR_NAME, ";
        $query .= "     REGD_H.HR_NAMEABBV, ";
        $query .= "     REGD_G.SCHOOL_KIND, ";
        $query .= "     T2.STAFFCD, ";
        $query .= "     T2.STAFFNAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT REGD_H";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT REGD_G ON REGD_H.YEAR = REGD_G.YEAR ";
        $query .= "          AND REGD_H.GRADE = REGD_G.GRADE, ";
        $query .= "     STAFF_MST T2 ";
        $query .= " WHERE ";
        if ($model->usr_auth == DEF_UPDATE_RESTRICT) {
            $query .= "     REGD_H.TR_CD1 = '". STAFFCD ."' AND";
        }
        $query .= "      REGD_H.TR_CD1 = T2.STAFFCD AND ";
        $query .= "      REGD_H.YEAR = '" .CTRL_YEAR ."' AND ";
        $query .= "      REGD_H.SEMESTER = '" .CTRL_SEMESTER ."' ";
        $query .= " ORDER BY ";
        $query .= "     REGD_H.GRADE, ";
        $query .= "     REGD_H.HR_CLASS ";

        return $query;
    }

    //生徒授業料
    public function getSlipPlan($model)
    {
        $query  = knjp721Query::getRegdWith($model);
        $query .= " SELECT ";
        $query .= "     SLIP_PLAN.SCHREGNO, ";
        $query .= "     SLIP_T.SLIP_NO, ";
        $query .= "     SLIP_PLAN.COLLECT_L_CD, ";
        $query .= "     SLIP_PLAN.COLLECT_M_CD, ";
        $query .= "     VALUE(M_MST.GAKUNOKIN_DIV,'0') AS GAKUNOKIN_DIV, ";
        $query .= "     VALUE(M_MST.REDUCTION_DIV,'0') AS REDUCTION_DIV, ";
        $query .= "     VALUE(M_MST.IS_REDUCTION_SCHOOL,'0') AS IS_REDUCTION_SCHOOL, ";
        $query .= "     SUM(SLIP_PLAN.PLAN_MONEY) AS MONEY ";
        $query .= " FROM ";
        $query .= "     SLIP_T, ";
        $query .= "     COLLECT_SLIP_DAT SLIP_D, ";
        $query .= "     COLLECT_SLIP_PLAN_M_DAT SLIP_PLAN, ";
        $query .= "     COLLECT_M_MST M_MST ";
        $query .= " WHERE ";
        $query .= "     SLIP_T.SCHREGNO             = SLIP_D.SCHREGNO ";
        $query .= "     AND SLIP_T.SLIP_NO          = SLIP_D.SLIP_NO ";
        $query .= "     AND SLIP_D.SCHOOLCD         = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SLIP_D.SCHOOL_KIND      = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND SLIP_D.YEAR             = '".CTRL_YEAR."' ";
        $query .= "     AND SLIP_D.CANCEL_DATE IS NULL ";
        $query .= "     AND SLIP_D.SCHOOLCD         = SLIP_PLAN.SCHOOLCD ";
        $query .= "     AND SLIP_D.SCHOOL_KIND      = SLIP_PLAN.SCHOOL_KIND ";
        $query .= "     AND SLIP_D.YEAR             = SLIP_PLAN.YEAR ";
        $query .= "     AND SLIP_D.SLIP_NO          = SLIP_PLAN.SLIP_NO ";
        $query .= "     AND SLIP_D.SCHREGNO         = SLIP_PLAN.SCHREGNO ";
        $query .= "     AND SLIP_PLAN.SCHOOLCD      = M_MST.SCHOOLCD ";
        $query .= "     AND SLIP_PLAN.SCHOOL_KIND   = M_MST.SCHOOL_KIND ";
        $query .= "     AND SLIP_PLAN.YEAR          = M_MST.YEAR ";
        $query .= "     AND SLIP_PLAN.COLLECT_L_CD  = M_MST.COLLECT_L_CD ";
        $query .= "     AND SLIP_PLAN.COLLECT_M_CD  = M_MST.COLLECT_M_CD ";
        $query .= "     AND VALUE(M_MST.KOUHI_SHIHI, '')    = '1' ";
        $query .= "     AND VALUE(M_MST.GAKUNOKIN_DIV, '') != '2' ";
        $query .= "     AND (VALUE(M_MST.REDUCTION_DIV, '') IN ('1','2') OR VALUE(M_MST.IS_REDUCTION_SCHOOL, '') = '1') ";
        $query .= " GROUP BY ";
        $query .= "     SLIP_PLAN.SCHREGNO, ";
        $query .= "     SLIP_T.SLIP_NO, ";
        $query .= "     SLIP_PLAN.COLLECT_L_CD, ";
        $query .= "     SLIP_PLAN.COLLECT_M_CD, ";
        $query .= "     GAKUNOKIN_DIV, ";
        $query .= "     REDUCTION_DIV, ";
        $query .= "     IS_REDUCTION_SCHOOL ";
        $query .= " ORDER BY ";
        $query .= "     SLIP_PLAN.SCHREGNO, ";
        $query .= "     SLIP_T.SLIP_NO, ";
        $query .= "     SLIP_PLAN.COLLECT_L_CD, ";
        $query .= "     SLIP_PLAN.COLLECT_M_CD, ";
        $query .= "     GAKUNOKIN_DIV, ";
        $query .= "     REDUCTION_DIV, ";
        $query .= "     IS_REDUCTION_SCHOOL ";

        return $query;
    }

    //府県補助マスタ
    public function getPrefGradeMst($model)
    {
        $query  = " SELECT ";
        $query .= "     PREFECTURESCD, ";
        $query .= "     GRADE, ";
        $query .= "     REFER_YEAR_DIV1, ";
        $query .= "     TOTALL_MONEY_1, ";
        $query .= "     REFER_YEAR_DIV2, ";
        $query .= "     TOTALL_MONEY_2, ";
        $query .= "     USE_RANK, ";
        $query .= "     ZENKI_KAISI_YEAR, ";
        $query .= "     KOUKI_KAISI_YEAR, ";
        $query .= "     STANDARD_SCHOOL_FEE ";
        $query .= " FROM ";
        $query .= "     REDUCTION_PREF_GRADE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '{$model->search["GRADE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     PREFECTURESCD ";

        return $query;
    }

    //補助金MST
    public function getReductionMst($model)
    {
        $query  = " SELECT ";
        $query .= "     REDUC_M.PREFECTURESCD, ";
        $query .= "     REDUC_M.MONEY_DIV1, ";
        $query .= "     REDUC_M.NUMERATOR1, ";
        $query .= "     REDUC_M.DENOMINATOR1, ";
        $query .= "     REDUC_M.REDUCTIONMONEY_1, ";
        $query .= "     REDUC_M.INCOME_LOW1, ";
        $query .= "     REDUC_M.INCOME_HIGH1, ";
        $query .= "     REDUC_M.INCOME_RANK1, ";
        $query .= "     REDUC_M.BURDEN_CHARGE_FLG1, ";
        $query .= "     REDUC_M.MONEY_DIV2, ";
        $query .= "     REDUC_M.NUMERATOR2, ";
        $query .= "     REDUC_M.DENOMINATOR2, ";
        $query .= "     REDUC_M.REDUCTIONMONEY_2, ";
        $query .= "     REDUC_M.INCOME_LOW2, ";
        $query .= "     REDUC_M.INCOME_HIGH2, ";
        $query .= "     REDUC_M.INCOME_RANK2, ";
        $query .= "     REDUC_M.BURDEN_CHARGE_FLG2 ";
        $query .= " FROM ";
        $query .= "     REDUCTION_MST REDUC_M ";
        $query .= " WHERE ";
        $query .= "     REDUC_M.SCHOOLCD                = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND REDUC_M.SCHOOL_KIND         = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND REDUC_M.YEAR                = '".CTRL_YEAR."' ";
        $query .= "     AND REDUC_M.REDUCTION_TARGET    = '1' ";
        $query .= "     AND REDUC_M.GRADE               = '{$model->search["GRADE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     REDUC_M.PREFECTURESCD, ";
        $query .= "     REDUC_M.INCOME_RANK1, ";
        $query .= "     REDUC_M.INCOME_RANK2, ";
        $query .= "     REDUC_M.REDUCTION_SEQ ";

        return $query;
    }

    //生徒減免
    public function getReductionStd($model)
    {
        $query  = knjp721Query::getRegdWith($model);
        $query .= " SELECT ";
        $query .= "     SCHOLAR_H.SCHREGNO, ";
        $query .= "     SCHOLAR_H.FROM_DATE, ";
        $query .= "     REDUC_SCHOOL.REDUCTION_DIV_CD, ";
        $query .= "     CASE WHEN '".CTRL_YEAR."-04-01' > SCHOLAR_H.FROM_DATE ";
        $query .= "          THEN '".CTRL_YEAR."-04-01' ";
        $query .= "          ELSE SCHOLAR_H.FROM_DATE ";
        $query .= "          END AS S_YEAR_MONTH, ";
        $query .= "     CASE WHEN '".(CTRL_YEAR + 1)."-03-31' < SCHOLAR_H.TO_DATE ";
        $query .= "          THEN '".(CTRL_YEAR + 1)."-03-31' ";
        $query .= "          ELSE SCHOLAR_H.TO_DATE ";
        $query .= "          END AS E_YEAR_MONTH, ";
        $query .= "     REDUC_SCHOOL.REDUCTION_DIV_NAME, ";
        $query .= "     REDUC_SCHOOL.SEMESTER_DIV, ";
        $query .= "     REDUC_SCHOOL.VALID_MONTH, ";
        $query .= "     REDUC_SCHOOL.REDUCTION_DIV, ";
        $query .= "     REDUC_SCH_TRGT.MONEY_DIV, ";
        $query .= "     REDUC_SCH_TRGT.NUMERATOR, ";
        $query .= "     REDUC_SCH_TRGT.DENOMINATOR, ";
        $query .= "     REDUC_SCH_TRGT.MONEY, ";
        $query .= "     SCHOLAR_H.REMARK, ";
        $query .= "     REDUC_SCHOOL.REDUCTION_TIMING ";
        $query .= " FROM ";
        $query .= "     REGD_T, ";
        $query .= "     SCHREG_SCHOLARSHIP_HIST_DAT SCHOLAR_H, ";
        $query .= "     REDUCTION_SCHOOL_MST REDUC_SCHOOL ";
        $query .= "     INNER JOIN REDUCTION_SCHOOL_TARGET_MST REDUC_SCH_TRGT ";
        $query .= "          ON REDUC_SCH_TRGT.SCHOOLCD         = REDUC_SCHOOL.SCHOOLCD ";
        $query .= "         AND REDUC_SCH_TRGT.SCHOOL_KIND      = REDUC_SCHOOL.SCHOOL_KIND ";
        $query .= "         AND REDUC_SCH_TRGT.YEAR             = REDUC_SCHOOL.YEAR ";
        $query .= "         AND REDUC_SCH_TRGT.REDUCTION_DIV_CD = REDUC_SCHOOL.REDUCTION_DIV_CD ";
        $query .= "         AND REDUC_SCH_TRGT.REDUCTION_TARGET = '1' ";
        $query .= " WHERE ";
        $query .= "     REGD_T.SCHREGNO = SCHOLAR_H.SCHREGNO ";
        $query .= "     AND SCHOLAR_H.SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOLAR_H.SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND ('".CTRL_YEAR."-04-01' BETWEEN SCHOLAR_H.FROM_DATE AND SCHOLAR_H.TO_DATE ";
        $query .= "          OR ";
        $query .= "          '".(CTRL_YEAR + 1)."-03-31' BETWEEN SCHOLAR_H.FROM_DATE AND SCHOLAR_H.TO_DATE ";
        $query .= "          OR ";
        $query .= "          SCHOLAR_H.FROM_DATE BETWEEN '".CTRL_YEAR."-04-01' AND '".(CTRL_YEAR + 1)."-03-31' ";
        $query .= "         ) ";
        $query .= "     AND SCHOLAR_H.SCHOOLCD      = REDUC_SCHOOL.SCHOOLCD ";
        $query .= "     AND SCHOLAR_H.SCHOOL_KIND   = REDUC_SCHOOL.SCHOOL_KIND ";
        $query .= "     AND REDUC_SCHOOL.YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOLAR_H.SCHOLARSHIP   = REDUC_SCHOOL.REDUCTION_DIV_CD ";
        $query .= " ORDER BY ";
        $query .= "     SCHOLAR_H.SCHREGNO, ";
        $query .= "     REDUC_SCHOOL.REDUCTION_TIMING, ";
        $query .= "     SCHOLAR_H.FROM_DATE ";

        return $query;
    }

    //支援金
    public function countryMstMonty($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNTRY_M.REDUCTIONMONEY1, ";
        $query .= "     COUNTRY_M.INCOME_LOW1, ";
        $query .= "     COUNTRY_M.INCOME_HIGH1, ";
        $query .= "     COUNTRY_M.INCOME_RANK1, ";
        $query .= "     COUNTRY_M.REDUCTIONMONEY2, ";
        $query .= "     COUNTRY_M.INCOME_LOW2, ";
        $query .= "     COUNTRY_M.INCOME_HIGH2, ";
        $query .= "     COUNTRY_M.INCOME_RANK2 ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_MST COUNTRY_M ";
        $query .= " WHERE ";
        $query .= "     COUNTRY_M.SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND COUNTRY_M.SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND COUNTRY_M.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND COUNTRY_M.GRADE = '".$model->search["GRADE"]."' ";

        return $query;
    }

    //支援金加算
    public function countryAddMstMonty($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNTRY_ADD.REDUCTION_ADD_MONEY1, ";
        $query .= "     COUNTRY_ADD.INCOME_LOW1, ";
        $query .= "     COUNTRY_ADD.INCOME_HIGH1, ";
        $query .= "     COUNTRY_ADD.INCOME_RANK1, ";
        $query .= "     COUNTRY_ADD.REDUCTION_ADD_MONEY2, ";
        $query .= "     COUNTRY_ADD.INCOME_LOW2, ";
        $query .= "     COUNTRY_ADD.INCOME_HIGH2, ";
        $query .= "     COUNTRY_ADD.INCOME_RANK2 ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_MST COUNTRY_ADD ";
        $query .= " WHERE ";
        $query .= "     COUNTRY_ADD.SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND COUNTRY_ADD.SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND COUNTRY_ADD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND COUNTRY_ADD.GRADE = '".$model->search["GRADE"]."' ";

        return $query;
    }

    //名称マスタ
    public function getNameMstSql($model, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        $query .= " UNION ALL VALUES('XX', 'なし') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //支援金（基準額）取得
    public function selectQueryReductionMonty(&$model, $data, $setSoeji, $flg)
    {
        $query  = " SELECT ";
        $query .= "     T1.REDUCTION_SEQ, ";
        $query .= "     T1.REDUCTIONMONEY{$setSoeji} AS COUNTRY_MONEY, ";
        $query .= "     T1.INCOME_RANK{$setSoeji} AS COUNTRY_RANK ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.GRADE = '".$data["GRADE"]."' ";
        $query .= "     AND T1.YEAR = '".CTRL_YEAR."' ";
        if ($flg == 'rank') {
            $query .= "     AND T1.INCOME_RANK{$setSoeji} = '".$data["COUNTRY_RANK_{$setSoeji}"]."' ";
        } else {
            $query .= "     AND ".$data["REDUC_INCOME_{$setSoeji}"]." BETWEEN T1.INCOME_LOW{$setSoeji} AND T1.INCOME_HIGH{$setSoeji} ";
        }
        $query .= " ORDER BY ";
        $query .= "     COUNTRY_MONEY, ";
        $query .= "     REDUCTION_SEQ ";
        $query .= " FETCH FIRST 1 ROWS ONLY ";

        return $query;
    }

    //支援金（加算額）取得
    public function selectQueryReductionAddMonty(&$model, $data, $setSoeji, $flg)
    {
        $query  = " SELECT ";
        $query .= "     T1.REDUCTION_SEQ, ";
        $query .= "     T1.REDUCTION_ADD_MONEY{$setSoeji} AS COUNTRY_ADD_MONEY ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.GRADE = '".$data["GRADE"]."' ";
        $query .= "     AND T1.YEAR = '".CTRL_YEAR."' ";
        if ($flg == 'rank') {
            $query .= "     AND T1.INCOME_RANK{$setSoeji} = '".$data["COUNTRY_RANK_{$setSoeji}"]."' ";
        } else {
            $query .= "     AND ".$data["REDUC_INCOME_{$setSoeji}"]." BETWEEN T1.INCOME_LOW{$setSoeji} AND T1.INCOME_HIGH{$setSoeji} ";
        }
        $query .= " ORDER BY ";
        $query .= "     COUNTRY_ADD_MONEY, ";
        $query .= "     REDUCTION_SEQ ";
        $query .= " FETCH FIRST 1 ROWS ONLY ";

        return $query;
    }

    //都道府県コード
    public function selectQueryReductionPref(&$model)
    {
        $query = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1 = 'G202' AND ";
        if (is_numeric($model->income["PREFECTURESCD"])) {     //軽減対象都道府県設定値
            $query .= "     T1.NAMECD2 = '".$model->income["PREFECTURESCD"]."' ";
        } else {                                                //保護者都道府県
            $query .= "     T1.NAMECD2 = (SELECT ";
            $query .= "                       SUBSTR(MIN(ST1.CITYCD),1,2) ";
            $query .= "                   FROM ";
            $query .= "                       ZIPCD_MST ST1, ";
            $query .= "                       GUARDIAN_DAT ST2 ";
            $query .= "                   WHERE ";
            $query .= "                       ST1.NEW_ZIPCD = ST2.GUARANTOR_ZIPCD AND ";
            $query .= "                       ST2.SCHREGNO = '".$model->income["SCHREGNO"]."' ";
            $query .= "                   ) ";
        }
        return $query;
    }

    public function getPlanCnt($model, $slipNo)
    {
        $query  = " SELECT ";
        $query .= "   COUNT(*) AS ALLCNT, ";
        $query .= "   SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH < '".CTRL_YEAR."07' AND PLAN_MONEY IS NOT NULL ";
        $query .= "            THEN 1 ";
        $query .= "            ELSE 0 ";
        $query .= "       END ";
        $query .= "   ) AS ZENKI_CNT, ";
        $query .= "   SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH >= '".CTRL_YEAR."07' AND PLAN_MONEY IS NOT NULL ";
        $query .= "            THEN 1 ";
        $query .= "            ELSE 0 ";
        $query .= "       END ";
        $query .= "   ) AS KOUKI_CNT, ";
        $query .= "   SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH < '".CTRL_YEAR."07' AND ADD_PLAN_MONEY IS NOT NULL ";
        $query .= "            THEN 1 ";
        $query .= "            ELSE 0 ";
        $query .= "       END ";
        $query .= "   ) AS ZENKI_ADD_CNT, ";
        $query .= "   SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH >= '".CTRL_YEAR."07' AND ADD_PLAN_MONEY IS NOT NULL ";
        $query .= "            THEN 1 ";
        $query .= "            ELSE 0 ";
        $query .= "       END ";
        $query .= "   ) AS KOUKI_ADD_CNT ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SLIP_NO = '".$slipNo."' ";
        return $query;
    }

    public function getPrefPlanMonth($model, $slipNo)
    {
        $query  = " SELECT ";
        $query .= "     PLAN_MONTH ";
        $query .= " FROM ";
        $query .= "     REDUCTION_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD                = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND         = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND YEAR                = '".CTRL_YEAR."' ";
        $query .= "     AND REDUCTION_TARGET    = '1' ";
        $query .= "     AND SLIP_NO             = '{$slipNo}' ";
        return $query;
    }

    public function getReducSchoolPlanMonth($model, $slipNo)
    {
        $query  = " SELECT ";
        $query .= "     PLAN_MONTH ";
        $query .= " FROM ";
        $query .= "     REDUCTION_SCHOOL_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD                = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND         = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND YEAR                = '".CTRL_YEAR."' ";
        $query .= "     AND SLIP_NO             = '{$slipNo}' ";
        $query .= "     AND REDUCTION_TARGET    = '1' ";
        return $query;
    }

    public function getBurdenChargePlanMonth($model, $slipNo)
    {
        $query  = " SELECT ";
        $query .= "     PLAN_MONTH ";
        $query .= " FROM ";
        $query .= "     REDUCTION_BURDEN_CHARGE_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD                = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND         = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND YEAR                = '".CTRL_YEAR."' ";
        $query .= "     AND REDUCTION_TARGET    = '1' ";
        $query .= "     AND SLIP_NO             = '{$slipNo}' ";
        return $query;
    }

    public function update($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        for ($i = 0; $i < get_count($model->slipno); $i++) {
            $slipNo = $model->slipno[$i];
            $schregNo = $model->slipSchreg[$slipNo];

            //入金済みの場合、処理しない
            if ($model->field["PAID_FLG1"][$slipNo] == '1' && $model->field["PAID_FLG2"][$slipNo] == '1') {
                continue;
            }

            knjp721Query::incomeUpdate($db, $model, $slipNo, $schregNo, $i);
            knjp721Query::countryUpdate($db, $model, $slipNo, $schregNo, $i);
            $prefFlg = knjp721Query::prefUpdate($db, $model, $slipNo, $schregNo, $i);
            knjp721Query::reductionSchoolUpdate($db, $model, $slipNo, $schregNo, $i);
            knjp721Query::updateReducBurdenCharge($db, $model, $slipNo, $schregNo, $i, $prefFlg);
            knjp721Query::updatePlanSum($db, $model, $slipNo, $schregNo);
        }
        $db->commit();
        Query::dbCheckIn($db);
    }

    public function incomeUpdate($db, $model, $slipNo, $schregNo, $rowCnt)
    {
        /*************************/
        /* REDUCTION_INCOME_DAT */
        /*************************/
        $data = array();

        if ($model->field["SCH_PAID_FLG1"][$slipNo] != '1') {
            $data["INCOME_1"][NUMBER]       = strlen($model->field["REDUC_INCOME_1"][$rowCnt]) >   0 ? $model->field["REDUC_INCOME_1"][$rowCnt]   : null;
            $data["INCOME_1_1"][NUMBER]     = strlen($model->field["REDUC_INCOME_1_1"][$rowCnt]) > 0 ? $model->field["REDUC_INCOME_1_1"][$rowCnt] : null;
            $data["INCOME_1_2"][NUMBER]     = strlen($model->field["REDUC_INCOME_1_2"][$rowCnt]) > 0 ? $model->field["REDUC_INCOME_1_2"][$rowCnt] : null;
            $data["INCOME_1_3"][NUMBER]     = strlen($model->field["REDUC_INCOME_1_3"][$rowCnt]) > 0 ? $model->field["REDUC_INCOME_1_3"][$rowCnt] : null;
            $data["INCOME_1_4"][NUMBER]     = strlen($model->field["REDUC_INCOME_1_4"][$rowCnt]) > 0 ? $model->field["REDUC_INCOME_1_4"][$rowCnt] : null;
        }
        if ($model->field["SCH_PAID_FLG2"][$slipNo] != '1') {
            $data["INCOME_2"][NUMBER]       = strlen($model->field["REDUC_INCOME_2"][$rowCnt])   > 0 ? $model->field["REDUC_INCOME_2"][$rowCnt]   : null;
            $data["INCOME_2_1"][NUMBER]     = strlen($model->field["REDUC_INCOME_2_1"][$rowCnt]) > 0 ? $model->field["REDUC_INCOME_2_1"][$rowCnt] : null;
            $data["INCOME_2_2"][NUMBER]     = strlen($model->field["REDUC_INCOME_2_2"][$rowCnt]) > 0 ? $model->field["REDUC_INCOME_2_2"][$rowCnt] : null;
            $data["INCOME_2_3"][NUMBER]     = strlen($model->field["REDUC_INCOME_2_3"][$rowCnt]) > 0 ? $model->field["REDUC_INCOME_2_3"][$rowCnt] : null;
            $data["INCOME_2_4"][NUMBER]     = strlen($model->field["REDUC_INCOME_2_4"][$rowCnt]) > 0 ? $model->field["REDUC_INCOME_2_4"][$rowCnt] : null;
        }

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $query  = " SELECT * FROM ";
        $query .= "     REDUCTION_INCOME_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND YEAR        = '".CTRL_YEAR ."' ";
        $query .= "     AND SCHREGNO    = '{$schregNo}' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if (is_array($row)) {
            /**********/
            /* UPDATE */
            /**********/

            $where  = " WHERE ";
            $where .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
            $where .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
            $where .= "     AND YEAR        = '".CTRL_YEAR ."' ";
            $where .= "     AND SCHREGNO    = '{$schregNo}' ";

            $query = Query::updateSQL($data, "REDUCTION_INCOME_DAT", $where);
            $result = $db->query($query);
        } else {

            /**********/
            /* INSERT */
            /**********/
            $data["SCHOOLCD"][TEXT]     = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]  = $model->schoolKindAry[$model->search["GRADE"]];
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]     = $schregNo;

            $query = Query::insertSQL($data, "REDUCTION_INCOME_DAT");
            $result = $db->query($query);
        }
    }

    public function countryUpdate($db, $model, $slipNo, $schregNo, $rowCnt)
    {

        //データ取得 -- REDUCTION_SCHOOL_DAT
        $query = knjp721Query::getReductionSchoolDat($model, $slipNo);
        $rowReduc = $db->getRow($query, DB_FETCHMODE_ASSOC);

        /*************************/
        /* REDUCTION_COUNTRY_DAT */
        /*************************/
        if (is_array($model->field["COUNTRY_BASE_FLG_1"])) {
            $baseFlg1 = in_array($slipNo, $model->field["COUNTRY_BASE_FLG_1"]) ? 1 : null;
        } else {
            $baseFlg1 = null;
        }
        if (is_array($model->field["COUNTRY_ADD_FLG_1"])) {
            $addFlg1 = in_array($slipNo, $model->field["COUNTRY_ADD_FLG_1"]) ? 1 : null;
        } else {
            $addFlg1 = null;
        }
        if (is_array($model->field["COUNTRY_BASE_FLG_2"])) {
            $baseFlg2 = in_array($slipNo, $model->field["COUNTRY_BASE_FLG_2"]) ? 1 : null;
        } else {
            $baseFlg2 = null;
        }
        if (is_array($model->field["COUNTRY_ADD_FLG_2"])) {
            $addFlg2 = in_array($slipNo, $model->field["COUNTRY_ADD_FLG_2"]) ? 1 : null;
        } else {
            $addFlg2 = null;
        }

        $data = array();
        $data["SCHREGNO"][TEXT]                 = $schregNo;
        $data["GRADE"][TEXT]                    = $model->search["GRADE"];

        if ($model->field["PAID_FLG1"][$slipNo] != '1') {
            $data["RARE_CASE_CD_1"][TEXT]           = null;
            $data["SEQ_1"][NUMBER]                  = is_numeric($model->field["REDUCTION_SEQ_1"][$rowCnt]) ? $model->field["REDUCTION_SEQ_1"][$rowCnt] : null;
            $data["RANK_1"][TEXT]                   = $model->field["COUNTRY_RANK_1"][$rowCnt];
            $data["MONEY_1"][NUMBER]                = strlen($model->field["COUNTRY_MONEY_1"][$rowCnt]) > 0 ? $model->field["COUNTRY_MONEY_1"][$rowCnt] : null;
            $data["DEC_FLG_1"][TEXT]                = $baseFlg1;
            $data["ADD_MONEY_1"][NUMBER]            = strlen($model->field["COUNTRY_ADD_MONEY_1"][$rowCnt]) > 0 ? $model->field["COUNTRY_ADD_MONEY_1"][$rowCnt]   : null;
            $data["ADD_DEC_FLG_1"][TEXT]            = $addFlg1;
        }
        if ($model->field["PAID_FLG2"][$slipNo] != '1') {
            $data["RARE_CASE_CD_2"][TEXT]           = null;
            $data["SEQ_2"][NUMBER]                  = is_numeric($model->field["REDUCTION_SEQ_2"][$rowCnt]) ? $model->field["REDUCTION_SEQ_2"][$rowCnt] : null;
            $data["RANK_2"][TEXT]                   = $model->field["COUNTRY_RANK_2"][$rowCnt];
            $data["MONEY_2"][NUMBER]                = strlen($model->field["COUNTRY_MONEY_2"][$rowCnt]) > 0 ? $model->field["COUNTRY_MONEY_2"][$rowCnt]   : null;
            $data["DEC_FLG_2"][TEXT]                = $baseFlg2;
            $data["ADD_MONEY_2"][NUMBER]            = strlen($model->field["COUNTRY_ADD_MONEY_2"][$rowCnt]) > 0 ? $model->field["COUNTRY_ADD_MONEY_2"][$rowCnt] : null;
            $data["ADD_DEC_FLG_2"][TEXT]            = $addFlg2;
        }

        if ($rowReduc["DEC_FLG_1"] != "1" || $rowReduc["DEC_FLG_2"] != "1") {
            $reducJugyouryou = 0;
            $reducJugyouryou += ($rowReduc["DEC_FLG_1"] == "1") ? $rowReduc["MONEY_1"] : $model->slipSchoolStd[$slipNo]["REDUCTION_JUGYOURYOU1"];
            $reducJugyouryou += ($rowReduc["DEC_FLG_2"] == "1") ? $rowReduc["MONEY_2"] : $model->slipSchoolStd[$slipNo]["REDUCTION_JUGYOURYOU2"];
            $data["REDUCTION_JUGYOURYOU"][NUMBER]   = $reducJugyouryou;
        }
        $data["REDUCTION_JUGYOURYOUTOU"][NUMBER]    = null;

        $data["REMARK"][TEXT]                   = $model->field["REDUC_REMARK"][$rowCnt] ? $model->field["REDUC_REMARK"][$rowCnt] : null;
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "SYSDATE()";

        $query  = " SELECT * FROM ";
        $query .= "     REDUCTION_COUNTRY_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND YEAR        = '".CTRL_YEAR ."' ";
        $query .= "     AND SLIP_NO     = '{$slipNo}' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if (is_array($row)) {
            /**********/
            /* UPDATE */
            /**********/

            $where  = " WHERE ";
            $where .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
            $where .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
            $where .= "     AND YEAR        = '".CTRL_YEAR ."' ";
            $where .= "     AND SLIP_NO     = '{$slipNo}' ";

            $query = Query::updateSQL($data, "REDUCTION_COUNTRY_DAT", $where);
            $result = $db->query($query);
        } else {

            /**********/
            /* INSERT */
            /**********/
            $data["SCHOOLCD"][TEXT]     = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]  = $model->schoolKindAry[$model->search["GRADE"]];
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["SLIP_NO"][TEXT]      = $slipNo;

            $query = Query::insertSQL($data, "REDUCTION_COUNTRY_DAT");
            $result = $db->query($query);
        }

        /******************************/
        /* REDUCTION_COUNTRY_PLAN_DAT */
        /******************************/
        //計画データの有無
        $planCnt = $db->getRow(knjp721Query::getPlanCnt($model, $slipNo), DB_FETCHMODE_ASSOC);
        $zenkiData = array("DEC" => $baseFlg1 == "1" ? $model->field["COUNTRY_MONEY_1"][$rowCnt] : 0,
                           "ADD" => $addFlg1  == "1" ? $model->field["COUNTRY_ADD_MONEY_1"][$rowCnt] : 0);
        $koukiData = array("DEC" => $baseFlg2 == "1" ? $model->field["COUNTRY_MONEY_2"][$rowCnt] : 0,
                           "ADD" => $addFlg2  == "1" ? $model->field["COUNTRY_ADD_MONEY_2"][$rowCnt] : 0);

        $planCnt["ZENKI_FLG"]       = ($baseFlg1 == '1' && $row["DEC_FLG_1"] != "1") ? 1 : 0;
        $planCnt["ZENKI_ADD_FLG"]   = ($addFlg1 == '1' && $row["ADD_DEC_FLG_1"] != "1") ? 1 : 0;
        $planCnt["KOUKI_FLG"]       = ($baseFlg2 == '1' && $row["DEC_FLG_2"] != "1") ? 1 : 0;
        $planCnt["KOUKI_ADD_FLG"]   = ($addFlg2 == '1' && $row["ADD_DEC_FLG_2"] != "1") ? 1 : 0;

        //計画データ削除(確定フラグ:OFF)
        if ($baseFlg1 != "1" && $addFlg1 != "1" && $baseFlg2 != "1" && $addFlg2 != "1" && $model->field["PAID_FLG1"][$slipNo] != '1' && $model->field["PAID_FLG2"][$slipNo] != '1') {
            $query = knjp721Query::planDecisionMoneyAllClear($model, $slipNo);
            $db->query($query);
        } else {
            //前期
            if ($model->field["PAID_FLG1"][$slipNo] != '1') {
                $zenkiMonthArray = array("04", "05", "06");
                if ($baseFlg1 != "1") {
                    $query = knjp721Query::planMoneyClear($model, $slipNo, "", $zenkiMonthArray);
                    $db->query($query);
                    $query = knjp721Query::planDecisionMoneyClear($model, $slipNo, "", $zenkiMonthArray);
                    $db->query($query);
                    $query = knjp721Query::planDecisionMoneyClear($model, $slipNo, "", $zenkiMonthArray, "1");
                    $db->query($query);
                }
                if ($addFlg1 != "1") {
                    $query = knjp721Query::planMoneyClear($model, $slipNo, "ADD_", $zenkiMonthArray);
                    $db->query($query);
                    $query = knjp721Query::planDecisionMoneyClear($model, $slipNo, "ADD_", $zenkiMonthArray);
                    $db->query($query);
                    $query = knjp721Query::planDecisionMoneyClear($model, $slipNo, "ADD_", $zenkiMonthArray, "1");
                    $db->query($query);
                }
            }

            //後期
            if ($model->field["PAID_FLG2"][$slipNo] != '1') {
                $koukiMonthArray = array("07", "08", "09", "10", "11", "12", "01", "02", "03");
                if ($baseFlg2 != "1") {
                    $query = knjp721Query::planMoneyClear($model, $slipNo, "", $koukiMonthArray);
                    $db->query($query);
                    $query = knjp721Query::planDecisionMoneyClear($model, $slipNo, "", $koukiMonthArray);
                    $db->query($query);
                    $query = knjp721Query::planDecisionMoneyClear($model, $slipNo, "", $koukiMonthArray, "2");
                    $db->query($query);
                }
                if ($addFlg2 != "1") {
                    $query = knjp721Query::planMoneyClear($model, $slipNo, "ADD_", $koukiMonthArray);
                    $db->query($query);
                    $query = knjp721Query::planDecisionMoneyClear($model, $slipNo, "ADD_", $koukiMonthArray);
                    $db->query($query);
                    $query = knjp721Query::planDecisionMoneyClear($model, $slipNo, "ADD_", $koukiMonthArray, "2");
                    $db->query($query);
                }
            }
        }

        $zenkiMonth = array("04", "05", "06");
        $koukiMonth = array("07", "08", "09", "10", "11", "12", "01", "02", "03");

        //授業料を上限値とする（前期）
        $zenkiJugyouryou = ($model->field["MAX_COUNTRY_JUGYOURYOU1"][$slipNo] > 0) ? $model->field["MAX_COUNTRY_JUGYOURYOU1"][$slipNo] /get_count($zenkiMonth) : 0;
        $diff = max($zenkiData["DEC"] + $zenkiData["ADD"] - $zenkiJugyouryou, 0);
        $zenkiAdd = max($zenkiData["ADD"] - $diff, 0);
        $diff = max($diff - $zenkiData["ADD"], 0);
        $zenkiDec = max($zenkiData["DEC"] - $diff, 0);
        //セット
        $zenkiData["DEC"] = $zenkiDec;
        $zenkiData["ADD"] = $zenkiAdd;

        //授業料を上限値とする（前期・合計金額）
        $zenkiJugyouryouMax = ($model->field["MAX_COUNTRY_JUGYOURYOU1"][$slipNo] > 0) ? $model->field["MAX_COUNTRY_JUGYOURYOU1"][$slipNo] : 0;
        $zenkiDataDec = $zenkiData["DEC"] * count($zenkiMonth);
        $zenkiDataAdd = $zenkiData["ADD"] * count($zenkiMonth);
        $diff = max($zenkiDataDec + $zenkiDataAdd - $zenkiJugyouryouMax, 0);
        $zenkiAddMax = max($zenkiDataAdd - $diff, 0);
        $diff = max($diff - $zenkiDataAdd, 0);
        $zenkiDecMax = max($zenkiDataDec - $diff, 0);
        //セット
        $zenkiData["DEC_MAX"] = $zenkiDecMax;
        $zenkiData["ADD_MAX"] = $zenkiAddMax;

        //授業料を上限値とする（後期）
        $koukiJugyouryou = ($model->field["MAX_COUNTRY_JUGYOURYOU2"][$slipNo] > 0) ? $model->field["MAX_COUNTRY_JUGYOURYOU2"][$slipNo] /get_count($koukiMonth) : 0;
        $diff = max($koukiData["DEC"] + $koukiData["ADD"] - $koukiJugyouryou, 0);
        $koukiAdd = max($koukiData["ADD"] - $diff, 0);
        $diff = max($diff - $koukiData["ADD"], 0);
        $koukiDec = max($koukiData["DEC"] - $diff, 0);
        //セット
        $koukiData["DEC"] = $koukiDec;
        $koukiData["ADD"] = $koukiAdd;

        //授業料を上限値とする（後期・合計金額）
        $koukiJugyouryouMax = ($model->field["MAX_COUNTRY_JUGYOURYOU2"][$slipNo] > 0) ? $model->field["MAX_COUNTRY_JUGYOURYOU2"][$slipNo] : 0;
        $koukiDataDec = $koukiData["DEC"] * count($koukiMonth);
        $koukiDataAdd = $koukiData["ADD"] * count($koukiMonth);
        $diff = max($koukiDataDec + $koukiDataAdd - $koukiJugyouryouMax, 0);
        $koukiAddMax = max($koukiDataAdd - $diff, 0);
        $diff = max($diff - $koukiDataAdd, 0);
        $koukiDecMax = max($koukiDataDec - $diff, 0);
        //セット
        $koukiData["DEC_MAX"] = $koukiDecMax;
        $koukiData["ADD_MAX"] = $koukiAddMax;

        $setDecisionMoneyArray = array();
        $setDecisionMoney = array();
        $checkMonth1 = $model->decMonth["COUNTRY_DEC_MONTH1"] < "04" ? $model->decMonth["COUNTRY_DEC_MONTH1"] + 12 : $model->decMonth["COUNTRY_DEC_MONTH1"];
        $checkMonth2 = $model->decMonth["COUNTRY_DEC_MONTH2"] < "04" ? $model->decMonth["COUNTRY_DEC_MONTH2"] + 12 : $model->decMonth["COUNTRY_DEC_MONTH2"];
        if ($planCnt["ALLCNT"] == 0) {
            //前期
            $dataDEC    = $zenkiData["DEC"];
            $dataADD    = $zenkiData["ADD"];
            $decMax     = $zenkiData["DEC_MAX"];
            $addMax     = $zenkiData["ADD_MAX"];
            $deffSumDEC = $deffSumADD = 0;
            foreach ($zenkiMonth as $key => $val) {
                //初期値セット
                $zenkiData["DEC"] = $dataDEC;
                $zenkiData["ADD"] = $dataADD;

                //差分
                $deffSumDEC += $zenkiData["DEC"] - intval($zenkiData["DEC"]);
                $deffSumADD += $zenkiData["ADD"] - intval($zenkiData["ADD"]);

                //端数加算値取得
                $addDEC = $addADD = 0;
                if (round($deffSumDEC, 3) >= 1) {
                    $addDEC++;
                    $deffSumDEC = 0;
                }
                if (round($deffSumADD, 3) >= 1) {
                    $addADD++;
                    $deffSumADD = 0;
                }
                $zenkiData["DEC"] = intval($zenkiData["DEC"]) + $addDEC;
                $zenkiData["ADD"] = intval($zenkiData["ADD"]) + $addADD;

                //最終調整値
                $decMax -= $zenkiData["DEC"];
                $addMax -= $zenkiData["ADD"];
                if ($val == "06" && $decMax > 0) {
                    $zenkiData["DEC"] += $decMax;
                }
                if ($val == "06" && $addMax > 0) {
                    $zenkiData["ADD"] += $addMax;
                }

                //セット
                $setDecisionMoney["BASE"]   = $zenkiData["DEC"];
                $setDecisionMoney["ADD"]    = $zenkiData["ADD"];

                if ($val <= $checkMonth1) {
                    $setDecisionMoneyArray["BASE"][$model->decMonth["COUNTRY_DEC_MONTH1"]] += $zenkiData["DEC"];
                    $setDecisionMoneyArray["ADD"][$model->decMonth["COUNTRY_DEC_MONTH1"]] += $zenkiData["ADD"];
                    $setDecisionMoney["BASE"] = 0;
                    $setDecisionMoney["ADD"] = 0;
                }
                knjp721Query::insertPlan($db, $model, $slipNo, $schregNo, $val, $zenkiData, $setDecisionMoney);
            }

            //後期
            $dataDEC    = $koukiData["DEC"];
            $dataADD    = $koukiData["ADD"];
            $decMax     = $koukiData["DEC_MAX"];
            $addMax     = $koukiData["ADD_MAX"];
            $deffSumDEC = $deffSumADD = 0;
            foreach ($koukiMonth as $key => $val) {
                //初期値セット
                $koukiData["DEC"] = $dataDEC;
                $koukiData["ADD"] = $dataADD;

                //差分
                $deffSumDEC += $koukiData["DEC"] - intval($koukiData["DEC"]);
                $deffSumADD += $koukiData["ADD"] - intval($koukiData["ADD"]);

                //端数加算値取得
                $addDEC = $addADD = 0;
                if (round($deffSumDEC, 3) >= 1) {
                    $addDEC++;
                    $deffSumDEC = 0;
                }
                if (round($deffSumADD, 3) >= 1) {
                    $addADD++;
                    $deffSumADD = 0;
                }
                $koukiData["DEC"] = intval($koukiData["DEC"]) + $addDEC;
                $koukiData["ADD"] = intval($koukiData["ADD"]) + $addADD;

                //最終調整値
                $decMax -= $koukiData["DEC"];
                $addMax -= $koukiData["ADD"];
                if ($val == "03" && $decMax > 0) {
                    $koukiData["DEC"] += $decMax;
                }
                if ($val == "03" && $addMax > 0) {
                    $koukiData["ADD"] += $addMax;
                }

                //セット
                $setDecisionMoney["BASE"]   = $koukiData["DEC"];
                $setDecisionMoney["ADD"]    = $koukiData["ADD"];

                $checkVal = $val < "04" ? $val + 12 : $val;
                if ($checkVal <= $checkMonth2) {
                    $setDecisionMoneyArray["BASE"][$model->decMonth["COUNTRY_DEC_MONTH2"]] += $koukiData["DEC"];
                    $setDecisionMoneyArray["ADD"][$model->decMonth["COUNTRY_DEC_MONTH2"]] += $koukiData["ADD"];
                    $setDecisionMoney["BASE"] = 0;
                    $setDecisionMoney["ADD"] = 0;
                }
                knjp721Query::insertPlan($db, $model, $slipNo, $schregNo, $val, $koukiData, $setDecisionMoney);
            }
        } else {
            //前期
            if ($model->field["PAID_FLG1"][$slipNo] != '1' && ($planCnt["ZENKI_FLG"] == "1" || $planCnt["ZENKI_ADD_FLG"] == "1")) {
                $dataDEC    = $zenkiData["DEC"];
                $dataADD    = $zenkiData["ADD"];
                $decMax     = $zenkiData["DEC_MAX"];
                $addMax     = $zenkiData["ADD_MAX"];
                $deffSumDEC = $deffSumADD = 0;
                foreach ($zenkiMonth as $key => $val) {
                    //初期値セット
                    $zenkiData["DEC"] = $dataDEC;
                    $zenkiData["ADD"] = $dataADD;

                    //差分
                    $deffSumDEC += $zenkiData["DEC"] - intval($zenkiData["DEC"]);
                    $deffSumADD += $zenkiData["ADD"] - intval($zenkiData["ADD"]);

                    //端数加算値取得
                    $addDEC = $addADD = 0;
                    if (round($deffSumDEC, 3) >= 1) {
                        $addDEC++;
                        $deffSumDEC = 0;
                    }
                    if (round($deffSumADD, 3) >= 1) {
                        $addADD++;
                        $deffSumADD = 0;
                    }
                    $zenkiData["DEC"] = intval($zenkiData["DEC"]) + $addDEC;
                    $zenkiData["ADD"] = intval($zenkiData["ADD"]) + $addADD;

                    //最終調整値
                    $decMax -= $zenkiData["DEC"];
                    $addMax -= $zenkiData["ADD"];
                    if ($val == "06" && $decMax > 0) {
                        $zenkiData["DEC"] += $decMax;
                    }
                    if ($val == "06" && $addMax > 0) {
                        $zenkiData["ADD"] += $addMax;
                    }

                    //セット
                    $setDecisionMoney["BASE"]   = $zenkiData["DEC"];
                    $setDecisionMoney["ADD"]    = $zenkiData["ADD"];

                    if ($val <= $checkMonth1) {
                        $setDecisionMoneyArray["BASE"][$model->decMonth["COUNTRY_DEC_MONTH1"]] += $setDecisionMoney["BASE"];
                        $setDecisionMoneyArray["ADD"][$model->decMonth["COUNTRY_DEC_MONTH1"]] += $setDecisionMoney["ADD"];
                        $setDecisionMoney["BASE"] = 0;
                        $setDecisionMoney["ADD"] = 0;
                    }
                    knjp721Query::updatePlan($db, $model, $slipNo, $val, $zenkiData, $planCnt, "ZENKI", $setDecisionMoney);
                }
            }
            //後期
            if ($model->field["PAID_FLG2"][$slipNo] != '1' && ($planCnt["KOUKI_FLG"] == "1" || $planCnt["KOUKI_ADD_FLG"] == "1")) {
                $dataDEC    = $koukiData["DEC"];
                $dataADD    = $koukiData["ADD"];
                $decMax     = $koukiData["DEC_MAX"];
                $addMax     = $koukiData["ADD_MAX"];
                $deffSumDEC = $deffSumADD = 0;
                foreach ($koukiMonth as $key => $val) {
                    //初期値セット
                    $koukiData["DEC"] = $dataDEC;
                    $koukiData["ADD"] = $dataADD;

                    //差分
                    $deffSumDEC += $koukiData["DEC"] - intval($koukiData["DEC"]);
                    $deffSumADD += $koukiData["ADD"] - intval($koukiData["ADD"]);

                    //端数加算値取得
                    $addDEC = $addADD = 0;
                    if (round($deffSumDEC, 3) >= 1) {
                        $addDEC++;
                        $deffSumDEC = 0;
                    }
                    if (round($deffSumADD, 3) >= 1) {
                        $addADD++;
                        $deffSumADD = 0;
                    }
                    $koukiData["DEC"] = intval($koukiData["DEC"]) + $addDEC;
                    $koukiData["ADD"] = intval($koukiData["ADD"]) + $addADD;

                    //最終調整値
                    $decMax -= $koukiData["DEC"];
                    $addMax -= $koukiData["ADD"];
                    if ($val == "03" && $decMax > 0) {
                        $koukiData["DEC"] += $decMax;
                    }
                    if ($val == "03" && $addMax > 0) {
                        $koukiData["ADD"] += $addMax;
                    }

                    //セット
                    $setDecisionMoney["BASE"]   = $koukiData["DEC"];
                    $setDecisionMoney["ADD"]    = $koukiData["ADD"];

                    $checkVal = $val < "04" ? $val + 12 : $val;
                    if ($checkVal <= $checkMonth2) {
                        $setDecisionMoneyArray["BASE"][$model->decMonth["COUNTRY_DEC_MONTH2"]] += $setDecisionMoney["BASE"];
                        $setDecisionMoneyArray["ADD"][$model->decMonth["COUNTRY_DEC_MONTH2"]] += $setDecisionMoney["ADD"];
                        $setDecisionMoney["BASE"] = 0;
                        $setDecisionMoney["ADD"] = 0;
                    }
                    knjp721Query::updatePlan($db, $model, $slipNo, $val, $koukiData, $planCnt, "KOUKI", $setDecisionMoney);
                }
            }
        }
        //確定月以前の確定金額を確定月に寄せる処理
        if ($model->field["PAID_FLG1"][$slipNo] != '1') {
            knjp721Query::updateCountryfDecPlan($db, $model, $slipNo, $model->decMonth["COUNTRY_DEC_MONTH1"], $setDecisionMoneyArray["BASE"][$model->decMonth["COUNTRY_DEC_MONTH1"]], "", "ZENKI");
            knjp721Query::updateCountryfDecPlan($db, $model, $slipNo, $model->decMonth["COUNTRY_DEC_MONTH1"], $setDecisionMoneyArray["ADD"][$model->decMonth["COUNTRY_DEC_MONTH1"]], "ADD_", "ZENKI");
        }
        if ($model->field["PAID_FLG2"][$slipNo] != '1') {
            knjp721Query::updateCountryfDecPlan($db, $model, $slipNo, $model->decMonth["COUNTRY_DEC_MONTH2"], $setDecisionMoneyArray["BASE"][$model->decMonth["COUNTRY_DEC_MONTH2"]], "", "KOUKI");
            knjp721Query::updateCountryfDecPlan($db, $model, $slipNo, $model->decMonth["COUNTRY_DEC_MONTH2"], $setDecisionMoneyArray["ADD"][$model->decMonth["COUNTRY_DEC_MONTH2"]], "ADD_", "KOUKI");
        }
    }

    public function prefUpdate($db, $model, $slipNo, $schregNo, $rowCnt)
    {
        /*****************/
        /* REDUCTION_DAT */
        /*****************/

        $query  = " SELECT * FROM";
        $query .= "     REDUCTION_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD                = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND         = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND YEAR                = '".CTRL_YEAR ."' ";
        $query .= "     AND REDUCTION_TARGET    = '1' ";
        $query .= "     AND SLIP_NO             = '{$slipNo}' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if (is_array($model->field["PREF_FLG_1"])) {
            $prefFlg1 = in_array($slipNo, $model->field["PREF_FLG_1"]) ? 1 : null;
        } else {
            $prefFlg1 = null;
        }
        if (is_array($model->field["PREF_FLG_2"])) {
            $prefFlg2 = in_array($slipNo, $model->field["PREF_FLG_2"]) ? 1 : null;
        } else {
            $prefFlg2 = null;
        }

        $data = array();
        $data["SCHREGNO"][TEXT]                 = $schregNo;
        $data["PREFECTURESCD"][TEXT]            = $model->slipnoPref[$slipNo];
        $data["GRADE"][TEXT]                    = $model->search["GRADE"];

        if ($model->field["PAID_FLG1"][$slipNo] != '1') {
            $data["RARE_CASE_CD_1"][TEXT]           = null;
            $data["SEQ_1"][NUMBER]                  = is_numeric($model->field["REDUCTION_SEQ_1"][$rowCnt]) ? $model->field["REDUCTION_SEQ_1"][$rowCnt] : null;
            if ($row["DEC_FLG_1"] != "1") {
                $data["RANK_1"][TEXT]                   = $model->field["PREF_RANK_1"][$rowCnt];
                $data["MONEY_1"][NUMBER]                = strlen($model->field["PREF_MONEY_1"][$rowCnt]) > 0 ? $model->field["PREF_MONEY_1"][$rowCnt] : null;
            }
            $data["DEC_FLG_1"][TEXT]                = $prefFlg1;
        }
        if ($model->field["PAID_FLG2"][$slipNo] != '1') {
            $data["RARE_CASE_CD_2"][TEXT]           = null;
            $data["SEQ_2"][NUMBER]                  = is_numeric($model->field["REDUCTION_SEQ_2"][$rowCnt]) ? $model->field["REDUCTION_SEQ_2"][$rowCnt] : null;
            if ($row["DEC_FLG_2"] != "1") {
                $data["RANK_2"][TEXT]                   = $model->field["PREF_RANK_2"][$rowCnt];
                $data["MONEY_2"][NUMBER]                = strlen($model->field["PREF_MONEY_2"][$rowCnt]) > 0 ? $model->field["PREF_MONEY_2"][$rowCnt]   : null;
            }
            $data["DEC_FLG_2"][TEXT]                = $prefFlg2;
        }

        $data["REMARK"][TEXT]                   = $model->field["REDUC_REMARK"][$rowCnt] ? $model->field["REDUC_REMARK"][$rowCnt] : null;
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "SYSDATE()";

        if (is_array($row)) {
            /**********/
            /* UPDATE */
            /**********/

            $where  = " WHERE ";
            $where .= "     SCHOOLCD                = '".(sprintf("%012d", SCHOOLCD))."' ";
            $where .= "     AND SCHOOL_KIND         = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
            $where .= "     AND YEAR                = '".CTRL_YEAR ."' ";
            $where .= "     AND REDUCTION_TARGET    = '1' ";
            $where .= "     AND SLIP_NO             = '{$slipNo}' ";

            $query = Query::updateSQL($data, "REDUCTION_DAT", $where);
            $result = $db->query($query);
        } else {

            /**********/
            /* INSERT */
            /**********/
            $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]      = $model->schoolKindAry[$model->search["GRADE"]];
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["REDUCTION_TARGET"][TEXT] = 1;
            $data["SLIP_NO"][TEXT]          = $slipNo;

            $query = Query::insertSQL($data, "REDUCTION_DAT");
            $result = $db->query($query);
        }

        /**********************/
        /* REDUCTION_PLAN_DAT */
        /**********************/

        //PLAN更新フラグ
        $prefUpdateFlg1 = ($prefFlg1 != $row["DEC_FLG_1"]) ? 1 : 0;
        $prefUpdateFlg2 = ($prefFlg2 != $row["DEC_FLG_2"]) ? 1 : 0;

        //計画データ削除(確定フラグ:OFF)
        if ($prefFlg1 != "1" && $prefFlg2 != "1" && $model->field["PAID_FLG1"][$slipNo] != '1' && $model->field["PAID_FLG2"][$slipNo] != '1') {
            $query = knjp721Query::planPreDecisionMoneyAllClear($model, $slipNo);
            $db->query($query);
        } else {
            //計画データ削除(確定フラグ:OFF)
            if ($prefUpdateFlg1 == "1" && $model->field["PAID_FLG1"][$slipNo] != '1') {
                $query = knjp721Query::planPreDecisionMoneyAllClear($model, $slipNo, 'zenki');
                $db->query($query);
            }
            if ($prefUpdateFlg2 == "1" && $model->field["PAID_FLG2"][$slipNo] != '1') {
                $query = knjp721Query::planPreDecisionMoneyAllClear($model, $slipNo, 'kouki');
                $db->query($query);
            }

            //計画データのある月取得
            $planMonthArray = $db->getCol(knjp721Query::getPrefPlanMonth($model, $slipNo));

            if (get_count($planMonthArray) != 12) {
                // INSERT
                for ($month = 1; $month <= 12; $month++) {
                    if (in_array($month, array_map('intval', $planMonthArray), true)) {
                        continue;
                    }

                    $setMonth = sprintf("%02d", $month);
                    $setMoney = $setDecisionMoney = 0;
                    ;
                    knjp721Query::insertPrefPlan($db, $model, $slipNo, $schregNo, $setMonth, $setMoney, $setDecisionMoney);
                }
            }

            $zenkiData = ($prefFlg1 == "1" && $model->field["PREF_MONEY_1"][$rowCnt] > 0) ? $model->field["PREF_MONEY_1"][$rowCnt] : 0;
            $koukiData = ($prefFlg2 == "1" && $model->field["PREF_MONEY_2"][$rowCnt] > 0) ? $model->field["PREF_MONEY_2"][$rowCnt] : 0;

            $setDecisionMoneyArray = array();
            $checkMonth1 = $model->decMonth["PREF_DEC_MONTH1"] < "04" ? $model->decMonth["PREF_DEC_MONTH1"] + 12 : $model->decMonth["PREF_DEC_MONTH1"];
            $checkMonth2 = $model->decMonth["PREF_DEC_MONTH2"] < "04" ? $model->decMonth["PREF_DEC_MONTH2"] + 12 : $model->decMonth["PREF_DEC_MONTH2"];

            // UPDATE
            $query  = " WITH MAIN AS ( ";
            $query .= "     SELECT ";
            $query .= "         SLIP_NO, ";
            $query .= "         PLAN_YEAR, ";
            $query .= "         PLAN_MONTH, ";
            $query .= "         PAID_LIMIT_MONTH_CALC, ";
            $query .= "         CASE WHEN PLAN_MONTH IN ('04', '05', '06') THEN '1' ELSE '2' END AS DIV, ";
            $query .= "         CASE WHEN PLAN_MONTH IN ('04', '05', '06') THEN {$zenkiData} ELSE {$koukiData} END AS MONEY ";
            $query .= "     FROM ";
            $query .= "         COLLECT_SLIP_PLAN_LIMITDATE_DAT LIMIT_DATE ";
            $query .= "     WHERE ";
            $query .= "         SCHOOLCD            = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "         AND SCHOOL_KIND     = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
            $query .= "         AND YEAR            = '".CTRL_YEAR."' ";
            $query .= "         AND SLIP_NO         = '{$slipNo}' ";
            $query .= "         AND SCHREGNO        = '{$schregNo}' ";
            $query .= "         AND PAID_LIMIT_DATE_CALC IS NOT NULL ";
            $query .= "         AND PAID_LIMIT_MONTH_CALC IS NOT NULL ";
            $query .= " ) ";

            $query .= " SELECT ";
            $query .= "     MAIN.SLIP_NO, ";
            $query .= "     MAIN.PLAN_YEAR, ";
            $query .= "     MAIN.PLAN_MONTH, ";
            $query .= "     CASE WHEN MAIN.MONEY > 0 THEN MAIN.MONEY / COUNT_P.CNT + (CASE WHEN RTRIM(CHAR(MAIN.PLAN_YEAR)) || MAIN.PLAN_MONTH = MAX_M.MAX_MONTH THEN MOD(MAIN.MONEY * COUNT_M.CNT / COUNT_P.CNT, COUNT_M.CNT) ELSE 0 END) ELSE 0 END AS PLAN_MONEY ";
            $query .= " FROM ";
            $query .= "     MAIN ";
            $query .= "     LEFT JOIN (SELECT ";
            $query .= "                     SLIP_NO, ";
            $query .= "                     DIV, ";
            $query .= "                     COUNT(*) AS CNT ";
            $query .= "                 FROM ";
            $query .= "                     MAIN ";
            $query .= "                 GROUP BY ";
            $query .= "                     SLIP_NO, ";
            $query .= "                     DIV ";
            $query .= "                 ) COUNT_P ON MAIN.SLIP_NO = COUNT_P.SLIP_NO AND MAIN.DIV = COUNT_P.DIV ";
            $query .= "     LEFT JOIN (SELECT ";
            $query .= "                     SLIP_NO, ";
            $query .= "                     DIV, ";
            $query .= "                     PAID_LIMIT_MONTH_CALC, ";
            $query .= "                     COUNT(*) AS CNT ";
            $query .= "                 FROM ";
            $query .= "                     MAIN ";
            $query .= "                 GROUP BY ";
            $query .= "                     SLIP_NO, ";
            $query .= "                     DIV, ";
            $query .= "                     PAID_LIMIT_MONTH_CALC ";
            $query .= "                 ) COUNT_M ON MAIN.SLIP_NO = COUNT_M.SLIP_NO AND MAIN.DIV = COUNT_M.DIV AND MAIN.PAID_LIMIT_MONTH_CALC = COUNT_M.PAID_LIMIT_MONTH_CALC ";
            $query .= "     LEFT JOIN (SELECT ";
            $query .= "                     SLIP_NO, ";
            $query .= "                     DIV, ";
            $query .= "                     PAID_LIMIT_MONTH_CALC, ";
            $query .= "                     MAX(RTRIM(CHAR(PLAN_YEAR)) || PLAN_MONTH) AS MAX_MONTH ";
            $query .= "                 FROM ";
            $query .= "                     MAIN ";
            $query .= "                 GROUP BY ";
            $query .= "                     SLIP_NO, ";
            $query .= "                     DIV, ";
            $query .= "                     PAID_LIMIT_MONTH_CALC ";
            $query .= "                 ) MAX_M ON MAIN.SLIP_NO = MAX_M.SLIP_NO AND MAIN.DIV = MAX_M.DIV AND MAIN.PAID_LIMIT_MONTH_CALC = MAX_M.PAID_LIMIT_MONTH_CALC ";
            $query .= " ORDER BY ";
            $query .= "     MAIN.PLAN_YEAR, ";
            $query .= "     MAIN.PLAN_MONTH ";

            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($model->field["PAID_FLG1"][$slipNo] == '1' && in_array($row["PLAN_MONTH"], array('04', '05', '06'))) {
                    continue;
                }
                if ($model->field["PAID_FLG2"][$slipNo] == '1' && !in_array($row["PLAN_MONTH"], array('04', '05', '06'))) {
                    continue;
                }

                $planMonth = $row["PLAN_MONTH"] < "04" ? $row["PLAN_MONTH"] + 12 : $row["PLAN_MONTH"];
                $setDecMoney = $row["PLAN_MONEY"];
                if (in_array($row["PLAN_MONTH"], array("04", "05", "06"))) {
                    $div = "ZENKI";
                    if ($planMonth <= $checkMonth1) {
                        $setDecisionMoneyArray[$model->decMonth["PREF_DEC_MONTH1"]] += $row["PLAN_MONEY"];
                        $setDecMoney = 0;
                    }
                } else {
                    $div = "KOUKI";
                    if ($planMonth <= $checkMonth2) {
                        $setDecisionMoneyArray[$model->decMonth["PREF_DEC_MONTH2"]] += $row["PLAN_MONEY"];
                        $setDecMoney = 0;
                    }
                }

                $data = array();
                $data["PLAN_MONEY"][NUMBER]         = $row["PLAN_MONEY"];
                $data["DECISION_MONEY"][NUMBER]     = $setDecMoney;
                if ($div == "ZENKI") {
                    $data["DECISION_MONEY1"][NUMBER]   = $setDecMoney;
                }
                if ($div == "KOUKI") {
                    $data["DECISION_MONEY2"][NUMBER]   = $setDecMoney;
                }
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = "SYSDATE()";

                $where  = " WHERE ";
                $where .= "     SCHOOLCD                = '".sprintf("%012d", SCHOOLCD)."' ";
                $where .= "     AND SCHOOL_KIND         = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
                $where .= "     AND YEAR                = '".CTRL_YEAR."' ";
                $where .= "     AND REDUCTION_TARGET    = '1' ";
                $where .= "     AND SLIP_NO             = '{$slipNo}' ";
                $where .= "     AND SCHREGNO            = '{$schregNo}' ";
                $where .= "     AND PLAN_YEAR           = '{$row["PLAN_YEAR"]}' ";
                $where .= "     AND PLAN_MONTH          = '{$row["PLAN_MONTH"]}' ";

                $query = Query::updateSQL($data, "REDUCTION_PLAN_DAT", $where);
                $db->query($query);
            }
            $result->free();
        }

        //確定月以前の確定金額を確定月に寄せる処理
        if ($model->field["PAID_FLG1"][$slipNo] != '1') {
            knjp721Query::updatePrefDecPlan($db, $model, $slipNo, $model->decMonth["PREF_DEC_MONTH1"], $setDecisionMoneyArray[$model->decMonth["PREF_DEC_MONTH1"]], "ZENKI");
        }
        if ($model->field["PAID_FLG2"][$slipNo] != '1') {
            knjp721Query::updatePrefDecPlan($db, $model, $slipNo, $model->decMonth["PREF_DEC_MONTH2"], $setDecisionMoneyArray[$model->decMonth["PREF_DEC_MONTH2"]], "KOUKI");
        }

        return array($prefFlg1, $prefFlg2, $prefUpdateFlg1, $prefUpdateFlg2);
    }

    public function reductionSchoolUpdate($db, $model, $slipNo, $schregNo, $rowCnt)
    {
        $reducSchoolFlg = array();
        for ($soeji = 1; $soeji <= 2; $soeji++) {
            if (is_array($model->field["REDUC_SCHOOL_FLG_".$soeji])) {
                $reducSchoolFlg[$soeji] = in_array($slipNo, $model->field["REDUC_SCHOOL_FLG_".$soeji]) ? 1 : null;
            } else {
                $reducSchoolFlg[$soeji] = null;
            }
        }

        knjp721Query::setReductionSchoolDat($db, $model, $slipNo, $schregNo, $model->slipSchoolStd[$slipNo], $rowCnt, $reducSchoolFlg);
    }

    public function setReductionSchoolDat($db, $model, $slipNo, $schregNo, $slipSchoolStd, $rowCnt, $reducSchoolFlg)
    {
        //データ取得 -- REDUCTION_SCHOOL_DAT
        $query = knjp721Query::getReductionSchoolDat($model, $slipNo);
        $rowReduc = $db->getRow($query, DB_FETCHMODE_ASSOC);

        /************************/
        /* REDUCTION_SCHOOL_DAT */
        /************************/
        $data = array();
        $data["SCHREGNO"][TEXT]                 = $schregNo;

        if ($model->field["PAID_FLG1"][$slipNo] != '1') {
            $data["DEC_FLG_1"][TEXT]                = $reducSchoolFlg[1];
            if ($rowReduc["DEC_FLG_1"] != "1") {
                $data["MONEY_1"][NUMBER]            = strlen($slipSchoolStd["REDUCTION_JUGYOURYOU1"]) > 0 ? $slipSchoolStd["REDUCTION_JUGYOURYOU1"] : null;
            }
        }
        if ($model->field["PAID_FLG2"][$slipNo] != '1') {
            $data["DEC_FLG_2"][TEXT]                = $reducSchoolFlg[2];
            if ($rowReduc["DEC_FLG_2"] != "1") {
                $data["MONEY_2"][NUMBER]            = strlen($slipSchoolStd["REDUCTION_JUGYOURYOU2"]) > 0 ? $slipSchoolStd["REDUCTION_JUGYOURYOU2"] : null;
            }
        }

        $data["REMARK"][TEXT]                   = $model->field["REDUC_REMARK"][$rowCnt] ? $model->field["REDUC_REMARK"][$rowCnt] : null;
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "SYSDATE()";

        if (is_array($rowReduc)) {
            /**********/
            /* UPDATE */
            /**********/
            $where  = " WHERE ";
            $where .= "     SCHOOLCD                = '".(sprintf("%012d", SCHOOLCD))."' ";
            $where .= "     AND SCHOOL_KIND         = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
            $where .= "     AND YEAR                = '".CTRL_YEAR ."' ";
            $where .= "     AND SLIP_NO             = '{$slipNo}' ";
            $where .= "     AND REDUCTION_TARGET    = '1' ";

            $query = Query::updateSQL($data, "REDUCTION_SCHOOL_DAT", $where);
            $result = $db->query($query);
        } else {
            /**********/
            /* INSERT */
            /**********/
            $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]      = $model->schoolKindAry[$model->search["GRADE"]];
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SLIP_NO"][TEXT]          = $slipNo;
            $data["REDUCTION_TARGET"][TEXT] = 1;

            $query = Query::insertSQL($data, "REDUCTION_SCHOOL_DAT");
            $result = $db->query($query);
        }

        /*****************************/
        /* REDUCTION_SCHOOL_PLAN_DAT */
        /*****************************/
        //PLAN更新フラグ
        $reducSchoolDecFlg1 = ($reducSchoolFlg[1] != $rowReduc["DEC_FLG_1"]) ? 1 : 0;
        $reducSchoolDecFlg2 = ($reducSchoolFlg[2] != $rowReduc["DEC_FLG_2"]) ? 1 : 0;

        $delMonth = $updMonth = array();
        for ($month = 1; $month <= 12; $month++) {
            if ($reducSchoolFlg[1] != "1" && $model->field["PAID_FLG1"][$slipNo] != '1' && in_array($month, array(4, 5, 6))) {
                $delMonth[] = sprintf("%02d", $month);
            }
            if ($reducSchoolFlg[2] != "1" && $model->field["PAID_FLG2"][$slipNo] != '1' && !in_array($month, array(4, 5, 6))) {
                $delMonth[] = sprintf("%02d", $month);
            }

            if ($reducSchoolFlg[1] == "1" && $reducSchoolDecFlg1 == "1" && $model->field["PAID_FLG1"][$slipNo] != '1' && in_array($month, array(4, 5, 6))) {
                $updMonth[] = sprintf("%02d", $month);
            }
            if ($reducSchoolFlg[2] == "1" && $reducSchoolDecFlg2 == "1" && $model->field["PAID_FLG2"][$slipNo] != '1' && !in_array($month, array(4, 5, 6))) {
                $updMonth[] = sprintf("%02d", $month);
            }
        }

        if ($reducSchoolFlg[1] != "1" || $reducSchoolFlg[2] != "1") {
            $query  = " DELETE FROM ";
            $query .= "     REDUCTION_SCHOOL_PLAN_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHOOLCD            = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "     AND SCHOOL_KIND     = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
            $query .= "     AND YEAR            = '".CTRL_YEAR ."' ";
            $query .= "     AND SLIP_NO         = '{$slipNo}' ";
            $query .= "     AND REDUCTION_TARGET = '1' ";
            $query .= "     AND PLAN_MONTH IN ('" .implode($delMonth, "','") ."') ";
            $db->query($query);
        }

        //計画データのある月取得
        $planMonthArray = $db->getCol(knjp721Query::getReducSchoolPlanMonth($model, $slipNo));

        if (get_count($planMonthArray) != 12) {
            // INSERT
            for ($month = 1; $month <= 12; $month++) {
                if (in_array($month, array_map('intval', $planMonthArray), true)) {
                    continue;
                }
                $setMoney = $setDecisionMoney = 0;
                $setMonth = sprintf("%02d", $month);
                knjp721Query::insertReducSchoolPlan($db, $model, $slipNo, $schregNo, $setMonth, $setMoney, $setDecisionMoney);
            }
        }

        if ($reducSchoolFlg[1] == "1" || $reducSchoolFlg[2] == "1") {
            $money1 = ($reducSchoolFlg[1] == "1" && $slipSchoolStd["REDUCTION_JUGYOURYOU1"] > 0) ? $slipSchoolStd["REDUCTION_JUGYOURYOU1"] : 0;
            $money2 = ($reducSchoolFlg[2] == "1" && $slipSchoolStd["REDUCTION_JUGYOURYOU2"] > 0) ? $slipSchoolStd["REDUCTION_JUGYOURYOU2"] : 0;

            // UPDATE
            $query  = " WITH MAIN AS ( ";
            $query .= "     SELECT ";
            $query .= "         SLIP_NO, ";
            $query .= "         PLAN_YEAR, ";
            $query .= "         PLAN_MONTH, ";
            $query .= "         PAID_LIMIT_MONTH_CALC, ";
            $query .= "         CASE WHEN PLAN_MONTH IN ('04', '05', '06') THEN '1' ELSE '2' END AS DIV, ";
            $query .= "         CASE WHEN PLAN_MONTH IN ('04', '05', '06') THEN {$money1} ELSE {$money2} END AS MONEY ";
            $query .= "     FROM ";
            $query .= "         COLLECT_SLIP_PLAN_LIMITDATE_DAT LIMIT_DATE ";
            $query .= "     WHERE ";
            $query .= "         SCHOOLCD                = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "         AND SCHOOL_KIND         = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
            $query .= "         AND YEAR                = '".CTRL_YEAR."' ";
            $query .= "         AND SLIP_NO             = '{$slipNo}' ";
            $query .= "         AND SCHREGNO            = '{$schregNo}' ";
            $query .= "         AND PAID_LIMIT_DATE_CALC IS NOT NULL ";
            $query .= "         AND PAID_LIMIT_MONTH_CALC IS NOT NULL ";
            $query .= " ) ";

            $query .= " SELECT ";
            $query .= "     MAIN.SLIP_NO, ";
            $query .= "     MAIN.PLAN_YEAR, ";
            $query .= "     MAIN.PLAN_MONTH, ";
            $query .= "     CASE WHEN MAIN.MONEY > 0 THEN MAIN.MONEY / COUNT_T.CNT + (CASE WHEN RTRIM(CHAR(MAIN.PLAN_YEAR)) || MAIN.PLAN_MONTH = MAX_M.MAX_MONTH THEN MOD(MAIN.MONEY * COUNT_M.CNT / COUNT_T.CNT, COUNT_M.CNT) ELSE 0 END) ELSE 0 END AS PLAN_MONEY, ";
            $query .= "     CASE WHEN PLAN_MONTH IN ('04','05','06') AND MAIN.MONEY > 0 THEN MAIN.MONEY / COUNT_T.CNT + (CASE WHEN RTRIM(CHAR(MAIN.PLAN_YEAR)) || MAIN.PLAN_MONTH = MAX_M.MAX_MONTH THEN MOD(MAIN.MONEY * COUNT_M.CNT / COUNT_T.CNT, COUNT_M.CNT) ELSE 0 END) ELSE 0 END AS DECISION_MONEY1, ";
            $query .= "     CASE WHEN PLAN_MONTH NOT IN ('04','05','06') AND MAIN.MONEY > 0 THEN MAIN.MONEY / COUNT_T.CNT + (CASE WHEN RTRIM(CHAR(MAIN.PLAN_YEAR)) || MAIN.PLAN_MONTH = MAX_M.MAX_MONTH THEN MOD(MAIN.MONEY * COUNT_M.CNT / COUNT_T.CNT, COUNT_M.CNT) ELSE 0 END) ELSE 0 END AS DECISION_MONEY2 ";
            $query .= " FROM ";
            $query .= "     MAIN ";
            $query .= "     LEFT JOIN (SELECT SLIP_NO, DIV, COUNT(*) AS CNT FROM MAIN GROUP BY SLIP_NO, DIV) COUNT_T ON MAIN.SLIP_NO = COUNT_T.SLIP_NO AND MAIN.DIV = COUNT_T.DIV ";
            $query .= "     LEFT JOIN (SELECT SLIP_NO, DIV, PAID_LIMIT_MONTH_CALC, COUNT(*) AS CNT FROM MAIN GROUP BY SLIP_NO, DIV, PAID_LIMIT_MONTH_CALC) COUNT_M ON MAIN.SLIP_NO = COUNT_M.SLIP_NO AND MAIN.DIV = COUNT_M.DIV AND MAIN.PAID_LIMIT_MONTH_CALC = COUNT_M.PAID_LIMIT_MONTH_CALC ";
            $query .= "     LEFT JOIN (SELECT SLIP_NO, DIV, PAID_LIMIT_MONTH_CALC, MAX(RTRIM(CHAR(PLAN_YEAR)) || PLAN_MONTH) AS MAX_MONTH FROM MAIN GROUP BY SLIP_NO, DIV, PAID_LIMIT_MONTH_CALC) MAX_M ON MAIN.SLIP_NO = MAX_M.SLIP_NO AND MAIN.DIV = MAX_M.DIV AND MAIN.PAID_LIMIT_MONTH_CALC = MAX_M.PAID_LIMIT_MONTH_CALC ";
            $query .= " ORDER BY ";
            $query .= "     MAIN.PLAN_YEAR, ";
            $query .= "     MAIN.PLAN_MONTH ";

            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if (!in_array($row["PLAN_MONTH"], $updMonth)) {
                    continue;
                }

                if ($model->field["PAID_FLG1"][$slipNo] == '1' && in_array($row["PLAN_MONTH"], array('04', '05', '06'))) {
                    continue;
                }
                if ($model->field["PAID_FLG2"][$slipNo] == '1' && !in_array($row["PLAN_MONTH"], array('04', '05', '06'))) {
                    continue;
                }

                $data = array();
                $data["PLAN_MONEY"][NUMBER]         = $row["PLAN_MONEY"];
                $data["DECISION_MONEY"][NUMBER]     = $row["DECISION_MONEY1"] + $row["DECISION_MONEY2"];
                $data["DECISION_MONEY1"][NUMBER]    = $row["DECISION_MONEY1"];
                $data["DECISION_MONEY2"][NUMBER]    = $row["DECISION_MONEY2"];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = "SYSDATE()";

                $where  = " WHERE ";
                $where .= "     SCHOOLCD                = '".sprintf("%012d", SCHOOLCD)."' ";
                $where .= "     AND SCHOOL_KIND         = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
                $where .= "     AND YEAR                = '".CTRL_YEAR."' ";
                $where .= "     AND SLIP_NO             = '{$slipNo}' ";
                $where .= "     AND REDUCTION_TARGET    = '1' ";
                $where .= "     AND SCHREGNO            = '{$schregNo}' ";
                $where .= "     AND PLAN_YEAR           = '{$row["PLAN_YEAR"]}' ";
                $where .= "     AND PLAN_MONTH          = '{$row["PLAN_MONTH"]}' ";

                $query = Query::updateSQL($data, "REDUCTION_SCHOOL_PLAN_DAT", $where);
                $db->query($query);
            }
            $result->free();
        }
    }

    public function getReductionSchoolDat($model, $slipNo)
    {
        $query  = " SELECT * FROM";
        $query .= "     REDUCTION_SCHOOL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD                = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND         = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND YEAR                = '".CTRL_YEAR ."' ";
        $query .= "     AND SLIP_NO             = '{$slipNo}' ";
        $query .= "     AND REDUCTION_TARGET    = '1' ";

        return $query;
    }

    public function delReductionDat($schregNo)
    {
        $query  = " DELETE FROM REDUCTION_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR                = '".CTRL_YEAR ."' AND ";
        $query .= "     SCHREGNO            = '{$schregNo}' AND ";
        $query .= "     REDUCTION_TARGET    = '1' ";

        return $query;
    }

    public function planMoneyClear($model, $slipNo, $fieldName, $monthArray)
    {
        $data = array();

        $data[$fieldName."PLAN_MONEY"][NUMBER]      = 0;
        $data["REGISTERCD"][TEXT]                   = STAFFCD;
        $data["UPDATED"][FUNC]                      = "SYSDATE()";

        $instate = "(";
        $sep = "";
        foreach ($monthArray as $key => $month) {
            $setYear = $month < "04" ? CTRL_YEAR + 1 : CTRL_YEAR;
            $instate .= $sep."'{$setYear}{$month}'";
            $sep = ",";
        }
        $instate .= ")";
        $where  = " WHERE ";
        $where .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $where .= "     AND YEAR        = '".CTRL_YEAR ."' ";
        $where .= "     AND SLIP_NO     = '{$slipNo}' ";
        $where .= "     AND PLAN_YEAR || PLAN_MONTH IN {$instate} ";

        $query = Query::updateSQL($data, "REDUCTION_COUNTRY_PLAN_DAT", $where);

        return $query;
    }

    public function planDecisionMoneyAllClear($model, $slipNo)
    {
        $data = array();

        $data["PLAN_MONEY"][NUMBER]             = 0;
        $data["ADD_PLAN_MONEY"][NUMBER]         = 0;
        $data["DECISION_MONEY"][NUMBER]         = 0;
        $data["DECISION_MONEY1"][NUMBER]        = 0;
        $data["DECISION_MONEY2"][NUMBER]        = 0;
        $data["ADD_DECISION_MONEY"][NUMBER]     = 0;
        $data["ADD_DECISION_MONEY1"][NUMBER]    = 0;
        $data["ADD_DECISION_MONEY2"][NUMBER]    = 0;
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $where .= "     AND YEAR        = '".CTRL_YEAR ."' ";
        $where .= "     AND SLIP_NO     = '{$slipNo}' ";

        $query = Query::updateSQL($data, "REDUCTION_COUNTRY_PLAN_DAT", $where);
        return $query;
    }

    public function planDecisionMoneyClear($model, $slipNo, $fieldName, $monthArray, $div = "")
    {
        $data = array();

        $data[$fieldName."DECISION_MONEY".$div][NUMBER]  = 0;
        $data["REGISTERCD"][TEXT]                   = STAFFCD;
        $data["UPDATED"][FUNC]                      = "SYSDATE()";

        $instate = "(";
        $sep = "";
        foreach ($monthArray as $key => $month) {
            $setYear = $month < "04" ? CTRL_YEAR + 1 : CTRL_YEAR;
            $instate .= $sep."'{$setYear}{$month}'";
            $sep = ",";
        }
        $instate .= ")";
        $where  = " WHERE ";
        $where .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $where .= "     AND YEAR        = '".CTRL_YEAR ."' ";
        $where .= "     AND SLIP_NO     = '{$slipNo}' ";
        if (!$div) {
            $where .= "     AND PLAN_YEAR || PLAN_MONTH IN {$instate} ";
        }

        $query = Query::updateSQL($data, "REDUCTION_COUNTRY_PLAN_DAT", $where);
        return $query;
    }

    public function planPreDecisionMoneyAllClear($model, $slipNo, $flg = "")
    {
        $data = array();

        $data["PLAN_MONEY"][NUMBER]             = 0;
        $data["DECISION_MONEY"][NUMBER]         = 0;
        $data["DECISION_MONEY1"][NUMBER]        = 0;
        $data["DECISION_MONEY2"][NUMBER]        = 0;
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $where .= "     AND YEAR        = '".CTRL_YEAR ."' ";
        $where .= "     AND REDUCTION_TARGET    = '1' ";
        $where .= "     AND SLIP_NO     = '{$slipNo}' ";
        if ($flg == 'zenki' || $flg == 'kouki') {
            if ($flg == 'zenki') {
                $monthArray = array("04", "05", "06");
            }
            if ($flg == 'kouki') {
                $monthArray = array("07", "08", "09", "10", "11", "12", "01", "02", "03");
            }
            $instate = "(";
            $sep = "";
            foreach ($monthArray as $key => $month) {
                $setYear = $month < "04" ? CTRL_YEAR + 1 : CTRL_YEAR;
                $instate .= $sep."'{$setYear}{$month}'";
                $sep = ",";
            }
            $instate .= ")";
            $where .= "     AND PLAN_YEAR || PLAN_MONTH IN {$instate} ";
        }

        $query = Query::updateSQL($data, "REDUCTION_PLAN_DAT", $where);
        return $query;
    }

    public function burdenChargePlanMoneyAllClear($model, $slipNo, $flg = "")
    {
        $data = array();

        $data["BURDEN_CHARGE"][NUMBER]      = 0;
        $data["BURDEN_CHARGE1"][NUMBER]     = 0;
        $data["BURDEN_CHARGE2"][NUMBER]     = 0;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "     SCHOOLCD                = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "     AND SCHOOL_KIND         = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $where .= "     AND YEAR                = '".CTRL_YEAR ."' ";
        $where .= "     AND REDUCTION_TARGET    = '1' ";
        $where .= "     AND SLIP_NO             = '{$slipNo}' ";
        if ($flg == 'zenki' || $flg == 'kouki') {
            if ($flg == 'zenki') {
                $monthArray = array("04", "05", "06");
            }
            if ($flg == 'kouki') {
                $monthArray = array("07", "08", "09", "10", "11", "12", "01", "02", "03");
            }
            $instate = "(";
            $sep = "";
            foreach ($monthArray as $key => $month) {
                $setYear = $month < "04" ? CTRL_YEAR + 1 : CTRL_YEAR;
                $instate .= $sep."'{$setYear}{$month}'";
                $sep = ",";
            }
            $instate .= ")";
            $where .= "     AND PLAN_YEAR || PLAN_MONTH IN {$instate} ";
        }

        $query = Query::updateSQL($data, "REDUCTION_BURDEN_CHARGE_PLAN_DAT", $where);
        return $query;
    }

    public function insertPlan($db, $model, $slipNo, $schregNo, $month, $moneyData, $setDecisionMoney)
    {
        $data = array();

        $data["SCHOOLCD"][TEXT]                 = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]              = $model->schoolKindAry[$model->search["GRADE"]];
        $data["YEAR"][TEXT]                     = CTRL_YEAR;
        $data["SLIP_NO"][TEXT]                  = $slipNo;
        $data["SCHREGNO"][TEXT]                 = $schregNo;
        $data["PLAN_YEAR"][TEXT]                = $month < "04" ? CTRL_YEAR + 1 : CTRL_YEAR;
        $data["PLAN_MONTH"][TEXT]               = $month;
        $data["PLAN_MONEY"][NUMBER]             = $moneyData["DEC"];
        $data["ADD_PLAN_MONEY"][NUMBER]         = $moneyData["ADD"];
        $data["DECISION_MONEY"][NUMBER]         = $setDecisionMoney["BASE"];
        $data["DECISION_MONEY1"][NUMBER]        = in_array($month, array('04','05','06')) ? $setDecisionMoney["BASE"] : 0;
        $data["DECISION_MONEY2"][NUMBER]        = in_array($month, array('04','05','06')) ? 0 : $setDecisionMoney["BASE"];
        $data["ADD_DECISION_MONEY"][NUMBER]     = $setDecisionMoney["ADD"];
        $data["ADD_DECISION_MONEY1"][NUMBER]    = in_array($month, array('04','05','06')) ? $setDecisionMoney["ADD"] : 0;
        $data["ADD_DECISION_MONEY2"][NUMBER]    = in_array($month, array('04','05','06')) ? 0 : $setDecisionMoney["ADD"];
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "SYSDATE()";

        $query = Query::insertSQL($data, "REDUCTION_COUNTRY_PLAN_DAT");
        $db->query($query);
    }

    public function updatePlan($db, $model, $slipNo, $month, $moneyData, $planCnt, $div, $setDecisionMoney)
    {
        $data = array();

        if ($planCnt[$div."_FLG"] == "1") {
            $data["PLAN_MONEY"][NUMBER]         = $moneyData["DEC"];
            $data["DECISION_MONEY"][NUMBER]     = $setDecisionMoney["BASE"];
            if ($div == 'ZENKI') {
                $data["DECISION_MONEY1"][NUMBER]       = $setDecisionMoney["BASE"];
            }
            if ($div == 'KOUKI') {
                $data["DECISION_MONEY2"][NUMBER]       = $setDecisionMoney["BASE"];
            }
        }
        if ($planCnt[$div."_ADD_FLG"] == "1") {
            $data["ADD_PLAN_MONEY"][NUMBER]     = $moneyData["ADD"];
            $data["ADD_DECISION_MONEY"][NUMBER] = $setDecisionMoney["ADD"];
            if ($div == 'ZENKI') {
                $data["ADD_DECISION_MONEY1"][NUMBER]   = $setDecisionMoney["ADD"];
            }
            if ($div == 'KOUKI') {
                $data["ADD_DECISION_MONEY2"][NUMBER]   = $setDecisionMoney["ADD"];
            }
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $setYear = $month < "04" ? CTRL_YEAR + 1 : CTRL_YEAR;
        $where  = " WHERE ";
        $where .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $where .= "     AND YEAR        = '".CTRL_YEAR ."' ";
        $where .= "     AND SLIP_NO     = '{$slipNo}' ";
        $where .= "     AND PLAN_YEAR   = '{$setYear}' ";
        $where .= "     AND PLAN_MONTH  = '{$month}' ";

        $query = Query::updateSQL($data, "REDUCTION_COUNTRY_PLAN_DAT", $where);
        $db->query($query);
    }

    public function updateCountryfDecPlan($db, $model, $slipNo, $month, $setDecisionMoney, $setDiv, $div)
    {
        if (!$setDecisionMoney) {
            return;
        }
        $data = array();

        $data[$setDiv."DECISION_MONEY"][NUMBER] = $setDecisionMoney;
        if ($div == 'ZENKI') {
            $data[$setDiv."DECISION_MONEY1"][NUMBER] = $setDecisionMoney;
        }
        if ($div == 'KOUKI') {
            $data[$setDiv."DECISION_MONEY2"][NUMBER] = $setDecisionMoney;
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $setYear = $month < "04" ? CTRL_YEAR + 1 : CTRL_YEAR;
        $where  = " WHERE ";
        $where .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $where .= "     AND YEAR        = '".CTRL_YEAR ."' ";
        $where .= "     AND SLIP_NO     = '{$slipNo}' ";
        $where .= "     AND PLAN_YEAR   = '{$setYear}' ";
        $where .= "     AND PLAN_MONTH  = '{$month}' ";

        $query = Query::updateSQL($data, "REDUCTION_COUNTRY_PLAN_DAT", $where);
        $db->query($query);
    }

    public function insertPrefPlan($db, $model, $slipNo, $schregNo, $month, $setMoney, $setDecisionMoney)
    {
        $data = array();

        $data["SCHOOLCD"][TEXT]             = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]          = $model->schoolKindAry[$model->search["GRADE"]];
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["REDUCTION_TARGET"][TEXT]     = 1;
        $data["SLIP_NO"][TEXT]              = $slipNo;
        $data["PLAN_YEAR"][TEXT]            = $month < "04" ? CTRL_YEAR + 1 : CTRL_YEAR;
        $data["PLAN_MONTH"][TEXT]           = $month;
        $data["SCHREGNO"][TEXT]             = $schregNo;
        $data["PLAN_MONEY"][NUMBER]         = $setMoney;
        $data["DECISION_MONEY"][NUMBER]     = $setDecisionMoney;
        $data["DECISION_MONEY1"][NUMBER]    = $setDecisionMoney;
        $data["DECISION_MONEY2"][NUMBER]    = $setDecisionMoney;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $query = Query::insertSQL($data, "REDUCTION_PLAN_DAT");
        $db->query($query);
    }

    public function updatePrefPlan($db, $model, $slipNo, $month, $setMoney, $planCnt, $div, $setDecisionMoney)
    {
        $data = array();

        if ($planCnt[$div."_FLG"] == "1") {
            $data["PLAN_MONEY"][NUMBER]     = $setMoney;
            $data["DECISION_MONEY"][NUMBER] = $setDecisionMoney;
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $setYear = $month < "04" ? CTRL_YEAR + 1 : CTRL_YEAR;
        $where  = " WHERE ";
        $where .= "     SCHOOLCD                = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "     AND SCHOOL_KIND         = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $where .= "     AND YEAR                = '".CTRL_YEAR ."' ";
        $where .= "     AND REDUCTION_TARGET    = '1' ";
        $where .= "     AND SLIP_NO             = '{$slipNo}' ";
        $where .= "     AND PLAN_YEAR           = '{$setYear}' ";
        $where .= "     AND PLAN_MONTH          = '{$month}' ";

        $query = Query::updateSQL($data, "REDUCTION_PLAN_DAT", $where);
        $db->query($query);
    }

    public function updatePrefDecPlan($db, $model, $slipNo, $month, $setDecisionMoney, $div)
    {
        if (!$setDecisionMoney) {
            return;
        }
        $data = array();

        $data["DECISION_MONEY"][NUMBER]     = $setDecisionMoney;
        if ($div == 'ZENKI') {
            $data["DECISION_MONEY1"][NUMBER]   = $setDecisionMoney;
        }
        if ($div == 'KOUKI') {
            $data["DECISION_MONEY2"][NUMBER]   = $setDecisionMoney;
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $setYear = $month < "04" ? CTRL_YEAR + 1 : CTRL_YEAR;
        $where  = " WHERE ";
        $where .= "     SCHOOLCD                = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "     AND SCHOOL_KIND         = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $where .= "     AND YEAR                = '".CTRL_YEAR ."' ";
        $where .= "     AND REDUCTION_TARGET    = '1' ";
        $where .= "     AND SLIP_NO             = '{$slipNo}' ";
        $where .= "     AND PLAN_YEAR           = '{$setYear}' ";
        $where .= "     AND PLAN_MONTH          = '{$month}' ";

        $query = Query::updateSQL($data, "REDUCTION_PLAN_DAT", $where);
        $db->query($query);
    }

    public function insertReducSchoolPlan($db, $model, $slipNo, $schregNo, $month, $setMoney, $setDecisionMoney)
    {
        $data = array();

        $data["SCHOOLCD"][TEXT]             = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]          = $model->schoolKindAry[$model->search["GRADE"]];
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["SLIP_NO"][TEXT]              = $slipNo;
        $data["REDUCTION_TARGET"][TEXT]     = 1;
        $data["PLAN_YEAR"][TEXT]            = $month < "04" ? CTRL_YEAR + 1 : CTRL_YEAR;
        $data["PLAN_MONTH"][TEXT]           = $month;
        $data["SCHREGNO"][TEXT]             = $schregNo;
        $data["PLAN_MONEY"][NUMBER]         = $setMoney;
        $data["DECISION_MONEY"][NUMBER]     = $setDecisionMoney;
        $data["DECISION_MONEY1"][NUMBER]    = $setDecisionMoney;
        $data["DECISION_MONEY2"][NUMBER]    = $setDecisionMoney;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $query = Query::insertSQL($data, "REDUCTION_SCHOOL_PLAN_DAT");
        $db->query($query);
    }

    public function updateReducSchoolPlan($db, $model, $slipNo, $month, $setMoney, $setDecisionMoney)
    {
        $data = array();

        $data["PLAN_MONEY"][NUMBER]     = $setMoney;
        $data["DECISION_MONEY"][NUMBER] = $setDecisionMoney;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $setYear = $month < "04" ? CTRL_YEAR + 1 : CTRL_YEAR;
        $where  = " WHERE ";
        $where .= "     SCHOOLCD                = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "     AND SCHOOL_KIND         = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $where .= "     AND YEAR                = '".CTRL_YEAR ."' ";
        $where .= "     AND SLIP_NO             = '{$slipNo}' ";
        $where .= "     AND REDUCTION_TARGET    = '1' ";
        $where .= "     AND PLAN_YEAR           = '{$setYear}' ";
        $where .= "     AND PLAN_MONTH          = '{$month}' ";

        $query = Query::updateSQL($data, "REDUCTION_SCHOOL_PLAN_DAT", $where);
        $db->query($query);
    }

    public function updateReducBurdenCharge($db, $model, $slipNo, $schregNo, $rowCnt, $prefFlg)
    {
        $query  = " SELECT * FROM ";
        $query .= "     REDUCTION_SLIP_BURDEN_CHARGE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD            = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND     = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND YEAR            = '".CTRL_YEAR ."' ";
        $query .= "     AND REDUCTION_TARGET = '1' ";
        $query .= "     AND SLIP_NO         = '{$slipNo}' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $setBurdenCharge1 = ($row["BURDEN_CHARGE1"]) ? $row["BURDEN_CHARGE1"] : 0;
        $setBurdenCharge2 = ($row["BURDEN_CHARGE2"]) ? $row["BURDEN_CHARGE2"] : 0;

        /************************************/
        /* REDUCTION_SLIP_BURDEN_CHARGE_DAT */
        /************************************/

        $data = array();
        $data["SCHREGNO"][TEXT]                 = $schregNo;
        if ($model->field["PAID_FLG1"][$slipNo] != '1') {
            $data["BURDEN_CHARGE1"][NUMBER]         = strlen($model->field["BURDEN_CHARGE1"][$slipNo]) > 0 ? $model->field["BURDEN_CHARGE1"][$slipNo] : null;
            $setBurdenCharge1   = strlen($model->field["BURDEN_CHARGE1"][$slipNo]) > 0 ? $model->field["BURDEN_CHARGE1"][$slipNo] : 0;
        }
        if ($model->field["PAID_FLG2"][$slipNo] != '1') {
            $data["BURDEN_CHARGE2"][NUMBER]         = strlen($model->field["BURDEN_CHARGE2"][$slipNo]) > 0 ? $model->field["BURDEN_CHARGE2"][$slipNo] : null;
            $setBurdenCharge2   = strlen($model->field["BURDEN_CHARGE2"][$slipNo]) > 0 ? $model->field["BURDEN_CHARGE2"][$slipNo] : 0;
        }
        $data["TOTAL_BURDEN_CHARGE"][NUMBER]    = ($setBurdenCharge1 + $setBurdenCharge2 > 0) ? $setBurdenCharge1 + $setBurdenCharge2 : null;

        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "SYSDATE()";

        if (is_array($row)) {
            // UPDATE
            $where  = " WHERE ";
            $where .= "     SCHOOLCD            = '".(sprintf("%012d", SCHOOLCD))."' ";
            $where .= "     AND SCHOOL_KIND     = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
            $where .= "     AND YEAR            = '".CTRL_YEAR ."' ";
            $where .= "     AND REDUCTION_TARGET = '1' ";
            $where .= "     AND SLIP_NO         = '{$slipNo}' ";

            $query = Query::updateSQL($data, "REDUCTION_SLIP_BURDEN_CHARGE_DAT", $where);
            $db->query($query);
        } else {
            // INSERT
            $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]      = $model->schoolKindAry[$model->search["GRADE"]];
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["REDUCTION_TARGET"][TEXT] = 1;
            $data["SLIP_NO"][TEXT]          = $slipNo;

            $query = Query::insertSQL($data, "REDUCTION_SLIP_BURDEN_CHARGE_DAT");
            $db->query($query);
        }

        /************************************/
        /* REDUCTION_BURDEN_CHARGE_PLAN_DAT */
        /************************************/

        //補助額の確定フラグ、PLAN更新フラグ
        list($prefFlg1, $prefFlg2, $prefUpdateFlg1, $prefUpdateFlg2) = $prefFlg;

        if ($prefFlg1 != "1" && $prefFlg2 != "1" && $model->field["PAID_FLG1"][$slipNo] != '1' && $model->field["PAID_FLG2"][$slipNo] != '1') {
            //計画データ削除
            $query  = " DELETE FROM ";
            $query .= "     REDUCTION_BURDEN_CHARGE_PLAN_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHOOLCD            = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "     AND SCHOOL_KIND     = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
            $query .= "     AND YEAR            = '".CTRL_YEAR ."' ";
            $query .= "     AND REDUCTION_TARGET = '1' ";
            $query .= "     AND SLIP_NO         = '{$slipNo}' ";
            $query .= "     AND SCHREGNO        = '{$schregNo}' ";
            $db->query($query);
        } else {
            //計画データ削除(確定フラグ:OFF)
            if ($prefUpdateFlg1 == "1" && $model->field["PAID_FLG1"][$slipNo] != '1') {
                $query = knjp721Query::burdenChargePlanMoneyAllClear($model, $slipNo, 'zenki');
                $db->query($query);
            }
            if ($prefUpdateFlg2 == "1" && $model->field["PAID_FLG2"][$slipNo] != '1') {
                $query = knjp721Query::burdenChargePlanMoneyAllClear($model, $slipNo, 'kouki');
                $db->query($query);
            }

            //計画データのある月取得
            $planMonthArray = $db->getCol(knjp721Query::getBurdenChargePlanMonth($model, $slipNo));

            if (get_count($planMonthArray) != 12) {
                // INSERT
                for ($month = 1; $month <= 12; $month++) {
                    if (in_array($month, array_map('intval', $planMonthArray), true)) {
                        continue;
                    }

                    $data = array();
                    $data["SCHOOLCD"][TEXT]             = sprintf("%012d", SCHOOLCD);
                    $data["SCHOOL_KIND"][TEXT]          = $model->schoolKindAry[$model->search["GRADE"]];
                    $data["YEAR"][TEXT]                 = CTRL_YEAR;
                    $data["REDUCTION_TARGET"][TEXT]     = 1;
                    $data["SLIP_NO"][TEXT]              = $slipNo;
                    $data["SCHREGNO"][TEXT]             = $schregNo;
                    $data["PLAN_YEAR"][TEXT]            = (in_array($month, array(1,2,3))) ? (CTRL_YEAR+1) : CTRL_YEAR;
                    $data["PLAN_MONTH"][TEXT]           = sprintf("%02d", $month);
                    $data["BURDEN_CHARGE"][NUMBER]      = 0;
                    $data["BURDEN_CHARGE1"][NUMBER]     = 0;
                    $data["BURDEN_CHARGE2"][NUMBER]     = 0;
                    $data["REGISTERCD"][TEXT]           = STAFFCD;
                    $data["UPDATED"][FUNC]              = "SYSDATE()";

                    $query = Query::insertSQL($data, "REDUCTION_BURDEN_CHARGE_PLAN_DAT");
                    $db->query($query);
                }
            }

            // UPDATE
            $burdenCharge1 = ($prefFlg1 == "1" && $model->field["BURDEN_CHARGE1"][$slipNo] > 0) ? $model->field["BURDEN_CHARGE1"][$slipNo] : 0;
            $burdenCharge2 = ($prefFlg2 == "1" && $model->field["BURDEN_CHARGE2"][$slipNo] > 0) ? $model->field["BURDEN_CHARGE2"][$slipNo] : 0;

            $query  = " WITH MAIN AS ( ";
            $query .= "     SELECT ";
            $query .= "         SLIP_NO, ";
            $query .= "         PLAN_YEAR, ";
            $query .= "         PLAN_MONTH, ";
            $query .= "         PAID_LIMIT_MONTH_CALC, ";
            $query .= "         CASE WHEN PLAN_MONTH IN ('04', '05', '06') THEN '1' ELSE '2' END AS DIV, ";
            $query .= "         CASE WHEN PLAN_MONTH IN ('04', '05', '06') THEN {$burdenCharge1} ELSE {$burdenCharge2} END AS MONEY ";
            $query .= "     FROM ";
            $query .= "         COLLECT_SLIP_PLAN_LIMITDATE_DAT LIMIT_DATE ";
            $query .= "     WHERE ";
            $query .= "         SCHOOLCD            = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "         AND SCHOOL_KIND     = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
            $query .= "         AND YEAR            = '".CTRL_YEAR."' ";
            $query .= "         AND SLIP_NO         = '{$slipNo}' ";
            $query .= "         AND SCHREGNO        = '{$schregNo}' ";
            $query .= "         AND PAID_LIMIT_DATE_CALC IS NOT NULL ";
            $query .= "         AND PAID_LIMIT_MONTH_CALC IS NOT NULL ";
            $query .= " ) ";

            $query .= " SELECT ";
            $query .= "     MAIN.SLIP_NO, ";
            $query .= "     MAIN.PLAN_YEAR, ";
            $query .= "     MAIN.PLAN_MONTH, ";
            $query .= "     CASE WHEN PLAN_MONTH IN ('04','05','06') AND MAIN.MONEY > 0 THEN MAIN.MONEY / COUNT_P.CNT + (CASE WHEN RTRIM(CHAR(MAIN.PLAN_YEAR)) || MAIN.PLAN_MONTH = MAX_M.MAX_MONTH THEN MOD(MAIN.MONEY * COUNT_M.CNT / COUNT_P.CNT, COUNT_M.CNT) ELSE 0 END) ELSE 0 END AS BURDEN_CHARGE1, ";
            $query .= "     CASE WHEN PLAN_MONTH NOT IN ('04','05','06') AND MAIN.MONEY > 0 THEN MAIN.MONEY / COUNT_P.CNT + (CASE WHEN RTRIM(CHAR(MAIN.PLAN_YEAR)) || MAIN.PLAN_MONTH = MAX_M.MAX_MONTH THEN MOD(MAIN.MONEY * COUNT_M.CNT / COUNT_P.CNT, COUNT_M.CNT) ELSE 0 END) ELSE 0 END AS BURDEN_CHARGE2 ";
            $query .= " FROM ";
            $query .= "     MAIN ";
            $query .= "     LEFT JOIN (SELECT ";
            $query .= "                     SLIP_NO, ";
            $query .= "                     DIV, ";
            $query .= "                     COUNT(*) AS CNT ";
            $query .= "                 FROM ";
            $query .= "                     MAIN ";
            $query .= "                 GROUP BY ";
            $query .= "                     SLIP_NO, ";
            $query .= "                     DIV ";
            $query .= "                 ) COUNT_P ON MAIN.SLIP_NO = COUNT_P.SLIP_NO AND MAIN.DIV = COUNT_P.DIV ";
            $query .= "     LEFT JOIN (SELECT ";
            $query .= "                     SLIP_NO, ";
            $query .= "                     DIV, ";
            $query .= "                     PAID_LIMIT_MONTH_CALC, ";
            $query .= "                     COUNT(*) AS CNT ";
            $query .= "                 FROM ";
            $query .= "                     MAIN ";
            $query .= "                 GROUP BY ";
            $query .= "                     SLIP_NO, ";
            $query .= "                     DIV, ";
            $query .= "                     PAID_LIMIT_MONTH_CALC ";
            $query .= "                 ) COUNT_M ON MAIN.SLIP_NO = COUNT_M.SLIP_NO AND MAIN.DIV = COUNT_M.DIV AND MAIN.PAID_LIMIT_MONTH_CALC = COUNT_M.PAID_LIMIT_MONTH_CALC ";
            $query .= "     LEFT JOIN (SELECT ";
            $query .= "                     SLIP_NO, ";
            $query .= "                     DIV, ";
            $query .= "                     PAID_LIMIT_MONTH_CALC, ";
            $query .= "                     MAX(RTRIM(CHAR(PLAN_YEAR)) || PLAN_MONTH) AS MAX_MONTH ";
            $query .= "                 FROM ";
            $query .= "                     MAIN ";
            $query .= "                 GROUP BY ";
            $query .= "                     SLIP_NO, ";
            $query .= "                     DIV, ";
            $query .= "                     PAID_LIMIT_MONTH_CALC ";
            $query .= "                 ) MAX_M ON MAIN.SLIP_NO = MAX_M.SLIP_NO AND MAIN.DIV = MAX_M.DIV AND MAIN.PAID_LIMIT_MONTH_CALC = MAX_M.PAID_LIMIT_MONTH_CALC ";
            $query .= " ORDER BY ";
            $query .= "     MAIN.PLAN_YEAR, ";
            $query .= "     MAIN.PLAN_MONTH ";

            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($model->field["PAID_FLG1"][$slipNo] == '1' && in_array($row["PLAN_MONTH"], array('04', '05', '06'))) {
                    continue;
                }
                if ($model->field["PAID_FLG2"][$slipNo] == '1' && !in_array($row["PLAN_MONTH"], array('04', '05', '06'))) {
                    continue;
                }

                $data = array();
                $data["BURDEN_CHARGE"][NUMBER]      = $row["BURDEN_CHARGE1"] + $row["BURDEN_CHARGE2"];
                $data["BURDEN_CHARGE1"][NUMBER]     = $row["BURDEN_CHARGE1"];
                $data["BURDEN_CHARGE2"][NUMBER]     = $row["BURDEN_CHARGE2"];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = "SYSDATE()";

                $where  = " WHERE ";
                $where .= "     SCHOOLCD                = '".sprintf("%012d", SCHOOLCD)."' ";
                $where .= "     AND SCHOOL_KIND         = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
                $where .= "     AND YEAR                = '".CTRL_YEAR."' ";
                $where .= "     AND REDUCTION_TARGET    = '1' ";
                $where .= "     AND SLIP_NO             = '{$slipNo}' ";
                $where .= "     AND SCHREGNO            = '{$schregNo}' ";
                $where .= "     AND PLAN_YEAR           = '{$row["PLAN_YEAR"]}' ";
                $where .= "     AND PLAN_MONTH          = '{$row["PLAN_MONTH"]}' ";

                $query = Query::updateSQL($data, "REDUCTION_BURDEN_CHARGE_PLAN_DAT", $where);
                $db->query($query);
            }
            $result->free();
        }
    }

    //減免情報取得
    public function selectQueryGenmen($model, $schregno, $school_kind)
    {
        $query  = " SELECT ";
        $query .= "     REDUC_SCHOOL.REDUCTION_DIV_CD, ";
        $query .= "     REDUC_SCHOOL.REDUCTION_DIV_NAME, ";
        $query .= "     SCHOLAR_H.FROM_DATE, ";
        $query .= "     SCHOLAR_H.TO_DATE, ";
        $query .= "     CASE WHEN REDUC_SCHOOL.REDUCTION_DIV = '1' ";
        $query .= "          THEN '学力' ";
        $query .= "          ELSE '家族' ";
        $query .= "     END AS GENMEN_TYPE ";
        $query .= " FROM ";
        $query .= "     SCHREG_SCHOLARSHIP_HIST_DAT SCHOLAR_H, ";
        $query .= "     REDUCTION_SCHOOL_MST REDUC_SCHOOL ";
        $query .= " WHERE ";
        $query .= "     SCHOLAR_H.SCHREGNO          = '{$schregno}' ";
        $query .= "     AND SCHOLAR_H.SCHOOLCD      = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOLAR_H.SCHOOL_KIND   = '{$school_kind}' ";
        $query .= "     AND ('".CTRL_YEAR."-04-01' BETWEEN SCHOLAR_H.FROM_DATE AND SCHOLAR_H.TO_DATE ";
        $query .= "          OR ";
        $query .= "          '".(CTRL_YEAR + 1)."-03-31' BETWEEN SCHOLAR_H.FROM_DATE AND SCHOLAR_H.TO_DATE ";
        $query .= "          OR ";
        $query .= "          SCHOLAR_H.FROM_DATE BETWEEN '".CTRL_YEAR."-04-01' AND '".(CTRL_YEAR + 1)."-03-31' ";
        $query .= "         ) ";
        $query .= "     AND SCHOLAR_H.SCHOOLCD      = REDUC_SCHOOL.SCHOOLCD ";
        $query .= "     AND SCHOLAR_H.SCHOOL_KIND   = REDUC_SCHOOL.SCHOOL_KIND ";
        $query .= "     AND REDUC_SCHOOL.YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOLAR_H.SCHOLARSHIP   = REDUC_SCHOOL.REDUCTION_DIV_CD ";
        $query .= " ORDER BY ";
        $query .= "     REDUC_SCHOOL.REDUCTION_DIV_CD, ";
        $query .= "     SCHOLAR_H.FROM_DATE ";
        return $query;
    }

    //異動情報取得
    public function selectQueryTransfer($model, $schregno)
    {
        //変数
        $sdate =  CTRL_YEAR      ."-04-01";
        $edate = (CTRL_YEAR + 1) ."-03-31";
        //SQL
        $query = "
            WITH TRANS_T AS (
            SELECT
                T1.TRANSFERCD,T1.TRANSFER_SDATE,T1.TRANSFER_EDATE,T2.NAME1
            FROM
                SCHREG_TRANSFER_DAT T1 LEFT OUTER JOIN NAME_MST T2
                                    ON T1.TRANSFERCD = T2.NAMECD2
                                    AND T2.NAMECD1 =  'A004'
            WHERE
                SCHREGNO = '".$schregno."'
                AND T1.TRANSFERCD IN ('1','2')
                AND ((T1.TRANSFER_SDATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}'))
                 OR  (T1.TRANSFER_EDATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}')))
            ), GRD_T AS (
            SELECT
                T1.GRD_DIV AS TRANSFERCD,T1.GRD_DATE AS TRANSFER_SDATE,CAST(NULL AS DATE) AS TRANSFER_EDATE,T2.NAME1
            FROM
                SCHREG_BASE_MST T1 LEFT OUTER JOIN NAME_MST T2
                                    ON T1.GRD_DIV = T2.NAMECD2
                                    AND T2.NAMECD1 =  'A003'
            WHERE
                SCHREGNO = '".$schregno."'
                AND T1.GRD_DIV > '1'
                AND T1.GRD_DATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}')
            ), ENT_T AS (
            SELECT
                T1.ENT_DIV AS TRANSFERCD,T1.ENT_DATE AS TRANSFER_SDATE,CAST(NULL AS DATE) AS TRANSFER_EDATE,T2.NAME1
            FROM
                SCHREG_BASE_MST T1 LEFT OUTER JOIN NAME_MST T2
                                    ON T1.ENT_DIV = T2.NAMECD2
                                    AND T2.NAMECD1 =  'A002'
            WHERE
                SCHREGNO = '".$schregno."'
                AND T1.ENT_DIV > '3'
                AND T1.ENT_DATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}')
            )
            SELECT
                *
            FROM
                ENT_T
            UNION ALL
            SELECT
                *
            FROM
                TRANS_T
            UNION ALL
            SELECT
                *
            FROM
                GRD_T
        ";
        return $query;
    }

    public function updatePlanSum($db, $model, $slipNo, $schregNo)
    {
        $tableArray = array();

        //学校負担
        $tableArray[0]["TABLE"] = "REDUCTION_BURDEN_CHARGE_PLAN_DAT";
        $tableArray[0]["REDUCTION_TARGET"] = "1";
        $tableArray[0]["MONEY_DIV"] = "";

        //支援基準
        $tableArray[1]["TABLE"] = "REDUCTION_COUNTRY_PLAN_DAT";
        $tableArray[1]["REDUCTION_TARGET"] = "";
        $tableArray[1]["MONEY_DIV"] = "";

        //支援加算
        $tableArray[2]["TABLE"] = "REDUCTION_COUNTRY_PLAN_DAT";
        $tableArray[2]["REDUCTION_TARGET"] = "";
        $tableArray[2]["MONEY_DIV"] = "ADD_";

        //補助
        $tableArray[3]["TABLE"] = "REDUCTION_PLAN_DAT";
        $tableArray[3]["REDUCTION_TARGET"] = "1";
        $tableArray[3]["MONEY_DIV"] = "";

        //減免
        $tableArray[4]["TABLE"] = "REDUCTION_SCHOOL_PLAN_DAT";
        $tableArray[4]["REDUCTION_TARGET"] = "1";
        $tableArray[4]["MONEY_DIV"] = "";

        foreach ($tableArray as $key => $tableInfo) {
            $query  = " SELECT ";
            $query .= "     PLAN_T.SCHOOLCD, ";
            $query .= "     PLAN_T.SCHOOL_KIND, ";
            $query .= "     PLAN_T.YEAR, ";
            $query .= "     PLAN_T.SLIP_NO, ";
            $query .= "     YEAR(MAX(LIMIT_DATE.PAID_LIMIT_DATE_CALC)) AS PLAN_YEAR, ";
            $query .= "     LIMIT_DATE.PAID_LIMIT_MONTH_CALC AS PLAN_MONTH, ";
            $query .= "     PLAN_T.SCHREGNO, ";
            if ($tableInfo["TABLE"] == "REDUCTION_BURDEN_CHARGE_PLAN_DAT") {
                $query .= "     SUM(PLAN_T.BURDEN_CHARGE) AS BURDEN_CHARGE, ";
                $query .= "     SUM(PLAN_T.BURDEN_CHARGE1) AS BURDEN_CHARGE1, ";
                $query .= "     SUM(PLAN_T.BURDEN_CHARGE2) AS BURDEN_CHARGE2 ";
            } else {
                $query .= "     SUM(PLAN_T.{$tableInfo["MONEY_DIV"]}PLAN_MONEY) AS PLAN_MONEY, ";
                $query .= "     SUM(PLAN_T.{$tableInfo["MONEY_DIV"]}DECISION_MONEY) AS DECISION_MONEY, ";
                $query .= "     SUM(PLAN_T.{$tableInfo["MONEY_DIV"]}DECISION_MONEY1) AS DECISION_MONEY1, ";
                $query .= "     SUM(PLAN_T.{$tableInfo["MONEY_DIV"]}DECISION_MONEY2) AS DECISION_MONEY2 ";
            }
            $query .= " FROM ";
            $query .= "     {$tableInfo["TABLE"]} PLAN_T ";
            $query .= "     INNER JOIN COLLECT_SLIP_PLAN_LIMITDATE_DAT LIMIT_DATE ";
            $query .= "          ON PLAN_T.SCHOOLCD     = LIMIT_DATE.SCHOOLCD ";
            $query .= "         AND PLAN_T.SCHOOL_KIND  = LIMIT_DATE.SCHOOL_KIND ";
            $query .= "         AND PLAN_T.YEAR         = LIMIT_DATE.YEAR ";
            $query .= "         AND PLAN_T.SCHREGNO     = LIMIT_DATE.SCHREGNO ";
            $query .= "         AND PLAN_T.SLIP_NO      = LIMIT_DATE.SLIP_NO ";
            $query .= "         AND PLAN_T.PLAN_YEAR    = LIMIT_DATE.PLAN_YEAR ";
            $query .= "         AND PLAN_T.PLAN_MONTH   = LIMIT_DATE.PLAN_MONTH ";
            $query .= " WHERE ";
            $query .= "     PLAN_T.SCHOOLCD         = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND PLAN_T.SCHOOL_KIND  = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
            $query .= "     AND PLAN_T.YEAR         = '".CTRL_YEAR."' ";
            $query .= "     AND PLAN_T.SLIP_NO      = '{$slipNo}' ";
            $query .= "     AND PLAN_T.SCHREGNO     = '{$schregNo}' ";
            if ($tableInfo["REDUCTION_TARGET"]) {
                $query .= "     AND PLAN_T.REDUCTION_TARGET = '{$tableInfo["REDUCTION_TARGET"]}' ";
            }
            $query .= "     AND LIMIT_DATE.PAID_LIMIT_DATE_CALC IS NOT NULL ";
            $query .= "     AND LIMIT_DATE.PAID_LIMIT_MONTH_CALC IS NOT NULL ";
            $query .= " GROUP BY ";
            $query .= "     PLAN_T.SCHOOLCD, ";
            $query .= "     PLAN_T.SCHOOL_KIND, ";
            $query .= "     PLAN_T.YEAR, ";
            $query .= "     PLAN_T.SCHREGNO, ";
            $query .= "     PLAN_T.SLIP_NO, ";
            $query .= "     LIMIT_DATE.PAID_LIMIT_MONTH_CALC ";
            $query .= " HAVING ";
            if ($tableInfo["TABLE"] == "REDUCTION_BURDEN_CHARGE_PLAN_DAT") {
                $query .= "     VALUE(SUM(PLAN_T.BURDEN_CHARGE1), 0) > 0 ";
                $query .= "     OR VALUE(SUM(PLAN_T.BURDEN_CHARGE2), 0) > 0 ";
            } else {
                $query .= "     VALUE(SUM(PLAN_T.{$tableInfo["MONEY_DIV"]}PLAN_MONEY), 0) > 0 ";
                $query .= "     OR VALUE(SUM(PLAN_T.{$tableInfo["MONEY_DIV"]}DECISION_MONEY1), 0) > 0 ";
                $query .= "     OR VALUE(SUM(PLAN_T.{$tableInfo["MONEY_DIV"]}DECISION_MONEY2), 0) > 0 ";
            }

            $result = $db->query($query);
            $updSumArray = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $updSumArray[] = $row;
            }
            $result->free();

            //金額クリア
            $data = array();
            if ($tableInfo["TABLE"] == "REDUCTION_BURDEN_CHARGE_PLAN_DAT") {
                $data["BURDEN_CHARGE"][NUMBER] = 0;
                $data["BURDEN_CHARGE1"][NUMBER] = 0;
                $data["BURDEN_CHARGE2"][NUMBER] = 0;
            } else {
                $data[$tableInfo["MONEY_DIV"]."PLAN_MONEY"][NUMBER]         = 0;
                $data[$tableInfo["MONEY_DIV"]."DECISION_MONEY"][NUMBER]     = 0;
                $data[$tableInfo["MONEY_DIV"]."DECISION_MONEY1"][NUMBER]    = 0;
                $data[$tableInfo["MONEY_DIV"]."DECISION_MONEY2"][NUMBER]    = 0;
            }
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][FUNC]    = "SYSDATE()";

            $where  = " WHERE ";
            $where .= "     SCHOOLCD            = '".(sprintf("%012d", SCHOOLCD))."' ";
            $where .= "     AND SCHOOL_KIND     = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
            $where .= "     AND YEAR            = '".CTRL_YEAR ."' ";
            $where .= "     AND SLIP_NO         = '{$slipNo}' ";
            $where .= "     AND SCHREGNO         = '{$schregNo}' ";
            if ($tableInfo["REDUCTION_TARGET"]) {
                $where .= "     AND REDUCTION_TARGET = '{$tableInfo["REDUCTION_TARGET"]}' ";
            }

            $query = Query::updateSQL($data, $tableInfo["TABLE"], $where);
            $db->query($query);

            //更新
            foreach ($updSumArray as $updKey => $updVal) {
                $data = array();
                if ($tableInfo["TABLE"] == "REDUCTION_BURDEN_CHARGE_PLAN_DAT") {
                    $data["BURDEN_CHARGE"][NUMBER]  = $updVal["BURDEN_CHARGE1"] + $updVal["BURDEN_CHARGE2"];
                    $data["BURDEN_CHARGE1"][NUMBER] = $updVal["BURDEN_CHARGE1"];
                    $data["BURDEN_CHARGE2"][NUMBER] = $updVal["BURDEN_CHARGE2"];
                } else {
                    $data[$tableInfo["MONEY_DIV"]."PLAN_MONEY"][NUMBER]         = $updVal["PLAN_MONEY"];
                    $data[$tableInfo["MONEY_DIV"]."DECISION_MONEY"][NUMBER]     = $updVal["DECISION_MONEY1"] + $updVal["DECISION_MONEY2"];
                    $data[$tableInfo["MONEY_DIV"]."DECISION_MONEY1"][NUMBER]    = $updVal["DECISION_MONEY1"];
                    $data[$tableInfo["MONEY_DIV"]."DECISION_MONEY2"][NUMBER]    = $updVal["DECISION_MONEY2"];
                }
                $data["REGISTERCD"][TEXT]                              = STAFFCD;
                $data["UPDATED"][FUNC]                                 = "SYSDATE()";

                $where  = " WHERE ";
                $where .= "     SCHOOLCD            = '{$updVal["SCHOOLCD"]}' ";
                $where .= "     AND SCHOOL_KIND     = '{$updVal["SCHOOL_KIND"]}' ";
                $where .= "     AND YEAR            = '{$updVal["YEAR"]}' ";
                $where .= "     AND SLIP_NO         = '{$updVal["SLIP_NO"]}' ";
                $where .= "     AND SCHREGNO        = '{$updVal["SCHREGNO"]}' ";
                if ($tableInfo["REDUCTION_TARGET"]) {
                    $where .= "     AND REDUCTION_TARGET = '{$tableInfo["REDUCTION_TARGET"]}' ";
                }
                $where .= "     AND PLAN_YEAR       = '{$updVal["PLAN_YEAR"]}' ";
                $where .= "     AND PLAN_MONTH      = '{$updVal["PLAN_MONTH"]}' ";

                $query = Query::updateSQL($data, $tableInfo["TABLE"], $where);
                $db->query($query);
            }
        }
    }
}
