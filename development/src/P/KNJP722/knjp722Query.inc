<?php

require_once('for_php7.php');

class knjp722Query extends Query {

    function getRegdWith($model) {
        $query  = " WITH REGD_MAX_SEM AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         MAX(SEMESTER) AS SEMESTER, ";
        $query .= "         SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= "         SCHREGNO ";
        $query .= " ), REGD_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         REGD_MAX_SEM T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = T2.YEAR ";
        $query .= "         AND T1.GRADE || '-' || T1.HR_CLASS = '".$model->search["HR_CLASS"]."' ";
        $query .= "         AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "         AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ), SLIP_T AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         REGD_T.YEAR, ";
        $query .= "         REGD_T.SEMESTER, ";
        $query .= "         REGD_T.SCHREGNO, ";
        $query .= "         REGD_T.GRADE, ";
        $query .= "         REGD_T.HR_CLASS, ";
        $query .= "         REGD_T.ATTENDNO, ";
        $query .= "         SLIP_D.SLIP_NO ";
        $query .= "     FROM ";
        $query .= "         REGD_T, ";
        $query .= "         COLLECT_SLIP_DAT SLIP_D ";
        $query .= "         INNER JOIN COLLECT_SLIP_M_DAT SLIP_MD ON SLIP_MD.SCHOOLCD = SLIP_D.SCHOOLCD ";
        $query .= "               AND SLIP_MD.SCHOOL_KIND   = SLIP_D.SCHOOL_KIND ";
        $query .= "               AND SLIP_MD.YEAR          = SLIP_D.YEAR ";
        $query .= "               AND SLIP_MD.SLIP_NO       = SLIP_D.SLIP_NO ";
        $query .= "         INNER JOIN COLLECT_M_MST COL_M ON COL_M.SCHOOLCD = SLIP_MD.SCHOOLCD ";
        $query .= "               AND COL_M.SCHOOL_KIND     = SLIP_MD.SCHOOL_KIND ";
        $query .= "               AND COL_M.YEAR            = SLIP_MD.YEAR ";
        $query .= "               AND COL_M.COLLECT_L_CD    = SLIP_MD.COLLECT_L_CD ";
        $query .= "               AND COL_M.COLLECT_M_CD    = SLIP_MD.COLLECT_M_CD ";
        $query .= "               AND VALUE(COL_M.GAKUNOKIN_DIV, '') = '2' ";
        $query .= "               AND (VALUE(COL_M.REDUCTION_DIV, '') = '3' OR VALUE(COL_M.IS_REDUCTION_SCHOOL, '') = '1') ";
        $query .= "     WHERE ";
        $query .= "         SLIP_D.SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "         AND SLIP_D.SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "         AND SLIP_D.CANCEL_DATE IS NULL ";
        $query .= "         AND REGD_T.YEAR = SLIP_D.YEAR ";
        $query .= "         AND REGD_T.SCHREGNO = SLIP_D.SCHREGNO ";
        $query .= " ) ";

