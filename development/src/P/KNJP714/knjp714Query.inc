<?php

require_once('for_php7.php');

class knjp714Query extends Query {

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

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->year}' ";
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
            $query .= "                      AND REGD_G.SCHOOL_KIND = '" .$model->schoolKind ."') ";
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
        $query .= "          AND GD.SCHOOLCD       = GM.SCHOOLCD ";
        $query .= "          AND GD.SCHOOL_KIND    = GM.SCHOOL_KIND ";
        $query .= "          AND GD.COLLECT_GRP_CD = GM.COLLECT_GRP_CD ";
        $query .= "     LEFT JOIN COLLECT_M_MST CM ON GD.YEAR = CM.YEAR ";
        $query .= "          AND GD.SCHOOLCD     = CM.SCHOOLCD ";
        $query .= "          AND GD.SCHOOL_KIND  = CM.SCHOOL_KIND ";
        $query .= "          AND GD.COLLECT_L_CD = CM.COLLECT_L_CD ";
        $query .= "          AND GD.COLLECT_M_CD = CM.COLLECT_M_CD ";
        $query .= "     LEFT JOIN COLLECT_S_MST CS ON GD.YEAR = CS.YEAR ";
        $query .= "          AND GD.SCHOOLCD     = CS.SCHOOLCD ";
        $query .= "          AND GD.SCHOOL_KIND  = CS.SCHOOL_KIND ";
        $query .= "          AND GD.COLLECT_L_CD = CS.COLLECT_L_CD ";
        $query .= "          AND GD.COLLECT_M_CD = CS.COLLECT_M_CD ";
        $query .= "          AND GD.COLLECT_S_CD = CS.COLLECT_S_CD ";
        $query .= " WHERE ";
        $query .= "     GD.YEAR = '{$model->year}' ";
        $query .= "     AND GD.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND GD.SCHOOL_KIND = '".$model->schoolKind."' ";
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

    //一覧
    function getSchregList($model) {
        $year = $model->year;
        if ($model->dataDiv == "4") {
            $year = $model->year + 1;
        }

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
        $query .= "     COLLECT_SLIP_PLAN_PAID_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '$year' ";
        $query .= "     AND SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "     AND VALUE(PLAN_PAID_MONEY, 0) > 0 ";
        $query .= " GROUP BY ";
        $query .= "     SLIP_NO, ";
        $query .= "     SCHREGNO ";
        $query .= " ), MONEY_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SLIP.SLIP_NO, ";
        $query .= "     SLIP.COLLECT_GRP_CD, ";
        $query .= "     SLIP.SCHREGNO, ";
        $query .= "     MAX(CASE WHEN COL_MM.GAKUNOKIN_DIV = '1' THEN SLIP_MD.COLLECT_L_CD ||  ':' || SLIP_MD.COLLECT_M_CD ELSE '' END) AS JUGYOU, ";
        $query .= "     MAX(CASE WHEN COL_MM.GAKUNOKIN_DIV = '1' THEN SLIP_MD.COLLECT_CNT ELSE 0 END) AS COLLECT_CNT, ";
        $query .= "     SUM(CASE WHEN VALUE(COL_MM.GAKUNOKIN_DIV, '0') = '0' THEN 1 ELSE 0 END) AS SYOKEIHI_CNT, ";
        $query .= "     SUM(CASE WHEN COL_MM.GAKUNOKIN_DIV = '1' THEN 1 ELSE 0 END) AS JUGYOU_CNT, ";
        $query .= "     SUM(CASE WHEN VALUE(COL_MM.GAKUNOKIN_DIV, '0') = '0' THEN VALUE(SLIP_MD.COLLECT_MONEY, 0) * VALUE(SLIP_MD.COLLECT_CNT, 0) ELSE 0 END) AS SYOKEIHI_DISP, ";
        $query .= "     SUM(CASE WHEN COL_MM.GAKUNOKIN_DIV = '1' THEN VALUE(SLIP_MD.COLLECT_MONEY, 0) * VALUE(SLIP_MD.COLLECT_CNT, 0) ELSE 0 END) AS JUGYOURYOU_DISP, ";
        $query .= "     SUM(VALUE(SLIP_MD.COLLECT_MONEY, 0) * VALUE(SLIP_MD.COLLECT_CNT, 0)) AS T_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_DAT SLIP ";
        $query .= "     LEFT JOIN COLLECT_SLIP_M_DAT SLIP_MD ON SLIP.YEAR = SLIP_MD.YEAR ";
        $query .= "          AND SLIP.SCHOOLCD    = SLIP_MD.SCHOOLCD ";
        $query .= "          AND SLIP.SCHOOL_KIND = SLIP_MD.SCHOOL_KIND ";
        $query .= "          AND SLIP.SCHREGNO = SLIP_MD.SCHREGNO ";
        $query .= "          AND SLIP.SLIP_NO = SLIP_MD.SLIP_NO ";
        $query .= "     LEFT JOIN COLLECT_M_MST COL_MM ON SLIP_MD.YEAR = COL_MM.YEAR ";
        $query .= "          AND SLIP_MD.SCHOOLCD     = COL_MM.SCHOOLCD ";
        $query .= "          AND SLIP_MD.SCHOOL_KIND  = COL_MM.SCHOOL_KIND ";
        $query .= "          AND SLIP_MD.COLLECT_L_CD = COL_MM.COLLECT_L_CD ";
        $query .= "          AND SLIP_MD.COLLECT_M_CD = COL_MM.COLLECT_M_CD ";
        $query .= " WHERE ";
        $query .= "     SLIP.YEAR = '$year' ";
        $query .= "     AND SLIP.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SLIP.SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "     AND SLIP.CANCEL_DATE IS NULL ";
        if ($model->seikyuuDisp == "1") {
            $query .= "     AND NOT EXISTS( ";
            $query .= "             SELECT ";
            $query .= "                 'X' ";
            $query .= "             FROM ";
            $query .= "                 COLLECT_SLIP_PRINT_HIST_DAT SLIP_PRINT ";
            $query .= "             WHERE ";
            $query .= "                 SLIP.YEAR = SLIP_PRINT.YEAR ";
            $query .= "                 AND SLIP.SCHOOLCD    = SLIP_PRINT.SCHOOLCD ";
            $query .= "                 AND SLIP.SCHOOL_KIND = SLIP_PRINT.SCHOOL_KIND ";
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
        if ($model->dataDiv == "4") {
            //新入生
            $query .= "         FRESHMAN_DAT FMD ";
            if ($model->Properties["UnUseSubclassStdSelectRirekiDat"] == "1") {
                $query .= "         INNER JOIN SUBCLASS_STD_SELECT_DAT SUB_STD ON SUB_STD.YEAR = FMD.ENTERYEAR ";
                $query .= "              AND SUB_STD.SCHOOL_KIND = '".$model->schoolKind."' ";
                $query .= "              AND FMD.SCHREGNO = SUB_STD.SCHREGNO ";
            } else {
                $query .= "         INNER JOIN ( ";
                $query .= "             SELECT ";
                $query .= "                 RIREKI.* ";
                $query .= "             FROM ";
                $query .= "                 SUBCLASS_STD_SELECT_RIREKI_DAT RIREKI, ";
                $query .= "                 (SELECT ";
                $query .= "                      MAX_T.YEAR, ";
                $query .= "                      MAX_T.SCHREGNO, ";
                $query .= "                      MAX(MAX_T.RIREKI_CODE) AS RIREKI_CODE ";
                $query .= "                  FROM ";
                $query .= "                      SUBCLASS_STD_SELECT_RIREKI_DAT MAX_T ";
                $query .= "                  WHERE ";
                $query .= "                      MAX_T.YEAR = '$year' ";
                $query .= "                      AND MAX_T.SCHOOL_KIND = '".$model->schoolKind."' ";
                $query .= "                  GROUP BY ";
                $query .= "                      MAX_T.YEAR, ";
                $query .= "                      MAX_T.SCHREGNO ";
                $query .= "                 ) RIREKI_MAX ";
                $query .= "             WHERE ";
                $query .= "                 RIREKI.YEAR = RIREKI_MAX.YEAR ";
                $query .= "                 AND RIREKI.SCHOOL_KIND = '".$model->schoolKind."' ";
                $query .= "                 AND RIREKI.SCHREGNO = RIREKI_MAX.SCHREGNO ";
                $query .= "                 AND RIREKI.RIREKI_CODE = RIREKI_MAX.RIREKI_CODE ";
                $query .= "         ) SUB_STD ON SUB_STD.YEAR = FMD.ENTERYEAR ";
                $query .= "              AND SUB_STD.SCHOOL_KIND = '".$model->schoolKind."' ";
                $query .= "              AND FMD.SCHREGNO = SUB_STD.SCHREGNO ";
            }
            $query .= "         LEFT JOIN CREDIT_MST CRE ON CRE.YEAR = FMD.ENTERYEAR ";
            $query .= "              AND FMD.COURSECD = CRE.COURSECD ";
            $query .= "              AND FMD.MAJORCD = CRE.MAJORCD ";
            $query .= "              AND FMD.GRADE = CRE.GRADE ";
            $query .= "              AND FMD.COURSECODE = CRE.COURSECODE ";
            $query .= "              AND SUB_STD.CLASSCD = CRE.CLASSCD ";
            $query .= "              AND SUB_STD.SCHOOL_KIND = CRE.SCHOOL_KIND ";
            $query .= "              AND SUB_STD.CURRICULUM_CD = CRE.CURRICULUM_CD ";
            $query .= "              AND SUB_STD.SUBCLASSCD = CRE.SUBCLASSCD ";
            $query .= "     WHERE ";
            $query .= "         FMD.ENTERYEAR = '$year' ";
            $query .= "     GROUP BY ";
            $query .= "         SUB_STD.SCHREGNO, ";
            $query .= "         SUB_STD.CLASSCD, ";
            $query .= "         SUB_STD.SCHOOL_KIND, ";
            $query .= "         SUB_STD.CURRICULUM_CD, ";
            $query .= "         SUB_STD.SUBCLASSCD, ";
            $query .= "         SUB_STD.CLASSCD ";
            $query .= "     ) T1 ";
        } else{
            $query .= "         SCHREG_REGD_DAT REGD ";
            if ($model->Properties["UnUseSubclassStdSelectRirekiDat"] == "1") {
                $query .= "         INNER JOIN SUBCLASS_STD_SELECT_DAT SUB_STD ON SUB_STD.YEAR = '$year' ";
                $query .= "              AND SUB_STD.SCHOOL_KIND = '".$model->schoolKind."' ";
                $query .= "              AND REGD.SCHREGNO = SUB_STD.SCHREGNO ";
            } else {
                $query .= "         INNER JOIN ( ";
                $query .= "             SELECT ";
                $query .= "                 RIREKI.* ";
                $query .= "             FROM ";
                $query .= "                 SUBCLASS_STD_SELECT_RIREKI_DAT RIREKI, ";
                $query .= "                 (SELECT ";
                $query .= "                      MAX_T.YEAR, ";
                $query .= "                      MAX_T.SCHREGNO, ";
                $query .= "                      MAX(MAX_T.RIREKI_CODE) AS RIREKI_CODE ";
                $query .= "                  FROM ";
                $query .= "                      SUBCLASS_STD_SELECT_RIREKI_DAT MAX_T ";
                $query .= "                  WHERE ";
                $query .= "                      MAX_T.YEAR = '$year' ";
                $query .= "                      AND MAX_T.SCHOOL_KIND = '".$model->schoolKind."' ";
                $query .= "                  GROUP BY ";
                $query .= "                      MAX_T.YEAR, ";
                $query .= "                      MAX_T.SCHREGNO ";
                $query .= "                 ) RIREKI_MAX ";
                $query .= "             WHERE ";
                $query .= "                 RIREKI.YEAR = RIREKI_MAX.YEAR ";
                $query .= "                 AND RIREKI.SCHOOL_KIND = '".$model->schoolKind."' ";
                $query .= "                 AND RIREKI.SCHREGNO = RIREKI_MAX.SCHREGNO ";
                $query .= "                 AND RIREKI.RIREKI_CODE = RIREKI_MAX.RIREKI_CODE ";
                $query .= "         ) SUB_STD ON SUB_STD.YEAR = '$year' ";
                $query .= "              AND SUB_STD.SCHOOL_KIND = '".$model->schoolKind."' ";
                $query .= "              AND REGD.SCHREGNO = SUB_STD.SCHREGNO ";
            }
            $query .= "         LEFT JOIN CREDIT_MST CRE ON CRE.YEAR = '$year' ";
            $query .= "              AND REGD.COURSECD = CRE.COURSECD ";
            $query .= "              AND REGD.MAJORCD = CRE.MAJORCD ";
            $query .= "              AND REGD.GRADE = CRE.GRADE ";
            $query .= "              AND REGD.COURSECODE = CRE.COURSECODE ";
            $query .= "              AND SUB_STD.CLASSCD = CRE.CLASSCD ";
            $query .= "              AND SUB_STD.SCHOOL_KIND = CRE.SCHOOL_KIND ";
            $query .= "              AND SUB_STD.CURRICULUM_CD = CRE.CURRICULUM_CD ";
            $query .= "              AND SUB_STD.SUBCLASSCD = CRE.SUBCLASSCD ";
            $query .= "     WHERE ";
            $query .= "         REGD.YEAR = '$year' ";
            $query .= "         AND REGD.SEMESTER = '{$model->semester}' ";
            $query .= "     GROUP BY ";
            $query .= "         SUB_STD.SCHREGNO, ";
            $query .= "         SUB_STD.CLASSCD, ";
            $query .= "         SUB_STD.SCHOOL_KIND, ";
            $query .= "         SUB_STD.CURRICULUM_CD, ";
            $query .= "         SUB_STD.SUBCLASSCD, ";
            $query .= "         SUB_STD.CLASSCD ";
            $query .= "     ) T1 ";
        }
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '2' AS SORT_CD, ";
        if ($model->dataDiv == "4") {
            $query .= "     FMD.SCHREGNO, ";
        } else{
            $query .= "     REGD.SCHREGNO, ";
        }
        $query .= "     MONEY_T.SLIP_NO, ";
        $query .= "     MONEY_T.COLLECT_GRP_CD, ";
        $query .= "     MONEY_T.JUGYOU, ";
        if ($model->dataDiv == "4") {
            $query .= "     FMD.GRADE || '年' || FMD.HR_CLASS || '組' AS HR_NAME, ";
            $query .= "     FMD.ATTENDNO, ";
            $query .= "     FMD.NAME, ";
        } else{
            $query .= "     HDAT.HR_NAME, ";
            $query .= "     REGD.ATTENDNO, ";
            $query .= "     BASE.NAME, ";
        }
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
        $query .= "     CLAIM.CLAIM_CNT, ";
        $query .= "     GDAT.SCHOOL_KIND ";
        $query .= " FROM ";
        if ($model->dataDiv == "4") {
            //新入生
            $query .= "         FRESHMAN_DAT FMD ";
            $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = FMD.ENTERYEAR AND GDAT.GRADE = FMD.GRADE ";
            $query .= "     LEFT JOIN NAME_MST A001 ON A001.NAMECD1 = 'A001' ";
            $query .= "          AND A001.NAMECD2 = FMD.INOUTCD ";
            $query .= "     LEFT JOIN NAME_MST A002 ON A002.NAMECD1 = 'A002' ";
            $query .= "          AND A002.NAMECD2 = FMD.ENT_DIV ";
            $query .= "     LEFT JOIN FUKUGAKU ON FUKUGAKU.SCHREGNO = FMD.SCHREGNO ";
            $query .= "     LEFT JOIN SPORT ON SPORT.SCHREGNO = FMD.SCHREGNO ";
            $query .= "     LEFT JOIN ZENSEKI_SPORT ON ZENSEKI_SPORT.SCHREGNO = FMD.SCHREGNO ";
            $query .= "     INNER JOIN MONEY_T ON MONEY_T.SCHREGNO = FMD.SCHREGNO ";
            $query .= "     LEFT JOIN PAID_M ON MONEY_T.SCHREGNO = PAID_M.SCHREGNO ";
            $query .= "          AND MONEY_T.SLIP_NO = PAID_M.SLIP_NO ";
            $query .= "     LEFT JOIN CREDIT_T ON CREDIT_T.SCHREGNO = FMD.SCHREGNO ";
            $query .= "     LEFT JOIN ( ";
            $query .= "             SELECT ";
            $query .= "                 SLIP_PRINT.SLIP_NO, ";
            $query .= "                 COUNT(*) AS CLAIM_CNT ";
            $query .= "             FROM ";
            $query .= "                 COLLECT_SLIP_PRINT_HIST_DAT SLIP_PRINT ";
            $query .= "             WHERE ";
            $query .= "                 SLIP_PRINT.YEAR = 'v' ";
            $query .= "                 AND SLIP_PRINT.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "                 AND SLIP_PRINT.SCHOOL_KIND = '".$model->schoolKind."' ";
            $query .= "                 AND VALUE(SLIP_PRINT.CLAIM_NONE_FLG, '0') = '0' ";
            $query .= "             GROUP BY ";
            $query .= "                 SLIP_PRINT.SLIP_NO ";
            $query .= "             ) CLAIM ON MONEY_T.SLIP_NO = CLAIM.SLIP_NO ";
            $query .= " WHERE ";
            $query .= "     FMD.ENTERYEAR = '$year' "; //FRESHMAN_DATの年度は"次年度"。
            $query .= "     AND GDAT.SCHOOL_KIND = '".$model->schoolKind."' ";
            if ($model->seikyuuDisp == "2") {
                $query .= "     AND VALUE(PAID_M.CNT, 0) = 0 ";
            }
        } else{
            $query .= "         SCHREG_REGD_DAT REGD ";
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
            $query .= "     LEFT JOIN CREDIT_T ON CREDIT_T.SCHREGNO = REGD.SCHREGNO ";
            $query .= "     LEFT JOIN ( ";
            $query .= "             SELECT ";
            $query .= "                 SLIP_PRINT.SLIP_NO, ";
            $query .= "                 COUNT(*) AS CLAIM_CNT ";
            $query .= "             FROM ";
            $query .= "                 COLLECT_SLIP_PRINT_HIST_DAT SLIP_PRINT ";
            $query .= "             WHERE ";
            $query .= "                 SLIP_PRINT.YEAR = '$year' ";
            $query .= "                 AND SLIP_PRINT.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "                 AND SLIP_PRINT.SCHOOL_KIND = '".$model->schoolKind."' ";
            $query .= "                 AND VALUE(SLIP_PRINT.CLAIM_NONE_FLG, '0') = '0' ";
            $query .= "             GROUP BY ";
            $query .= "                 SLIP_PRINT.SLIP_NO ";
            $query .= "             ) CLAIM ON MONEY_T.SLIP_NO = CLAIM.SLIP_NO ";
            $query .= " WHERE ";
            $query .= "     REGD.YEAR = '$year' ";
            $query .= "     AND REGD.SEMESTER = '{$model->semester}' ";
            $query .= "     AND GDAT.SCHOOL_KIND = '".$model->schoolKind."' ";
            if ($model->dataDiv == "1") {
                $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '{$model->hr_class}' ";
            }
            if ($model->seikyuuDisp == "2") {
                $query .= "     AND VALUE(PAID_M.CNT, 0) = 0 ";
            }
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

    //生徒情報(新入生)
    function getFreshmanInfo($model) {
        $year = $model->year + 1;
        $query .= " SELECT ";
        $query .= "     FMD.SCHREGNO, ";
        $query .= "     FMD.GRADE || '年' || FMD.HR_CLASS || '組' AS HR_NAME, ";
        $query .= "     FMD.ATTENDNO, ";
        $query .= "     FMD.NAME ";
        $query .= " FROM ";
        $query .= "     FRESHMAN_DAT FMD ";
        $query .= " WHERE ";
        $query .= "     FMD.ENTERYEAR = '$year' ";
        $query .= "     AND FMD.SCHREGNO = '{$model->sendSchregNo}' ";

        return $query;
    }

    //生徒伝票明細
    function getMeisaiData($model) {
        $year = $model->year;
        if ($model->dataDiv == "4") {
            $year = $model->year + 1;
        }
        $query  = " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SLIP.SLIP_NO, ";
        $query .= "     SLIP_MD.COLLECT_L_CD || SLIP_MD.COLLECT_M_CD AS COLLECT_LM_CD, ";
        $query .= "     MAX(CM.COLLECT_M_NAME) AS COLLECT_M_NAME, ";
        $query .= "     SUM(VALUE(SLIP_MD.COLLECT_MONEY, 0) * VALUE(SLIP_MD.COLLECT_CNT, 0)) AS TMONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_DAT SLIP ";
        $query .= "     LEFT JOIN COLLECT_SLIP_M_DAT SLIP_MD ON SLIP.YEAR = SLIP_MD.YEAR ";
        $query .= "          AND SLIP.SCHOOLCD      = SLIP_MD.SCHOOLCD ";
        $query .= "          AND SLIP.SCHOOL_KIND   = SLIP_MD.SCHOOL_KIND ";
        $query .= "          AND SLIP.SLIP_NO       = SLIP_MD.SLIP_NO ";
        $query .= "     LEFT JOIN COLLECT_M_MST CM ON SLIP_MD.YEAR = CM.YEAR ";
        $query .= "          AND SLIP_MD.SCHOOLCD     = CM.SCHOOLCD ";
        $query .= "          AND SLIP_MD.SCHOOL_KIND  = CM.SCHOOL_KIND ";
        $query .= "          AND SLIP_MD.COLLECT_L_CD = CM.COLLECT_L_CD ";
        $query .= "          AND SLIP_MD.COLLECT_M_CD = CM.COLLECT_M_CD ";
        $query .= " WHERE ";
        $query .= "     SLIP.YEAR = '$year' ";
        $query .= "     AND SLIP.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SLIP.SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "     AND SLIP.SLIP_NO = '{$model->sendSlipNo}' ";
        $query .= "     AND SLIP.SCHREGNO = '{$model->sendSchregNo}' ";
        $query .= " GROUP BY ";
        $query .= "     SLIP.SLIP_NO, ";
        $query .= "     SLIP_MD.COLLECT_L_CD, ";
        $query .= "     SLIP_MD.COLLECT_M_CD ";
        $query .= " ), TOTAL_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SLIP_NO, ";
        $query .= "     '' AS COLLECT_LM_CD, ";
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
        $query .= "     MAIN_T.COLLECT_LM_CD, ";
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
        $query .= "     TOTAL_T.COLLECT_LM_CD, ";
        $query .= "     TOTAL_T.COLLECT_M_NAME, ";
        $query .= "     TOTAL_T.CNT, ";
        $query .= "     TOTAL_T.TMONEY ";
        $query .= " FROM ";
        $query .= "     TOTAL_T ";
        $query .= " ORDER BY ";
        $query .= "     SLIP_NO, ";
        $query .= "     ORDERCD, ";
        $query .= "     COLLECT_LM_CD ";

        return $query;
    }
}
?>
