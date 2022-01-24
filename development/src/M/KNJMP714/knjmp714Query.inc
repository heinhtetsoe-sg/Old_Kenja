<?php

require_once('for_php7.php');

class knjmp714Query extends Query {

    function getYear($model) {
        $query  = " WITH THIS_YEAR(LABEL, VALUE) AS ( ";
        $query .= "     VALUES('".CTRL_YEAR."', '".CTRL_YEAR."'), ";
        $query .= "     ('".(CTRL_YEAR + 1)."', '".(CTRL_YEAR + 1)."') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     LABEL, ";
        $query .= "     VALUE ";
        $query .= " FROM ";
        $query .= "     THIS_YEAR ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //学期
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTER || ':' || SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER < '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組
    function getHrClass($model) {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND GRADE IN (SELECT ";
            $query .= "                      REGD_G.GRADE ";
            $query .= "                   FROM ";
            $query .= "                      SCHREG_REGD_GDAT REGD_G ";
            $query .= "                   WHERE ";
            $query .= "                      REGD_G.YEAR = '{$model->year}' ";
            $query .= "                      AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //グループ
    function getGroupList($model, $groupCd = "") {
        $query .= " SELECT ";
        $query .= "     GD.COLLECT_GRP_CD AS VALUE, ";
        $query .= "     GD.COLLECT_GRP_CD || ':' || GM.COLLECT_GRP_NAME AS LABEL, ";
        $query .= "     SUM(VALUE(CM.COLLECT_M_MONEY, 0)) + SUM(VALUE(CS.COLLECT_S_MONEY, 0)) AS TMONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_GRP_DAT GD ";
        $query .= "     LEFT JOIN COLLECT_GRP_MST GM ON GD.YEAR = GM.YEAR ";
        $query .= "          AND GD.COLLECT_GRP_CD = GM.COLLECT_GRP_CD ";
        $query .= "     LEFT JOIN COLLECT_M_MST CM ON GD.YEAR = CM.YEAR ";
        $query .= "          AND GD.COLLECT_L_CD = CM.COLLECT_L_CD ";
        $query .= "          AND GD.COLLECT_M_CD = CM.COLLECT_M_CD ";
        $query .= "     LEFT JOIN COLLECT_S_MST CS ON GD.YEAR = CS.YEAR ";
        $query .= "          AND GD.COLLECT_L_CD = CS.COLLECT_L_CD ";
        $query .= "          AND GD.COLLECT_M_CD = CS.COLLECT_M_CD ";
        $query .= "          AND GD.COLLECT_S_CD = CS.COLLECT_S_CD ";
        $query .= " WHERE ";
        $query .= "     GD.YEAR = '{$model->year}' ";
        if ($groupCd) {
            $query .= "     AND GD.COLLECT_GRP_CD = '{$groupCd}' ";
        }
        $query .= " GROUP BY ";
        $query .= "     GD.COLLECT_GRP_CD, ";
        $query .= "     GM.COLLECT_GRP_NAME ";
        $query .= " ORDER BY ";
        $query .= "     GD.COLLECT_GRP_CD ";

        return $query;
    }

    //授業料
    function getJugyouryou($model) {
        $query  = " SELECT ";
        $query .= "     COLLECT_L_CD || ':' || COLLECT_M_CD AS VALUE, ";
        $query .= "     COLLECT_L_CD || COLLECT_M_CD || ':' || COLLECT_M_NAME AS LABEL, ";
        $query .= "     COLLECT_M_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_M_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND IS_JUGYOURYOU = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //一覧
    function getSchregList($model) {
        $sdate = str_replace("/", "-", $model->control["学期開始日付"][$model->semester]);
        $edate = str_replace("/", "-", $model->control["学期終了日付"][$model->semester]);
        $query .= " WITH FUKUGAKU AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_DETAIL_COMEBACK_MST ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ), SPORT AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     BASE_REMARK1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     BASE_SEQ = '008' ";
        $query .= " ), ZENSEKI_SPORT AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     HEALTH_AFT_SICKREC_DAT ";
        $query .= " WHERE ";
        $query .= "     SPORT = '1' ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ), PAID_M AS ( ";
        $query .= " SELECT ";
        $query .= "     SLIP_NO, ";
        $query .= "     SCHREGNO, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_MONEY_PAID_M_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND VALUE(PAID_MONEY, 0) > 0 ";
        $query .= " GROUP BY ";
        $query .= "     SLIP_NO, ";
        $query .= "     SCHREGNO ";
        $query .= " ), PAID_S AS ( ";
        $query .= " SELECT ";
        $query .= "     SLIP_NO, ";
        $query .= "     SCHREGNO, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_MONEY_PAID_S_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND VALUE(PAID_MONEY, 0) > 0 ";
        $query .= " GROUP BY ";
        $query .= "     SLIP_NO, ";
        $query .= "     SCHREGNO ";
        $query .= " ), MONEY_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SLIP.SLIP_NO, ";
        $query .= "     SLIP.COLLECT_GRP_CD, ";
        $query .= "     SLIP.SCHREGNO, ";
        $query .= "     MAX(CASE WHEN COL_MM.IS_JUGYOURYOU = '1' THEN SLIP_MD.COLLECT_L_CD ||  ':' || SLIP_MD.COLLECT_M_CD ELSE '' END) AS JUGYOU, ";
        $query .= "     MAX(CASE WHEN COL_MM.IS_JUGYOURYOU = '1' THEN SLIP_MD.COLLECT_CNT ELSE 0 END) AS COLLECT_CNT, ";
        $query .= "     SUM(CASE WHEN VALUE(COL_MM.IS_JUGYOURYOU, '0') = '0' THEN 1 ELSE 0 END) AS SYOKEIHI_CNT, ";
        $query .= "     SUM(CASE WHEN COL_MM.IS_JUGYOURYOU = '1' THEN 1 ELSE 0 END) AS JUGYOU_CNT, ";
        $query .= "     SUM(CASE WHEN VALUE(COL_MM.IS_JUGYOURYOU, '0') = '0' THEN VALUE(SLIP_MD.MONEY_DUE, 0) * VALUE(SLIP_MD.COLLECT_CNT, 0) ELSE 0 END) AS SYOKEIHI_DISP, ";
        $query .= "     SUM(CASE WHEN COL_MM.IS_JUGYOURYOU = '1' THEN VALUE(SLIP_MD.MONEY_DUE, 0) * VALUE(SLIP_MD.COLLECT_CNT, 0) ELSE 0 END) AS JUGYOURYOU_DISP, ";
        $query .= "     SUM(VALUE(SLIP_MD.MONEY_DUE, 0) * VALUE(SLIP_MD.COLLECT_CNT, 0)) AS T_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_DAT SLIP ";
        $query .= "     LEFT JOIN COLLECT_SLIP_MONEY_DUE_M_DAT SLIP_MD ON SLIP.YEAR = SLIP_MD.YEAR ";
        $query .= "          AND SLIP.SCHREGNO = SLIP_MD.SCHREGNO ";
        $query .= "          AND SLIP.SLIP_NO = SLIP_MD.SLIP_NO ";
        $query .= "     LEFT JOIN COLLECT_M_MST COL_MM ON SLIP_MD.YEAR = COL_MM.YEAR ";
        $query .= "          AND SLIP_MD.COLLECT_L_CD = COL_MM.COLLECT_L_CD ";
        $query .= "          AND SLIP_MD.COLLECT_M_CD = COL_MM.COLLECT_M_CD ";
        $query .= " WHERE ";
        $query .= "     SLIP.YEAR = '{$model->year}' ";
        $query .= "     AND SLIP.CANCEL_DATE IS NULL ";
        if ($model->seikyuuDisp == "1") {
            $query .= "     AND NOT EXISTS( ";
            $query .= "             SELECT ";
            $query .= "                 'X' ";
            $query .= "             FROM ";
            $query .= "                 COLLECT_SLIP_PRINT_HIST_DAT SLIP_PRINT ";
            $query .= "             WHERE ";
            $query .= "                 SLIP.YEAR = SLIP_PRINT.YEAR ";
            $query .= "                 AND SLIP.SCHREGNO = SLIP_PRINT.SCHREGNO ";
            $query .= "                 AND SLIP.SLIP_NO = SLIP_PRINT.SLIP_NO ";
            $query .= "                 AND VALUE(SLIP_PRINT.CLAIM_NONE_FLG, '0') = '0' ";
            $query .= "             ) ";
        }
        $query .= " GROUP BY ";
        $query .= "     SLIP.SLIP_NO, ";
        $query .= "     SLIP.COLLECT_GRP_CD, ";
        $query .= "     SLIP.SCHREGNO ";
        $query .= " ), CREDIT_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(T1.CREDITS) AS CREDITS ";
        $query .= " FROM ";
        $query .= "     ( ";
        $query .= "     SELECT ";
        $query .= "         SUB_STD.SCHREGNO, ";
        $query .= "         SUB_STD.CLASSCD, ";
        $query .= "         SUB_STD.SCHOOL_KIND, ";
        $query .= "         SUB_STD.CURRICULUM_CD, ";
        $query .= "         SUB_STD.SUBCLASSCD, ";
        $query .= "         SUB_STD.CLASSCD, ";
        $query .= "         SUM(VALUE(CRE.CREDITS, 0)) AS CREDITS ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT REGD ";
        $query .= "         INNER JOIN SUBCLASS_STD_SELECT_DAT SUB_STD ON SUB_STD.YEAR = '{$model->year}' ";
        $query .= "              AND REGD.SCHREGNO = SUB_STD.SCHREGNO ";
        $query .= "         LEFT JOIN CREDIT_MST CRE ON CRE.YEAR = '{$model->year}' ";
        $query .= "              AND REGD.COURSECD = CRE.COURSECD ";
        $query .= "              AND REGD.MAJORCD = CRE.MAJORCD ";
        $query .= "              AND REGD.GRADE = CRE.GRADE ";
        $query .= "              AND REGD.COURSECODE = CRE.COURSECODE ";
        $query .= "              AND SUB_STD.CLASSCD = CRE.CLASSCD ";
        $query .= "              AND SUB_STD.SCHOOL_KIND = CRE.SCHOOL_KIND ";
        $query .= "              AND SUB_STD.CURRICULUM_CD = CRE.CURRICULUM_CD ";
        $query .= "              AND SUB_STD.SUBCLASSCD = CRE.SUBCLASSCD ";
        $query .= "     WHERE ";
        $query .= "         REGD.YEAR = '{$model->year}' ";
        $query .= "         AND REGD.SEMESTER = '{$model->semester}' ";
        $query .= "     GROUP BY ";
        $query .= "         SUB_STD.SCHREGNO, ";
        $query .= "         SUB_STD.CLASSCD, ";
        $query .= "         SUB_STD.SCHOOL_KIND, ";
        $query .= "         SUB_STD.CURRICULUM_CD, ";
        $query .= "         SUB_STD.SUBCLASSCD, ";
        $query .= "         SUB_STD.CLASSCD ";
        $query .= "     ) T1 ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '1' AS SORT_CD, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     '' AS SLIP_NO, ";
        $query .= "     '' AS COLLECT_GRP_CD, ";
        $query .= "     '' AS JUGYOU, ";
        $query .= "     HDAT.HR_NAME, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     A001.NAME1 AS INOUT_NAME, ";
        $query .= "     A002.NAME1 AS ENT_NAME, ";
        $query .= "     VALUE(CREDIT_T.CREDITS, 0) AS CREDITS, ";
        $query .= "     0 AS T_MONEY, ";
        $query .= "     0 AS SYOKEIHI_CNT, ";
        $query .= "     0 AS JUGYOU_CNT, ";
        $query .= "     0 AS SYOKEIHI_DISP, ";
        $query .= "     0 AS JUGYOURYOU_DISP, ";
        $query .= "     0 AS COLLECT_CNT, ";
        $query .= "     FUKUGAKU.CNT AS FUKUGAKU, ";
        $query .= "     SPORT.BASE_REMARK1 AS SPORT, ";
        $query .= "     ZENSEKI_SPORT.CNT AS ZENSEKI_SPORT, ";
        $query .= "     0 AS PAIDMONEY_M, ";
        $query .= "     0 AS PAIDMONEY_S, ";
        $query .= "     0 AS CLAIM_CNT, ";
        $query .= "     GDAT.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = REGD.YEAR ";
        $query .= "          AND HDAT.SEMESTER = REGD.SEMESTER ";
        $query .= "          AND HDAT.GRADE = REGD.GRADE ";
        $query .= "          AND HDAT.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
        if ($model->dataDiv == "3") {
            $query .= "           AND BASE.ENT_DIV = '4' ";
            $query .= "           AND BASE.ENT_DATE BETWEEN '{$sdate}' AND '{$edate}' ";
        }
        if ($model->dataDiv == "2") {
            $query .= "           AND BASE.INOUTCD IN ('7', '9') ";
        }
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR AND GDAT.GRADE = REGD.GRADE ";
        $query .= "     LEFT JOIN NAME_MST A001 ON A001.NAMECD1 = 'A001' ";
        $query .= "          AND A001.NAMECD2 = BASE.INOUTCD ";
        $query .= "     LEFT JOIN NAME_MST A002 ON A002.NAMECD1 = 'A002' ";
        $query .= "          AND A002.NAMECD2 = BASE.ENT_DIV ";
        $query .= "     LEFT JOIN FUKUGAKU ON FUKUGAKU.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN SPORT ON SPORT.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN ZENSEKI_SPORT ON ZENSEKI_SPORT.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN CREDIT_T ON CREDIT_T.SCHREGNO = REGD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '{$model->year}' ";
        $query .= "     AND REGD.SEMESTER = '{$model->semester}' ";
        if ($model->dataDiv == "1") {
            $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '{$model->hr_class}' ";
        }
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '2' AS SORT_CD, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     MONEY_T.SLIP_NO, ";
        $query .= "     MONEY_T.COLLECT_GRP_CD, ";
        $query .= "     MONEY_T.JUGYOU, ";
        $query .= "     HDAT.HR_NAME, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     A001.NAME1 AS INOUT_NAME, ";
        $query .= "     A002.NAME1 AS ENT_NAME, ";
        $query .= "     VALUE(CREDIT_T.CREDITS, 0) AS CREDITS, ";
        $query .= "     VALUE(MONEY_T.T_MONEY, 0) AS T_MONEY, ";
        $query .= "     MONEY_T.SYOKEIHI_CNT, ";
        $query .= "     MONEY_T.JUGYOU_CNT, ";
        $query .= "     MONEY_T.SYOKEIHI_DISP, ";
        $query .= "     MONEY_T.JUGYOURYOU_DISP, ";
        $query .= "     MONEY_T.COLLECT_CNT, ";
        $query .= "     FUKUGAKU.CNT AS FUKUGAKU, ";
        $query .= "     SPORT.BASE_REMARK1 AS SPORT, ";
        $query .= "     ZENSEKI_SPORT.CNT AS ZENSEKI_SPORT, ";
        $query .= "     PAID_M.CNT AS PAIDMONEY_M, ";
        $query .= "     PAID_S.CNT AS PAIDMONEY_S, ";
        $query .= "     CLAIM.CLAIM_CNT, ";
        $query .= "     GDAT.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = REGD.YEAR ";
        $query .= "          AND HDAT.SEMESTER = REGD.SEMESTER ";
        $query .= "          AND HDAT.GRADE = REGD.GRADE ";
        $query .= "          AND HDAT.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
        if ($model->dataDiv == "3") {
            $query .= "           AND BASE.ENT_DIV = '4' ";
            $query .= "           AND BASE.ENT_DATE BETWEEN '{$sdate}' AND '{$edate}' ";
        }
        if ($model->dataDiv == "2") {
            $query .= "           AND BASE.INOUTCD IN ('7', '9') ";
        }
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR AND GDAT.GRADE = REGD.GRADE ";
        $query .= "     LEFT JOIN NAME_MST A001 ON A001.NAMECD1 = 'A001' ";
        $query .= "          AND A001.NAMECD2 = BASE.INOUTCD ";
        $query .= "     LEFT JOIN NAME_MST A002 ON A002.NAMECD1 = 'A002' ";
        $query .= "          AND A002.NAMECD2 = BASE.ENT_DIV ";
        $query .= "     LEFT JOIN FUKUGAKU ON FUKUGAKU.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN SPORT ON SPORT.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN ZENSEKI_SPORT ON ZENSEKI_SPORT.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     INNER JOIN MONEY_T ON MONEY_T.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN PAID_M ON MONEY_T.SCHREGNO = PAID_M.SCHREGNO ";
        $query .= "          AND MONEY_T.SLIP_NO = PAID_M.SLIP_NO ";
        $query .= "     LEFT JOIN PAID_S ON MONEY_T.SCHREGNO = PAID_S.SCHREGNO ";
        $query .= "          AND MONEY_T.SLIP_NO = PAID_S.SLIP_NO ";
        $query .= "     LEFT JOIN CREDIT_T ON CREDIT_T.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN ( ";
        $query .= "             SELECT ";
        $query .= "                 SLIP_PRINT.SLIP_NO, ";
        $query .= "                 COUNT(*) AS CLAIM_CNT ";
        $query .= "             FROM ";
        $query .= "                 COLLECT_SLIP_PRINT_HIST_DAT SLIP_PRINT ";
        $query .= "             WHERE ";
        $query .= "                 SLIP_PRINT.YEAR = '{$model->year}' ";
        $query .= "                 AND VALUE(SLIP_PRINT.CLAIM_NONE_FLG, '0') = '0' ";
        $query .= "             GROUP BY ";
        $query .= "                 SLIP_PRINT.SLIP_NO ";
        $query .= "             ) CLAIM ON MONEY_T.SLIP_NO = CLAIM.SLIP_NO ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '{$model->year}' ";
        $query .= "     AND REGD.SEMESTER = '{$model->semester}' ";
        if ($model->dataDiv == "1") {
            $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '{$model->hr_class}' ";
        }
        if ($model->seikyuuDisp == "2") {
            $query .= "     AND VALUE(PAID_M.CNT, 0) = 0 ";
            $query .= "     AND VALUE(PAID_S.CNT, 0) = 0 ";
        }
        $query .= " ORDER BY ";
        $query .= "     ATTENDNO, ";
        $query .= "     SORT_CD, ";
        $query .= "     SLIP_NO DESC ";

        return $query;
    }

    //生徒情報
    function getSchregInfo($model) {
        $query .= " SELECT ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     HDAT.HR_NAME, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = REGD.YEAR ";
        $query .= "          AND HDAT.SEMESTER = REGD.SEMESTER ";
        $query .= "          AND HDAT.GRADE = REGD.GRADE ";
        $query .= "          AND HDAT.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '{$model->year}' ";
        $query .= "     AND REGD.SEMESTER = '{$model->semester}' ";
        $query .= "     AND REGD.SCHREGNO = '{$model->sendSchregNo}' ";

        return $query;
    }

    //生徒伝票明細
    function getMeisaiData($model) {
        $query  = " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SLIP.SLIP_NO, ";
        $query .= "     SLIP_MD.SEQ, ";
        $query .= "     MAX(CM.COLLECT_M_NAME) AS COLLECT_M_NAME, ";
        $query .= "     SUM(CASE WHEN SLIP_SD.COLLECT_S_CD IS NULL ";
        $query .= "              THEN VALUE(SLIP_MD.MONEY_DUE, 0) * VALUE(SLIP_MD.COLLECT_CNT, 0) ";
        $query .= "              ELSE VALUE(SLIP_SD.MONEY_DUE, 0) * VALUE(SLIP_SD.COLLECT_CNT, 0) ";
        $query .= "         END) AS TMONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_DAT SLIP ";
        $query .= "     LEFT JOIN COLLECT_SLIP_MONEY_DUE_M_DAT SLIP_MD ON SLIP.YEAR = SLIP_MD.YEAR ";
        $query .= "          AND SLIP.SLIP_NO = SLIP_MD.SLIP_NO ";
        $query .= "     LEFT JOIN COLLECT_M_MST CM ON SLIP_MD.YEAR = CM.YEAR ";
        $query .= "          AND SLIP_MD.COLLECT_L_CD = CM.COLLECT_L_CD ";
        $query .= "          AND SLIP_MD.COLLECT_M_CD = CM.COLLECT_M_CD ";
        $query .= "     LEFT JOIN COLLECT_SLIP_MONEY_DUE_S_DAT SLIP_SD ON SLIP_MD.YEAR = SLIP_SD.YEAR ";
        $query .= "          AND SLIP_MD.SLIP_NO = SLIP_SD.SLIP_NO ";
        $query .= "          AND SLIP_MD.SEQ = SLIP_SD.MSEQ ";
        $query .= " WHERE ";
        $query .= "     SLIP.YEAR = '{$model->year}' ";
        $query .= "     AND SLIP.SLIP_NO = '{$model->sendSlipNo}' ";
        $query .= "     AND SLIP.SCHREGNO = '{$model->sendSchregNo}' ";
        $query .= " GROUP BY ";
        $query .= "     SLIP.SLIP_NO, ";
        $query .= "     SLIP_MD.SEQ ";
        $query .= " ), TOTAL_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SLIP_NO, ";
        $query .= "     '' AS SEQ, ";
        $query .= "     '合計' AS COLLECT_M_NAME, ";
        $query .= "     COUNT(*) AS CNT, ";
        $query .= "     SUM(VALUE(TMONEY, 0)) AS TMONEY ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " GROUP BY ";
        $query .= "     SLIP_NO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '1' AS ORDERCD, ";
        $query .= "     MAIN_T.SLIP_NO, ";
        $query .= "     MAIN_T.SEQ, ";
        $query .= "     MAIN_T.COLLECT_M_NAME, ";
        $query .= "     TOTAL_T.CNT, ";
        $query .= "     MAIN_T.TMONEY ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= "     LEFT JOIN TOTAL_T ON MAIN_T.SLIP_NO = TOTAL_T.SLIP_NO ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '2' AS ORDERCD, ";
        $query .= "     TOTAL_T.SLIP_NO, ";
        $query .= "     TOTAL_T.SEQ, ";
        $query .= "     TOTAL_T.COLLECT_M_NAME, ";
        $query .= "     TOTAL_T.CNT, ";
        $query .= "     TOTAL_T.TMONEY ";
        $query .= " FROM ";
        $query .= "     TOTAL_T ";
        $query .= " ORDER BY ";
        $query .= "     SLIP_NO, ";
        $query .= "     ORDERCD, ";
        $query .= "     SEQ ";

        return $query;
    }

    //伝票番号取得
    function getSlipNo($model, $schregNo) {
        $query  = " SELECT ";
        $query .= "     MAX(SLIP.SLIP_NO) AS SLIP_NO ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_DAT SLIP ";
        $query .= " WHERE ";
        $query .= "     SLIP.YEAR = '{$model->year}' ";
        $query .= "     AND SLIP.SCHREGNO = '{$schregNo}' ";
        $query .= "     AND SLIP.CANCEL_DATE IS NULL ";
        $query .= "     AND NOT EXISTS( ";
        $query .= "             SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 COLLECT_SLIP_PRINT_HIST_DAT SLIP_PRINT ";
        $query .= "             WHERE ";
        $query .= "                 SLIP.YEAR = SLIP_PRINT.YEAR ";
        $query .= "                 AND SLIP.SCHREGNO = SLIP_PRINT.SCHREGNO ";
        $query .= "                 AND SLIP.SLIP_NO = SLIP_PRINT.SLIP_NO ";
        $query .= "                 AND VALUE(SLIP_PRINT.CLAIM_NONE_FLG, '0') = '0' ";
        $query .= "             ) ";

        return $query;
    }

    //MAX伝票番号
    function getMaxSlipNo($model) {
        $query  = " SELECT ";
        $query .= "     MAX(SLIP.SLIP_NO) AS SLIP_NO ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_DAT SLIP ";
        $query .= " WHERE ";
        $query .= "     SLIP.YEAR = '{$model->year}' ";

        return $query;
    }

    //データ作成
    function getInsertQuery($db, $model, $schregNo, $slipNo, $groupCd, $jugyouLM, $collectCnt) {

        //新規作成FLG
        $isNewData = true;

        //自身の未請求MAX伝票番号なし
        if ($slipNo == "") {
            //全体でのMAX伝票番号
            $query = knjmp714Query::getMaxSlipNo($model);
            $slipNo = $db->getOne($query);
        } else {
            $isNewData = false;
        }

        //自身/全体での未請求MAX伝票番号なし
        if ($slipNo == "") {
            //年度＋6桁
            $slipNo = $model->year."000001";
        } else if ($isNewData) {
            $slipNo = $slipNo + 1;
        }

        //伝票データ作成
        if ($isNewData) {
            $dataG = array();
            $dataG["YEAR"][TEXT]            = $model->year;
            $dataG["SLIP_NO"][TEXT]         = $slipNo;
            $dataG["SCHREGNO"][TEXT]        = $schregNo;
            $dataG["PAY_DIV"][TEXT]         = "1";
            $dataG["SLIP_DATE"][FUNC]       = "date(sysdate())";
            $dataG["SLIP_STAFFCD"][TEXT]    = STAFFCD;
            $dataG["COLLECT_GRP_CD"][TEXT]  = $groupCd;
            $dataG["REGISTERCD"][TEXT]      = STAFFCD;
            $dataG["UPDATED"][FUNC]         = "sysdate()";

            $query = Query::insertSQL($dataG, "COLLECT_SLIP_DAT");
            $db->query($query);
        } else {
            $dataG["COLLECT_GRP_CD"][TEXT]  = $groupCd;
            $dataG["REGISTERCD"][TEXT]      = STAFFCD;
            $dataG["UPDATED"][FUNC]         = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR = '{$model->year}' ";
            $where .= "     AND SLIP_NO = '{$slipNo}' ";

            $query = Query::updateSQL($dataG, "COLLECT_SLIP_DAT", $where);
            $db->query($query);
        }

        //削除
        $query  = " DELETE FROM ";
        $query .= "     COLLECT_SLIP_MONEY_DUE_M_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SLIP_NO = '{$slipNo}' ";
        $db->query($query);

        //削除
        $query  = " DELETE FROM ";
        $query .= "     COLLECT_SLIP_MONEY_DUE_S_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SLIP_NO = '{$slipNo}' ";
        $db->query($query);

        //伝票SEQ番号
        $slipMSeq = "00";
        $slipSSeq = "00";

        //諸経費作成
        if ($groupCd) {
            $query = knjmp714Query::getInsGroupData($model, $groupCd);
            $result = $db->query($query);
            $befLMcd = "";
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

                if ($befLMcd != $row["COLLECT_L_CD"].$row["COLLECT_M_CD"]) {
                    $slipMSeq = sprintf("%02d", ($slipMSeq + 1));

                    //入金予定項目データ
                    $dataM = array();
                    $dataM["YEAR"][TEXT]            = $model->year;
                    $dataM["SLIP_NO"][TEXT]         = $slipNo;
                    $dataM["SEQ"][TEXT]             = $slipMSeq;
                    $dataM["SCHREGNO"][TEXT]        = $schregNo;
                    $dataM["COLLECT_L_CD"][TEXT]    = $row["COLLECT_L_CD"];
                    $dataM["COLLECT_M_CD"][TEXT]    = $row["COLLECT_M_CD"];
                    $dataM["MONEY_DUE"][NUMBER]     = $row["COLLECT_M_MONEY"];
                    $dataM["COLLECT_CNT"][NUMBER]   = "1";
                    $dataM["REGISTERCD"][TEXT]      = STAFFCD;
                    $dataM["UPDATED"][FUNC]         = "sysdate()";

                    $query = Query::insertSQL($dataM, "COLLECT_SLIP_MONEY_DUE_M_DAT");
                    $db->query($query);

                    $slipSSeq = "00";
                }

                if ($row["COLLECT_S_CD"] > "00") {

                    $slipSSeq = sprintf("%02d", ($slipSSeq + 1));
                    //入金予定細目データ
                    $dataS = array();
                    $dataS["YEAR"][TEXT]            = $model->year;
                    $dataS["SLIP_NO"][TEXT]         = $slipNo;
                    $dataS["MSEQ"][TEXT]            = $slipMSeq;
                    $dataS["SSEQ"][TEXT]            = $slipSSeq;
                    $dataS["SCHREGNO"][TEXT]        = $schregNo;
                    $dataS["COLLECT_L_CD"][TEXT]    = $row["COLLECT_L_CD"];
                    $dataS["COLLECT_M_CD"][TEXT]    = $row["COLLECT_M_CD"];
                    $dataS["COLLECT_S_CD"][TEXT]    = $row["COLLECT_S_CD"];
                    $dataS["MONEY_DUE"][NUMBER]     = $row["COLLECT_S_MONEY"];
                    $dataS["COLLECT_CNT"][NUMBER]   = "1";
                    $dataS["REGISTERCD"][TEXT]      = STAFFCD;
                    $dataS["UPDATED"][FUNC]         = "sysdate()";

                    $query = Query::insertSQL($dataS, "COLLECT_SLIP_MONEY_DUE_S_DAT");
                    $db->query($query);
                }
                $befLMcd = $row["COLLECT_L_CD"].$row["COLLECT_M_CD"];
            }
            $result->free();
        }
        if ($jugyouLM) {
            list($lcd, $mcd) = explode(":", $jugyouLM);
            $slipMSeq = sprintf("%02d", ($slipMSeq + 1));

            //入金予定項目データ
            $dataM = array();
            $dataM["YEAR"][TEXT]            = $model->year;
            $dataM["SLIP_NO"][TEXT]         = $slipNo;
            $dataM["SEQ"][TEXT]             = $slipMSeq;
            $dataM["SCHREGNO"][TEXT]        = $schregNo;
            $dataM["COLLECT_L_CD"][TEXT]    = $lcd;
            $dataM["COLLECT_M_CD"][TEXT]    = $mcd;
            $dataM["MONEY_DUE"][NUMBER]     = $model->jugyouRyouArray[$jugyouLM];
            $dataM["COLLECT_CNT"][NUMBER]   = $collectCnt;
            $dataM["REGISTERCD"][TEXT]      = STAFFCD;
            $dataM["UPDATED"][FUNC]         = "sysdate()";

            $query = Query::insertSQL($dataM, "COLLECT_SLIP_MONEY_DUE_M_DAT");
            $db->query($query);
        }
    }

    //グループデータ取得
    function getInsGroupData($model, $groupCd) {
        $query  = " SELECT ";
        $query .= "     GD.COLLECT_GRP_CD, ";
        $query .= "     GD.COLLECT_L_CD, ";
        $query .= "     GD.COLLECT_M_CD, ";
        $query .= "     GD.COLLECT_S_CD, ";
        $query .= "     CM.COLLECT_M_MONEY, ";
        $query .= "     CM.PAY_DIV, ";
        $query .= "     CM.PAY_DATE, ";
        $query .= "     CS.COLLECT_S_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_GRP_DAT GD ";
        $query .= "     LEFT JOIN COLLECT_GRP_MST GM ON GD.YEAR = GM.YEAR ";
        $query .= "          AND GD.COLLECT_GRP_CD = GM.COLLECT_GRP_CD ";
        $query .= "     LEFT JOIN COLLECT_M_MST CM ON GD.YEAR = CM.YEAR ";
        $query .= "          AND GD.COLLECT_L_CD = CM.COLLECT_L_CD ";
        $query .= "          AND GD.COLLECT_M_CD = CM.COLLECT_M_CD ";
        $query .= "     LEFT JOIN COLLECT_S_MST CS ON GD.YEAR = CS.YEAR ";
        $query .= "          AND GD.COLLECT_L_CD = CS.COLLECT_L_CD ";
        $query .= "          AND GD.COLLECT_M_CD = CS.COLLECT_M_CD ";
        $query .= "          AND GD.COLLECT_S_CD = CS.COLLECT_S_CD ";
        $query .= " WHERE ";
        $query .= "     GD.YEAR = '{$model->year}' ";
        $query .= "     AND GD.COLLECT_GRP_CD = '{$groupCd}' ";
        $query .= " ORDER BY ";
        $query .= "     GD.COLLECT_GRP_CD, ";
        $query .= "     GD.COLLECT_L_CD, ";
        $query .= "     GD.COLLECT_M_CD, ";
        $query .= "     GD.COLLECT_S_CD ";

        return $query;
    }
}
?>