        return $query;
    }

    function selectQuery($model) {
        //変数
        $sdate =  CTRL_YEAR      ."-04-01";
        $edate = (CTRL_YEAR + 1) ."-03-31";
        //SQL
        $query  = knjp722Query::getRegdWith($model);
        $query .= " SELECT ";
        $query .= "     STD_INFO.SCHREGNO, ";
        $query .= "     STD_INFO.NAME_SHOW, ";
        $query .= "     STD_INFO.GRADE, ";
        $query .= "     STD_INFO.HR_CLASS, ";
        $query .= "     STD_INFO.HR_NAMEABBV, ";
        $query .= "     STD_INFO.ATTENDNO, ";
        $query .= "     STD_INFO.SLIP_NO, ";
        $query .= "     PAID_D.PAID_FLG, ";

        $query .= "     INCOME_D.INCOME_1 AS REDUC_INCOME, ";

        $query .= "     REDUCTION_D.SEQ_1 AS REDUCTION_SEQ, ";
        $query .= "     GUARDIAN.PREF AS G_PREF, ";
        $query .= "     GUARDIAN.PREFECTURESCD AS G_PREF_CD, ";
        $query .= "     P_RARE.NAMECD2 || ':' || P_RARE.NAMESPARE1 AS PREF_RARE_CASE_CD, ";
        $query .= "     P_RARE.NAMESPARE1 AS PREF_TEXT_DISP, ";
        $query .= "     REDUCTION_D.RANK_1 AS PREF_RANK, ";
        $query .= "     REDUCTION_D.MONEY_1 AS PREF_MONEY, ";
        $query .= "     REDUCTION_D.DEC_FLG_1 AS PREF_FLG, ";
        $query .= "     REDUCTION_D.PREFECTURESCD AS REDUC_PREF_CD, ";
        $query .= "     NM1.NAME1 AS REDUC_PREF, ";
        $query .= "     PREF_GRADE.PREFECTURESCD AS PREF_GRADE, ";

        $query .= "     value(TRANSFER.COUNTTRANSFER,0) + value(GRD.COUNTTRANSFER,0) + value(ENT.COUNTTRANSFER,0) AS COUNTTRANSFER, ";
        $query .= "     value(GENMEN.COUNTGENMEN, 0) AS COUNTGENMEN, ";

        $query .= "     PREF_PLAN.ZENKI_PREF_CNT, ";
        $query .= "     PREF_PLAN.KOUKI_PREF_CNT, ";

        $query .= "     COUNTRY_D.REDUCTION_NYUGAKUKIN, ";
        $query .= "     REDUCTION_SCHOOL_D.DEC_FLG_1 AS REDUC_SCHOOL_FLG, ";
        $query .= "     REDUCTION_SCHOOL_D.SCHREGNO AS REDUCTION_SCHREGNO, ";

        $query .= "     REDUCTION_D.REMARK ";
        $query .= " FROM ";
        $query .= "   (SELECT ";
        $query .= "     ST2.YEAR, ";
        $query .= "     ST2.SEMESTER, ";
        $query .= "     ST1.SCHREGNO, ";
        $query .= "     ST1.NAME_SHOW, ";
        $query .= "     ST2.GRADE, ";
        $query .= "     ST2.HR_CLASS, ";
        $query .= "     ST2.ATTENDNO, ";
        $query .= "     ST3.HR_NAMEABBV, ";
        $query .= "     ST2.SLIP_NO ";
        $query .= "   FROM ";
        $query .= "     SCHREG_BASE_MST ST1, ";
        $query .= "     SLIP_T ST2 ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT ST3 ";
        $query .= "          ON ST2.YEAR        = ST3.YEAR ";
        $query .= "         AND ST2.SEMESTER    = ST3.SEMESTER ";
        $query .= "         AND ST2.GRADE       = ST3.GRADE ";
        $query .= "         AND ST2.HR_CLASS    = ST3.HR_CLASS ";
        $query .= "   WHERE ";
        $query .= "     ST1.SCHREGNO = ST2.SCHREGNO ";
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
        $query .= "          ON INCOME_D.SCHOOLCD       = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "         AND INCOME_D.SCHOOL_KIND    = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "         AND INCOME_D.YEAR           = '".CTRL_YEAR."' ";
        $query .= "         AND INCOME_D.SCHREGNO       = STD_INFO.SCHREGNO ";
        //支援金
        $query .= "     LEFT JOIN REDUCTION_COUNTRY_DAT COUNTRY_D ";
        $query .= "          ON COUNTRY_D.SCHOOLCD      = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "         AND COUNTRY_D.SCHOOL_KIND   = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "         AND COUNTRY_D.YEAR          = '".CTRL_YEAR."' ";
        $query .= "         AND STD_INFO.SLIP_NO        = COUNTRY_D.SLIP_NO ";
        //補助金
        $query .= "     LEFT JOIN REDUCTION_DAT REDUCTION_D ";
        $query .= "          ON REDUCTION_D.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "         AND REDUCTION_D.SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "         AND STD_INFO.YEAR           = REDUCTION_D.YEAR ";
        $query .= "         AND STD_INFO.SLIP_NO        = REDUCTION_D.SLIP_NO ";
        $query .= "         AND REDUCTION_D.REDUCTION_TARGET = '2' ";
        $query .= "     LEFT JOIN NAME_MST NM1 ";
        $query .= "          ON NM1.NAMECD1 = 'G202' ";
        $query .= "         AND NM1.NAMECD2 = REDUCTION_D.PREFECTURESCD ";
        $query .= "     LEFT JOIN NAME_MST P_RARE ";
        $query .= "          ON P_RARE.NAMECD1  = 'P711' ";
        $query .= "         AND P_RARE.NAMECD2  = REDUCTION_D.RARE_CASE_CD_1 ";
        //減免
        $query .= "     LEFT JOIN REDUCTION_SCHOOL_DAT REDUCTION_SCHOOL_D ";
        $query .= "          ON REDUCTION_SCHOOL_D.SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "         AND REDUCTION_SCHOOL_D.SCHOOL_KIND  = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "         AND REDUCTION_SCHOOL_D.REDUCTION_TARGET = '2' ";
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
        $query .= "                     OR  (TRANSFER_EDATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}'))) ";
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
        //補助金計画カウント
        $query .= "     LEFT JOIN (SELECT ";
        $query .= "                     SLIP_NO, ";
        $query .= "                     SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH < '".CTRL_YEAR."07' AND PLAN_MONEY IS NOT NULL ";
        $query .= "                              THEN 1 ";
        $query .= "                              ELSE 0 ";
        $query .= "                         END) AS ZENKI_PREF_CNT, ";
        $query .= "                     SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH >= '".CTRL_YEAR."07' AND PLAN_MONEY IS NOT NULL ";
        $query .= "                              THEN 1 ";
        $query .= "                              ELSE 0 ";
        $query .= "                         END) AS KOUKI_PREF_CNT ";
        $query .= "                 FROM ";
        $query .= "                     REDUCTION_PLAN_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     SCHOOLCD            = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "                     AND SCHOOL_KIND     = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "                     AND YEAR            = '".CTRL_YEAR."' ";
        $query .= "                     AND REDUCTION_TARGET = '2' ";
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
        $query .= "                 ) PREF_GRADE ON STD_INFO.GRADE = PREF_GRADE.GRADE AND GUARDIAN.PREFECTURESCD = PREF_GRADE.PREFECTURESCD ";
        //入金済み
        $query .= "     LEFT JOIN (SELECT DISTINCT ";
        $query .= "                     SLIP_NO, ";
        $query .= "                     '1' AS PAID_FLG ";
        $query .= "                 FROM ";
        $query .= "                     COLLECT_SLIP_PLAN_PAID_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "                     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "                     AND YEAR        = '".CTRL_YEAR."' ";
        $query .= "                     AND PLAN_PAID_MONEY_DATE IS NOT NULL ";
        $query .= "                 ) PAID_D ON STD_INFO.SLIP_NO = PAID_D.SLIP_NO ";
        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     ATTENDNO, ";
        $query .= "     STD_INFO.SLIP_NO ";

        return $query;
    }

    //年組
    function selectQueryGrade(&$model) {
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
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT REGD_G ON REGD_H.YEAR = REGD_G.YEAR ";
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
    function getSlipPlan($model) {
        $query  = knjp722Query::getRegdWith($model);
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
        $query .= "     AND VALUE(M_MST.GAKUNOKIN_DIV, '')  = '2' ";
        $query .= "     AND (VALUE(M_MST.REDUCTION_DIV, '') = '3' OR VALUE(M_MST.IS_REDUCTION_SCHOOL, '') = '1') ";
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

    //補助金MST
    function getReductionMst($model) {
        $query  = " SELECT ";
        $query .= "     REDUC_M.PREFECTURESCD, ";
        $query .= "     REDUC_M.REDUCTION_SEQ, ";
        $query .= "     REDUC_M.MONEY_DIV1 AS MONEY_DIV, ";
        $query .= "     REDUC_M.NUMERATOR1 AS NUMERATOR, ";
        $query .= "     REDUC_M.DENOMINATOR1 AS DENOMINATOR, ";
        $query .= "     REDUC_M.REDUCTIONMONEY_1 AS REDUCTIONMONEY, ";
        $query .= "     REDUC_M.INCOME_LOW1 AS INCOME_LOW, ";
        $query .= "     REDUC_M.INCOME_HIGH1 AS INCOME_HIGH, ";
        $query .= "     REDUC_M.INCOME_RANK1 AS INCOME_RANK ";
        $query .= " FROM ";
        $query .= "     REDUCTION_MST REDUC_M ";
        $query .= " WHERE ";
        $query .= "     REDUC_M.SCHOOLCD                = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND REDUC_M.SCHOOL_KIND         = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND REDUC_M.YEAR                = '".CTRL_YEAR."' ";
        $query .= "     AND REDUC_M.REDUCTION_TARGET    = '2' ";
        $query .= "     AND REDUC_M.GRADE               = '{$model->search["GRADE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     REDUC_M.PREFECTURESCD, ";
        $query .= "     REDUC_M.INCOME_RANK1, ";
        $query .= "     REDUC_M.INCOME_RANK2, ";
        $query .= "     REDUC_M.REDUCTION_SEQ ";

        return $query;
    }

    //生徒減免
    function getReductionStd($model) {
        $query  = knjp722Query::getRegdWith($model);
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
        $query .= "         AND REDUC_SCH_TRGT.REDUCTION_TARGET = '2' ";
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

    //名称マスタ
    function getNameMstSql($model, $namecd1) {
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
        $query .= "   VALUE ";

        return $query;
    }

    //都道府県コード
    function selectQueryReductionPref(&$model) {
        $query = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1 = 'G202' AND ";
        if (is_numeric($model->income["PREFECTURESCD"])) {      //軽減対象都道府県設定値
            $query .= "     T1.NAMECD2 = '".$model->income["PREFECTURESCD"]."' ";
        } else {                                                //保護者都道府県
            $query .= "     T1.NAMECD2 = (SELECT ";
            $query .= "                       SUBSTR(MIN(ST1.CITYCD),1,2) ";
            $query .= "                   FROM ";
            $query .= "                       ZIPCD_MST ST1, ";
            $query .= "                       GUARDIAN_DAT ST2 ";
            $query .= "                   WHERE ";
            $query .= "                       ST1.NEW_ZIPCD = ST2.GUARANTOR_ZIPCD AND ";
            $query .= "                       ST2.SCHREGNO  = '".$model->income["SCHREGNO"]."' ";
            $query .= "                   ) ";
        }
        return $query;
    }

    function getPrefPlanMonth($model, $slipNo) {
        $query  = " SELECT ";
        $query .= "     PLAN_MONTH ";
        $query .= " FROM ";
        $query .= "     REDUCTION_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD                = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND         = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND YEAR                = '".CTRL_YEAR."' ";
        $query .= "     AND SLIP_NO             = '{$slipNo}' ";
        $query .= "     AND REDUCTION_TARGET    = '2' ";
        return $query;
    }

    function getReducSchoolPlanMonth($model, $slipNo) {
        $query  = " SELECT ";
        $query .= "     PLAN_MONTH ";
        $query .= " FROM ";
        $query .= "     REDUCTION_SCHOOL_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD                = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND         = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND YEAR                = '".CTRL_YEAR."' ";
        $query .= "     AND SLIP_NO             = '{$slipNo}' ";
        $query .= "     AND REDUCTION_TARGET    = '2' ";
        return $query;
    }

    function update($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        for($i = 0; $i < get_count($model->slipno); $i++) {
            $slipNo = $model->slipno[$i];
            $schregNo = $model->slipSchreg[$slipNo];

            //入金済みの場合、処理しない
            if ($model->field["PAID_FLG"][$slipNo] == '1') continue;

            knjp722Query::countryUpdate($db, $model, $slipNo, $schregNo, $i);
            knjp722Query::prefUpdate($db, $model, $slipNo, $schregNo, $i);
            knjp722Query::reductionSchoolUpdate($db, $model, $slipNo, $schregNo, $i);
            knjp722Query::updatePlanSum($db, $model, $slipNo, $schregNo);
        }
        $db->commit();
        Query::dbCheckIn($db);
    }

    function countryUpdate($db, $model, $slipNo, $schregNo, $rowCnt) {
        /*************************/
        /* REDUCTION_COUNTRY_DAT */
        /*************************/
        $data = array();
        $data["SCHREGNO"][TEXT]                 = $schregNo;
        $data["GRADE"][TEXT]                    = $model->search["GRADE"];
        $data["REDUCTION_NYUGAKUKIN"][NUMBER]   = $model->slipSchoolStd[$slipNo]["SAKI1"];
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
    }

    function prefUpdate($db, $model, $slipNo, $schregNo, $rowCnt) {
        /*****************/
        /* REDUCTION_DAT */
        /*****************/
        if (is_array($model->field["PREF_FLG"])) {
            $prefFlg = in_array($slipNo, $model->field["PREF_FLG"])? 1 : NULL;
        } else {
            $prefFlg = NULL;
        }

        $data = array();
        $data["SCHREGNO"][TEXT]                 = $schregNo;
        $data["PREFECTURESCD"][TEXT]            = $model->slipnoPref[$slipNo];
        $data["GRADE"][TEXT]                    = $model->search["GRADE"];

        list($rareCd, $baseDummy1) = preg_split("/:/", $model->field["PREF_RARE_CASE_CD"][$rowCnt]);
        $model->field["PREF_RARE_CASE_CD"][$rowCnt] = ($rareCd == 'DUMMY') ? '' : $rareCd;
        $data["RARE_CASE_CD_1"][TEXT]           = $model->field["PREF_RARE_CASE_CD"][$rowCnt] != "" ? $model->field["PREF_RARE_CASE_CD"][$rowCnt] : NULL;

        $data["SEQ_1"][NUMBER]                  = (is_numeric($model->field["REDUCTION_SEQ"][$rowCnt]) && $model->field["PREF_RARE_CASE_CD"][$rowCnt] == "") ? $model->field["REDUCTION_SEQ"][$rowCnt] : NULL;
        $data["RANK_1"][TEXT]                   = $model->field["PREF_RANK"][$rowCnt];
        $data["MONEY_1"][NUMBER]                = strlen($model->field["PREF_MONEY"][$rowCnt]) > 0 ? $model->field["PREF_MONEY"][$rowCnt] : NULL;
        $data["DEC_FLG_1"][TEXT]                = $prefFlg;

        $data["REMARK"][TEXT]                   = $model->field["REDUC_REMARK"][$rowCnt] ? $model->field["REDUC_REMARK"][$rowCnt] : NULL;
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "SYSDATE()";

        $query  = " SELECT * FROM";
        $query .= "     REDUCTION_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD                = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND         = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND YEAR                = '".CTRL_YEAR ."' ";
        $query .= "     AND REDUCTION_TARGET    = '2' ";
        $query .= "     AND SLIP_NO             = '{$slipNo}' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if (is_array($row)) {
            /**********/
            /* UPDATE */
            /**********/

            $where  = " WHERE ";
            $where .= "     SCHOOLCD                = '".(sprintf("%012d", SCHOOLCD))."' ";
            $where .= "     AND SCHOOL_KIND         = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
            $where .= "     AND YEAR                = '".CTRL_YEAR ."' ";
            $where .= "     AND REDUCTION_TARGET    = '2' ";
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
            $data["REDUCTION_TARGET"][TEXT] = 2;
            $data["SLIP_NO"][TEXT]          = $slipNo;

            $query = Query::insertSQL($data, "REDUCTION_DAT");
            $result = $db->query($query);
        }

        /**********************/
        /* REDUCTION_PLAN_DAT */
        /**********************/
        $prefDecFlg = ($prefFlg == '1' && $row["DEC_FLG_1"] != "1") ? 1 : 0;

        if ($prefFlg != "1") {
            //計画データ削除(確定フラグ:OFF)
            $query = knjp722Query::planPreDecisionMoneyAllClear($model, $slipNo);
            $db->query($query);
        } else if ($prefDecFlg == "1") {
            //計画データのある月取得
            $planMonthArray = $db->getCol(knjp722Query::getPrefPlanMonth($model, $slipNo));

            if (get_count($planMonthArray) != 12) {
                // INSERT
                for ($month = 1; $month <= 12; $month++) {
                    if (in_array($month, array_map('intval', $planMonthArray), true)) {
                        continue;
                    }
                    $setMoney = $setDecisionMoney = 0;
                    $setMonth = sprintf("%02d", $month);
                    knjp722Query::insertPrefPlan($db, $model, $slipNo, $schregNo, $setMonth, $setMoney, $setDecisionMoney);
                }
            }

            // UPDATE
            $money = ($model->field["PREF_MONEY"][$rowCnt] > 0) ? $model->field["PREF_MONEY"][$rowCnt] : 0;
            $setDecisionMoneyArray = array();
            $checkMonth = $model->decMonth["PREF_DEC_MONTH"] < "04" ? $model->decMonth["PREF_DEC_MONTH"] + 12 : $model->decMonth["PREF_DEC_MONTH"];

            $query  = " WITH MAIN AS ( ";
            $query .= "     SELECT ";
            $query .= "         SLIP_NO, ";
            $query .= "         PLAN_YEAR, ";
            $query .= "         PLAN_MONTH, ";
            $query .= "         PAID_LIMIT_MONTH, ";
            $query .= "         {$money} AS MONEY ";
            $query .= "     FROM ";
            $query .= "         COLLECT_SLIP_PLAN_LIMITDATE_DAT LIMIT_DATE ";
            $query .= "     WHERE ";
            $query .= "         SCHOOLCD                = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "         AND SCHOOL_KIND         = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
            $query .= "         AND YEAR                = '".CTRL_YEAR."' ";
            $query .= "         AND SLIP_NO             = '{$slipNo}' ";
            $query .= "         AND SCHREGNO            = '{$schregNo}' ";
            $query .= "         AND PAID_LIMIT_DATE IS NOT NULL ";
            $query .= "         AND PAID_LIMIT_MONTH IS NOT NULL ";
            $query .= " ) ";

            $query .= " SELECT ";
            $query .= "     MAIN.SLIP_NO, ";
            $query .= "     MAIN.PLAN_YEAR, ";
            $query .= "     MAIN.PLAN_MONTH, ";
            $query .= "     CASE WHEN MAIN.MONEY > 0 THEN MAIN.MONEY / COUNT_T.CNT + (CASE WHEN RTRIM(CHAR(MAIN.PLAN_YEAR)) || MAIN.PLAN_MONTH = MAX_M.MAX_MONTH THEN MOD(MAIN.MONEY * COUNT_M.CNT / COUNT_T.CNT, COUNT_M.CNT) ELSE 0 END) ELSE 0 END AS PLAN_MONEY ";
            $query .= " FROM ";
            $query .= "     MAIN ";
            $query .= "     LEFT JOIN (SELECT SLIP_NO, COUNT(*) AS CNT FROM MAIN GROUP BY SLIP_NO) COUNT_T ON MAIN.SLIP_NO = COUNT_T.SLIP_NO ";
            $query .= "     LEFT JOIN (SELECT SLIP_NO, PAID_LIMIT_MONTH, COUNT(*) AS CNT FROM MAIN GROUP BY SLIP_NO, PAID_LIMIT_MONTH) COUNT_M ON MAIN.SLIP_NO = COUNT_M.SLIP_NO AND MAIN.PAID_LIMIT_MONTH = COUNT_M.PAID_LIMIT_MONTH ";
            $query .= "     LEFT JOIN (SELECT SLIP_NO, PAID_LIMIT_MONTH, MAX(RTRIM(CHAR(PLAN_YEAR)) || PLAN_MONTH) AS MAX_MONTH FROM MAIN GROUP BY SLIP_NO, PAID_LIMIT_MONTH) MAX_M ON MAIN.SLIP_NO = MAX_M.SLIP_NO AND MAIN.PAID_LIMIT_MONTH = MAX_M.PAID_LIMIT_MONTH ";
            $query .= " ORDER BY ";
            $query .= "     MAIN.PLAN_YEAR, ";
            $query .= "     MAIN.PLAN_MONTH ";

            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $setMoney = $row["PLAN_MONEY"];
                $setDecisionMoney = $row["PLAN_MONEY"];
                $planMonth = $row["PLAN_MONTH"] < "04" ? $row["PLAN_MONTH"] + 12 : $row["PLAN_MONTH"];
                if ($planMonth <= $checkMonth) {
                    $setDecisionMoneyArray[$model->decMonth["PREF_DEC_MONTH"]] += $setMoney;
                    $setDecisionMoney = 0;
                }

                $data = array();
                $data["PLAN_MONEY"][NUMBER]         = $setMoney;
                $data["DECISION_MONEY"][NUMBER]     = $setDecisionMoney;
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = "SYSDATE()";

                $where  = " WHERE ";
                $where .= "     SCHOOLCD                = '".sprintf("%012d", SCHOOLCD)."' ";
                $where .= "     AND SCHOOL_KIND         = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
                $where .= "     AND YEAR                = '".CTRL_YEAR."' ";
                $where .= "     AND SLIP_NO             = '{$slipNo}' ";
                $where .= "     AND REDUCTION_TARGET    = '2' ";
                $where .= "     AND SCHREGNO            = '{$schregNo}' ";
                $where .= "     AND PLAN_YEAR           = '{$row["PLAN_YEAR"]}' ";
                $where .= "     AND PLAN_MONTH          = '{$row["PLAN_MONTH"]}' ";

                $query = Query::updateSQL($data, "REDUCTION_PLAN_DAT", $where);
                $db->query($query);
            }
            $result->free();
        }

        //確定月以前の確定金額を確定月に寄せる処理
        knjp722Query::updatePrefDecPlan($db, $model, $slipNo, $model->decMonth["PREF_DEC_MONTH"], $setDecisionMoneyArray[$model->decMonth["PREF_DEC_MONTH"]]);
    }

    function reductionSchoolUpdate($db, $model, $slipNo, $schregNo, $rowCnt) {
        if (is_array($model->field["REDUC_SCHOOL_FLG"])) {
            $reducSchoolFlg = in_array($slipNo, $model->field["REDUC_SCHOOL_FLG"])? 1 : NULL;
        } else {
            $reducSchoolFlg = NULL;
        }
        knjp722Query::setReductionSchoolDat($db, $model, $slipNo, $schregNo, $model->slipSchoolStd[$slipNo]["SAKI1"], $rowCnt, $reducSchoolFlg);
    }

    function setReductionSchoolDat($db, $model, $slipNo, $schregNo, $money, $rowCnt, $reducSchoolFlg) {
        /************************/
        /* REDUCTION_SCHOOL_DAT */
        /************************/
        $data = array();
        $data["SCHREGNO"][TEXT]                 = $schregNo;

        $data["MONEY_1"][NUMBER]                = strlen($money) > 0 ? $money : NULL;
        $data["DEC_FLG_1"][TEXT]                = $reducSchoolFlg;

        $data["REMARK"][TEXT]                   = $model->field["REDUC_REMARK"][$rowCnt] ? $model->field["REDUC_REMARK"][$rowCnt] : NULL;
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "SYSDATE()";

        $query  = " SELECT * FROM";
        $query .= "     REDUCTION_SCHOOL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD                = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND         = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND YEAR                = '".CTRL_YEAR ."' ";
        $query .= "     AND SLIP_NO             = '{$slipNo}' ";
        $query .= "     AND REDUCTION_TARGET    = '2' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if (is_array($row)) {
            /**********/
            /* UPDATE */
            /**********/
            $where  = " WHERE ";
            $where .= "     SCHOOLCD                = '".(sprintf("%012d", SCHOOLCD))."' ";
            $where .= "     AND SCHOOL_KIND         = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
            $where .= "     AND YEAR                = '".CTRL_YEAR ."' ";
            $where .= "     AND SLIP_NO             = '{$slipNo}' ";
            $where .= "     AND REDUCTION_TARGET    = '2' ";

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
            $data["REDUCTION_TARGET"][TEXT] = 2;

            $query = Query::insertSQL($data, "REDUCTION_SCHOOL_DAT");
            $result = $db->query($query);
        }

        /*****************************/
        /* REDUCTION_SCHOOL_PLAN_DAT */
        /*****************************/
        $reducSchoolDecFlg = ($reducSchoolFlg == '1' && $row["DEC_FLG_1"] != "1") ? 1 : 0;

        if ($reducSchoolFlg != "1") {
            //計画データ削除
            $query  = " DELETE FROM ";
            $query .= "     REDUCTION_SCHOOL_PLAN_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHOOLCD            = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "     AND SCHOOL_KIND     = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
            $query .= "     AND YEAR            = '".CTRL_YEAR ."' ";
            $query .= "     AND SLIP_NO         = '{$slipNo}' ";
            $query .= "     AND REDUCTION_TARGET = '2' ";
            $db->query($query);

        } else if ($reducSchoolDecFlg == "1") {
            //計画データのある月取得
            $planMonthArray = $db->getCol(knjp722Query::getReducSchoolPlanMonth($model, $slipNo));

            if (get_count($planMonthArray) != 12) {
                // INSERT
                for ($month = 1; $month <= 12; $month++) {
                    if (in_array($month, array_map('intval', $planMonthArray), true)) {
                        continue;
                    }
                    $setMoney = $setDecisionMoney = 0;
                    $setMonth = sprintf("%02d", $month);
                    knjp722Query::insertReducSchoolPlan($db, $model, $slipNo, $schregNo, $setMonth, $setMoney, $setDecisionMoney);
                }
            }

            // UPDATE
            $money = ($money > 0) ? $money : 0;

            $query  = " WITH MAIN AS ( ";
            $query .= "     SELECT ";
            $query .= "         SLIP_NO, ";
            $query .= "         PLAN_YEAR, ";
            $query .= "         PLAN_MONTH, ";
            $query .= "         PAID_LIMIT_MONTH, ";
            $query .= "         {$money} AS MONEY ";
            $query .= "     FROM ";
            $query .= "         COLLECT_SLIP_PLAN_LIMITDATE_DAT LIMIT_DATE ";
            $query .= "     WHERE ";
            $query .= "         SCHOOLCD                = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "         AND SCHOOL_KIND         = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
            $query .= "         AND YEAR                = '".CTRL_YEAR."' ";
            $query .= "         AND SLIP_NO             = '{$slipNo}' ";
            $query .= "         AND SCHREGNO            = '{$schregNo}' ";
            $query .= "         AND PAID_LIMIT_DATE IS NOT NULL ";
            $query .= "         AND PAID_LIMIT_MONTH IS NOT NULL ";
            $query .= " ) ";

            $query .= " SELECT ";
            $query .= "     MAIN.SLIP_NO, ";
            $query .= "     MAIN.PLAN_YEAR, ";
            $query .= "     MAIN.PLAN_MONTH, ";
            $query .= "     CASE WHEN MAIN.MONEY > 0 THEN MAIN.MONEY / COUNT_T.CNT + (CASE WHEN RTRIM(CHAR(MAIN.PLAN_YEAR)) || MAIN.PLAN_MONTH = MAX_M.MAX_MONTH THEN MOD(MAIN.MONEY * COUNT_M.CNT / COUNT_T.CNT, COUNT_M.CNT) ELSE 0 END) ELSE 0 END AS PLAN_MONEY ";
            $query .= " FROM ";
            $query .= "     MAIN ";
            $query .= "     LEFT JOIN (SELECT SLIP_NO, COUNT(*) AS CNT FROM MAIN GROUP BY SLIP_NO) COUNT_T ON MAIN.SLIP_NO = COUNT_T.SLIP_NO ";
            $query .= "     LEFT JOIN (SELECT SLIP_NO, PAID_LIMIT_MONTH, COUNT(*) AS CNT FROM MAIN GROUP BY SLIP_NO, PAID_LIMIT_MONTH) COUNT_M ON MAIN.SLIP_NO = COUNT_M.SLIP_NO AND MAIN.PAID_LIMIT_MONTH = COUNT_M.PAID_LIMIT_MONTH ";
            $query .= "     LEFT JOIN (SELECT SLIP_NO, PAID_LIMIT_MONTH, MAX(RTRIM(CHAR(PLAN_YEAR)) || PLAN_MONTH) AS MAX_MONTH FROM MAIN GROUP BY SLIP_NO, PAID_LIMIT_MONTH) MAX_M ON MAIN.SLIP_NO = MAX_M.SLIP_NO AND MAIN.PAID_LIMIT_MONTH = MAX_M.PAID_LIMIT_MONTH ";
            $query .= " ORDER BY ";
            $query .= "     MAIN.PLAN_YEAR, ";
            $query .= "     MAIN.PLAN_MONTH ";

            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $data = array();
                $data["PLAN_MONEY"][NUMBER]         = $row["PLAN_MONEY"];
                $data["DECISION_MONEY"][NUMBER]     = $row["PLAN_MONEY"];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = "SYSDATE()";

                $where  = " WHERE ";
                $where .= "     SCHOOLCD                = '".sprintf("%012d", SCHOOLCD)."' ";
                $where .= "     AND SCHOOL_KIND         = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
                $where .= "     AND YEAR                = '".CTRL_YEAR."' ";
                $where .= "     AND SLIP_NO             = '{$slipNo}' ";
                $where .= "     AND REDUCTION_TARGET    = '2' ";
                $where .= "     AND SCHREGNO            = '{$schregNo}' ";
                $where .= "     AND PLAN_YEAR           = '{$row["PLAN_YEAR"]}' ";
                $where .= "     AND PLAN_MONTH          = '{$row["PLAN_MONTH"]}' ";

                $query = Query::updateSQL($data, "REDUCTION_SCHOOL_PLAN_DAT", $where);
                $db->query($query);
            }
            $result->free();
        }
    }

    function getRaducRareCase($schregNo) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     REDUCTION_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR                    = '".CTRL_YEAR ."' ";
        $query .= "     AND SCHREGNO            = '{$schregNo}' ";
        $query .= "     AND REDUCTION_TARGET    = '2' ";
        $query .= "     AND REDUC_RARE_CASE_CD  = 'T' ";

        return $query;
    }

    function delReductionDat($schregNo) {
        $query  = " DELETE FROM REDUCTION_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR                = '".CTRL_YEAR ."' AND ";
        $query .= "     SCHREGNO            = '{$schregNo}' AND ";
        $query .= "     REDUCTION_TARGET    = '2' ";

        return $query;
    }

    function planPreDecisionMoneyAllClear($model, $slipNo) {
        $data = array();

        $data["PLAN_MONEY"][NUMBER]             = 0;
        $data["DECISION_MONEY"][NUMBER]         = 0;
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $where .= "     AND YEAR        = '".CTRL_YEAR ."' ";
        $where .= "     AND REDUCTION_TARGET    = '2' ";
        $where .= "     AND SLIP_NO     = '{$slipNo}' ";

        $query = Query::updateSQL($data, "REDUCTION_PLAN_DAT", $where);
        return $query;
    }

    function insertPrefPlan($db, $model, $slipNo, $schregNo, $month, $setMoney, $setDecisionMoney) {
        $data = array();

        $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]      = $model->schoolKindAry[$model->search["GRADE"]];
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["REDUCTION_TARGET"][TEXT] = 2;
        $data["SLIP_NO"][TEXT]          = $slipNo;
        $data["PLAN_YEAR"][TEXT]        = $month < "04" ? CTRL_YEAR + 1 : CTRL_YEAR;
        $data["PLAN_MONTH"][TEXT]       = $month;
        $data["SCHREGNO"][TEXT]         = $schregNo;
        $data["PLAN_MONEY"][NUMBER]     = $setMoney;
        $data["DECISION_MONEY"][NUMBER] = $setDecisionMoney;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($data, "REDUCTION_PLAN_DAT");
        $db->query($query);
    }

    function updatePrefPlan($db, $model, $slipNo, $month, $setMoney, $planCnt, $div, $setDecisionMoney) {
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
        $where .= "     AND REDUCTION_TARGET    = '2' ";
        $where .= "     AND SLIP_NO             = '{$slipNo}' ";
        $where .= "     AND PLAN_YEAR           = '{$setYear}' ";
        $where .= "     AND PLAN_MONTH          = '{$month}' ";

        $query = Query::updateSQL($data, "REDUCTION_PLAN_DAT", $where);
        $db->query($query);
    }

    function updatePrefDecPlan($db, $model, $slipNo, $month, $setDecisionMoney) {
        if (!$setDecisionMoney) {
            return;
        }
        $data = array();

        $data["DECISION_MONEY"][NUMBER] = $setDecisionMoney;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $setYear = $month < "04" ? CTRL_YEAR + 1 : CTRL_YEAR;
        $where  = " WHERE ";
        $where .= "     SCHOOLCD                = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "     AND SCHOOL_KIND         = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $where .= "     AND YEAR                = '".CTRL_YEAR ."' ";
        $where .= "     AND REDUCTION_TARGET    = '2' ";
        $where .= "     AND SLIP_NO             = '{$slipNo}' ";
        $where .= "     AND PLAN_YEAR           = '{$setYear}' ";
        $where .= "     AND PLAN_MONTH          = '{$month}' ";

        $query = Query::updateSQL($data, "REDUCTION_PLAN_DAT", $where);
        $db->query($query);
    }

    function insertReducSchoolPlan($db, $model, $slipNo, $schregNo, $month, $setMoney, $setDecisionMoney) {
        $data = array();

        $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]      = $model->schoolKindAry[$model->search["GRADE"]];
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SLIP_NO"][TEXT]          = $slipNo;
        $data["REDUCTION_TARGET"][TEXT] = 2;
        $data["PLAN_YEAR"][TEXT]        = $month < "04" ? CTRL_YEAR + 1 : CTRL_YEAR;
        $data["PLAN_MONTH"][TEXT]       = $month;
        $data["SCHREGNO"][TEXT]         = $schregNo;
        $data["PLAN_MONEY"][NUMBER]     = $setMoney;
        $data["DECISION_MONEY"][NUMBER] = $setDecisionMoney;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($data, "REDUCTION_SCHOOL_PLAN_DAT");
        $db->query($query);
    }

    function updateReducSchoolPlan($db, $model, $slipNo, $month, $setMoney, $setDecisionMoney) {
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
        $where .= "     AND REDUCTION_TARGET    = '2' ";
        $where .= "     AND PLAN_YEAR           = '{$setYear}' ";
        $where .= "     AND PLAN_MONTH          = '{$month}' ";

        $query = Query::updateSQL($data, "REDUCTION_SCHOOL_PLAN_DAT", $where);
        $db->query($query);
    }

    //減免情報取得
    function selectQueryGenmen($model) {
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
        $query .= "     SCHOLAR_H.SCHREGNO          = '{$model->genmenTransfer["SCHREGNO"]}' ";
        $query .= "     AND SCHOLAR_H.SCHOOLCD      = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOLAR_H.SCHOOL_KIND   = '{$model->genmenTransfer["SCHOOL_KIND"]}' ";
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
    function selectQueryTransfer($model) {
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
                SCHREGNO = '".$model->genmenTransfer["SCHREGNO"]."'
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
                SCHREGNO = '".$model->genmenTransfer["SCHREGNO"]."'
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
                SCHREGNO = '".$model->genmenTransfer["SCHREGNO"]."'
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

    //軽減特殊コード取得
    function selectQuerySpecailCode() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS LABEL, ";
        $query .= "     NAMECD2 || ':' || NAMESPARE1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'P711' AND ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function updatePlanSum($db, $model, $slipNo, $schregNo) {

        $tableArray = array();

        //補助
        $tableArray[0]["TABLE"] = "REDUCTION_PLAN_DAT";
        $tableArray[0]["REDUCTION_TARGET"] = "2";

        //減免
        $tableArray[1]["TABLE"] = "REDUCTION_SCHOOL_PLAN_DAT";
        $tableArray[1]["REDUCTION_TARGET"] = "2";

        foreach ($tableArray as $key => $tableInfo) {
            $query  = " SELECT ";
            $query .= "     PLAN_T.SCHOOLCD, ";
            $query .= "     PLAN_T.SCHOOL_KIND, ";
            $query .= "     PLAN_T.YEAR, ";
            $query .= "     PLAN_T.SLIP_NO, ";
            $query .= "     YEAR(MAX(LIMIT_DATE.PAID_LIMIT_DATE)) AS PLAN_YEAR, ";
            $query .= "     LIMIT_DATE.PAID_LIMIT_MONTH AS PLAN_MONTH, ";
            $query .= "     PLAN_T.SCHREGNO, ";
            $query .= "     SUM(PLAN_T.PLAN_MONEY) AS PLAN_MONEY, ";
            $query .= "     SUM(PLAN_T.DECISION_MONEY) AS DECISION_MONEY ";
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
            $query .= "     AND LIMIT_DATE.PAID_LIMIT_DATE IS NOT NULL ";
            $query .= "     AND LIMIT_DATE.PAID_LIMIT_MONTH IS NOT NULL ";
            $query .= " GROUP BY ";
            $query .= "     PLAN_T.SCHOOLCD, ";
            $query .= "     PLAN_T.SCHOOL_KIND, ";
            $query .= "     PLAN_T.YEAR, ";
            $query .= "     PLAN_T.SCHREGNO, ";
            $query .= "     PLAN_T.SLIP_NO, ";
            $query .= "     LIMIT_DATE.PAID_LIMIT_MONTH ";
            $query .= " HAVING ";
            $query .= "     VALUE(SUM(PLAN_T.PLAN_MONEY), 0) > 0 ";
            $query .= "     OR VALUE(SUM(PLAN_T.DECISION_MONEY), 0) > 0 ";

            $result = $db->query($query);
            $updSumArray = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $updSumArray[] = $row;
            }
            $result->free();

            //金額クリア
            $data = array();
            $data["PLAN_MONEY"][NUMBER]     = 0;
            $data["DECISION_MONEY"][NUMBER] = 0;
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
                $data["PLAN_MONEY"][NUMBER]     = $updVal["PLAN_MONEY"];
                $data["DECISION_MONEY"][NUMBER] = $updVal["DECISION_MONEY"];
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
?>