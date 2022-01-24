<?php

require_once('for_php7.php');

class knjp715Query extends Query {
    //年度取得
    function getYear($model) {
        $query  = " WITH THIS_YEAR(LABEL, VALUE) AS ( ";
        $query .= "     VALUES ";
        if ($model->search_div != "1") {
            $query .= "     ('".CTRL_YEAR."', '".CTRL_YEAR."'), ";
        }
        $query .= "     ('".(CTRL_YEAR + 1)."', '".(CTRL_YEAR + 1)."') ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     LABEL, ";
        $query .= "     VALUE ";
        $query .= " FROM ";
        $query .= "     THIS_YEAR ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //校種取得
    function getSchKind($model) {
        if ($model->search_div == "1") {
            $defaultYear = CTRL_YEAR + 1;
            $table = "FRESHMAN_DAT";
            $yearField = "ENTERYEAR";
        } else {
            $defaultYear = CTRL_YEAR;
            $table = "SCHREG_REGD_DAT";
            $yearField = "YEAR";
        }
        $setYear = ($model->mst_field["SELECT_YEAR"]) ? $model->mst_field["SELECT_YEAR"] : $defaultYear;

        $query  = " SELECT DISTINCT ";
        $query .= "     GDAT.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     {$table} T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "          ON T1.{$yearField} = GDAT.YEAR ";
        $query .= "         AND T1.GRADE        = GDAT.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.{$yearField} = '{$setYear}' AND ";
        $query .= "     T1.SCHREGNO     = '{$model->schregno}' ";

        return $query;
    }

    //伝票番号取得
    function getSlipNo($model) {
        $query  = " SELECT ";
        $query .= "     SLIP_NO AS LABEL, ";
        $query .= "     SLIP_NO AS VALUE ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->mst_field["SELECT_YEAR"]."' AND ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolKind."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     SLIP_NO DESC ";

        return $query;
    }

    //伝票情報取得
    function getSlipInfo($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->mst_field["SELECT_YEAR"]."' AND ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolKind."' AND ";
        $query .= "     SLIP_NO     = '".$model->mst_field["SLIP_NO"]."' ";

        return $query;
    }

    //入金日付取得
    function getLimitDate($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PLAN_LIMITDATE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolKind."' AND ";
        $query .= "     YEAR        = '".$model->mst_field["SELECT_YEAR"]."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     SLIP_NO     = '".$model->mst_field["SLIP_NO"]."' ";

        return $query;
    }

    //新入生データ
    function getFreshMan($model) {
        $query  = "(SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     FRESHMAN_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTERYEAR = '{$model->mst_field["SELECT_YEAR"]}') ";

        return $query;
    }

    //生徒の名前の取得
    function getStudentName($model) {
        $tableName = " V_SCHREG_BASE_MST ";
        if ($model->search_div == "1") {
            $tableName = knjp715Query::getFreshMan($model);
        }

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA ";
        $query .= " FROM ";
        $query .= "     {$tableName} T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //単位数
    function getCredits($model) {
        $query  = " SELECT ";
        $query .= "     SUM(T1.CREDITS) AS CREDITS ";
        $query .= " FROM ";
        $query .= "    (SELECT ";
        $query .= "         SUB_STD.SCHREGNO, ";
        $query .= "         SUB_STD.CLASSCD, ";
        $query .= "         SUB_STD.SCHOOL_KIND, ";
        $query .= "         SUB_STD.CURRICULUM_CD, ";
        $query .= "         SUB_STD.SUBCLASSCD, ";
        $query .= "         SUM(VALUE(CRE.CREDITS, 0)) AS CREDITS ";
        $query .= "     FROM ";
        if ($model->search_div == "1") {
            $query .= "         FRESHMAN_DAT SCHREG ";
        } else {
            $query .= "         SCHREG_REGD_DAT SCHREG ";
        }
        $query .= "         INNER JOIN (SELECT ";
        $query .= "                         I_SUB_STD.SCHREGNO, ";
        $query .= "                         I_SUB_STD.CLASSCD, ";
        $query .= "                         I_SUB_STD.SCHOOL_KIND, ";
        $query .= "                         I_SUB_STD.CURRICULUM_CD, ";
        $query .= "                         I_SUB_STD.SUBCLASSCD ";
        $query .= "                     FROM ";
        if ($model->Properties["UnUseSubclassStdSelectRirekiDat"] == "1") {
            $query .= "                     SUBCLASS_STD_SELECT_DAT I_SUB_STD ";
        } else {
            $query .= "                    (SELECT ";
            $query .= "                         RIREKI.* ";
            $query .= "                     FROM ";
            $query .= "                         SUBCLASS_STD_SELECT_RIREKI_DAT RIREKI, ";
            $query .= "                        (SELECT ";
            $query .= "                             MAX_T.YEAR, ";
            $query .= "                             MAX_T.SCHREGNO, ";
            $query .= "                             MAX(MAX_T.RIREKI_CODE) AS RIREKI_CODE ";
            $query .= "                         FROM ";
            $query .= "                             SUBCLASS_STD_SELECT_RIREKI_DAT MAX_T ";
            $query .= "                         WHERE ";
            $query .= "                             MAX_T.YEAR          = '{$model->mst_field["SELECT_YEAR"]}' AND ";
            $query .= "                             MAX_T.SCHOOL_KIND   = '".$model->schoolKind."' ";
            $query .= "                         GROUP BY ";
            $query .= "                             MAX_T.YEAR, ";
            $query .= "                             MAX_T.SCHREGNO ";
            $query .= "                         ) RIREKI_MAX ";
            $query .= "                     WHERE ";
            $query .= "                         RIREKI.YEAR         = RIREKI_MAX.YEAR AND ";
            $query .= "                         RIREKI.SCHOOL_KIND  = '".$model->schoolKind."' AND ";
            $query .= "                         RIREKI.SCHREGNO     = RIREKI_MAX.SCHREGNO AND ";
            $query .= "                         RIREKI.RIREKI_CODE  = RIREKI_MAX.RIREKI_CODE ";
            $query .= "                     ) I_SUB_STD ";
        }
        $query .= "         WHERE ";
        $query .= "             I_SUB_STD.YEAR          = '{$model->mst_field["SELECT_YEAR"]}' AND ";
        $query .= "             I_SUB_STD.SCHOOL_KIND   = '".$model->schoolKind."' ";
        $query .= "         GROUP BY ";
        $query .= "             I_SUB_STD.SCHREGNO, ";
        $query .= "             I_SUB_STD.CLASSCD, ";
        $query .= "             I_SUB_STD.SCHOOL_KIND, ";
        $query .= "             I_SUB_STD.CURRICULUM_CD, ";
        $query .= "             I_SUB_STD.SUBCLASSCD ";
        $query .= "         ) SUB_STD ON SCHREG.SCHREGNO = SUB_STD.SCHREGNO ";
        $query .= "         LEFT JOIN CREDIT_MST CRE ";
        $query .= "              ON CRE.YEAR                = '{$model->mst_field["SELECT_YEAR"]}' ";
        $query .= "             AND SCHREG.COURSECD         = CRE.COURSECD ";
        $query .= "             AND SCHREG.MAJORCD          = CRE.MAJORCD ";
        $query .= "             AND SCHREG.GRADE            = CRE.GRADE ";
        $query .= "             AND SCHREG.COURSECODE       = CRE.COURSECODE ";
        $query .= "             AND SUB_STD.CLASSCD         = CRE.CLASSCD ";
        $query .= "             AND SUB_STD.SCHOOL_KIND     = CRE.SCHOOL_KIND ";
        $query .= "             AND SUB_STD.CURRICULUM_CD   = CRE.CURRICULUM_CD ";
        $query .= "             AND SUB_STD.SUBCLASSCD      = CRE.SUBCLASSCD ";
        $query .= "     WHERE ";
        $query .= "         SCHREG.SCHREGNO = '{$model->schregno}' ";
        if ($model->search_div == "1") {
            $query .= "         AND SCHREG.ENTERYEAR = '{$model->mst_field["SELECT_YEAR"]}' ";
        } else {
            $query .= "         AND SCHREG.YEAR = '{$model->mst_field["SELECT_YEAR"]}' ";
            $query .= "         AND SCHREG.SEMESTER = (SELECT ";
            $query .= "                                 MAX(E1.SEMESTER) ";
            $query .= "                             FROM ";
            $query .= "                                 SCHREG_REGD_DAT E1 ";
            $query .= "                             WHERE ";
            $query .= "                                 E1.YEAR     = '{$model->mst_field["SELECT_YEAR"]}' AND ";
            $query .= "                                 E1.SCHREGNO = '{$model->schregno}') ";
        }
        $query .= "     GROUP BY ";
        $query .= "         SUB_STD.SCHREGNO, ";
        $query .= "         SUB_STD.CLASSCD, ";
        $query .= "         SUB_STD.SCHOOL_KIND, ";
        $query .= "         SUB_STD.CURRICULUM_CD, ";
        $query .= "         SUB_STD.SUBCLASSCD ";
        $query .= "     ) T1 ";

        return $query;
    }

    //生徒伝票
    function getSlipData($model) {
        $query  = " WITH SLIP_M AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHOOLCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         YEAR, ";
        $query .= "         SLIP_NO, ";
        $query .= "         SUM(VALUE(COLLECT_MONEY, 0) * VALUE(COLLECT_CNT, 0)) AS TMONEY ";
        $query .= "     FROM ";
        $query .= "         COLLECT_SLIP_M_DAT ";
        $query .= "     GROUP BY ";
        $query .= "         SCHOOLCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         YEAR, ";
        $query .= "         SLIP_NO ";
        $query .= " ), SLIP_PLAN_PAID AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHOOLCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         YEAR, ";
        $query .= "         SLIP_NO, ";
        $query .= "         MAX(PLAN_PAID_MONEY_DATE) AS PAID_MONEY_DATE, ";
        $query .= "         SUM(VALUE(PLAN_PAID_MONEY,0)) AS TPAID_MONEY ";
        $query .= "     FROM ";
        $query .= "         COLLECT_SLIP_PLAN_PAID_DAT ";
        $query .= "     GROUP BY ";
        $query .= "         SCHOOLCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         YEAR, ";
        $query .= "         SLIP_NO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     SLIP.SLIP_NO, ";
        $query .= "     SLIP.PAY_DIV, ";
        $query .= "     SLIP.SLIP_DATE, ";
        $query .= "     SLIP.CANCEL_DATE, ";
        $query .= "     PLAN_PAID.PAID_MONEY_DATE, ";
        $query .= "     SLIP_M.TMONEY, ";
        $query .= "     PLAN_PAID.TPAID_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_DAT SLIP ";
        $query .= "     LEFT JOIN SLIP_M ";
        $query .= "          ON SLIP.YEAR           = SLIP_M.YEAR ";
        $query .= "         AND SLIP.SCHOOLCD       = SLIP_M.SCHOOLCD ";
        $query .= "         AND SLIP.SCHOOL_KIND    = SLIP_M.SCHOOL_KIND ";
        $query .= "         AND SLIP.SLIP_NO        = SLIP_M.SLIP_NO ";
        $query .= "     LEFT JOIN SLIP_PLAN_PAID PLAN_PAID ";
        $query .= "          ON SLIP.YEAR           = PLAN_PAID.YEAR ";
        $query .= "         AND SLIP.SCHOOLCD       = PLAN_PAID.SCHOOLCD ";
        $query .= "         AND SLIP.SCHOOL_KIND    = PLAN_PAID.SCHOOL_KIND ";
        $query .= "         AND SLIP.SLIP_NO        = PLAN_PAID.SLIP_NO ";
        $query .= " WHERE ";
        $query .= "     SLIP.YEAR           = '{$model->mst_field["SELECT_YEAR"]}' AND ";
        $query .= "     SLIP.SCHOOLCD       = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "     SLIP.SCHOOL_KIND    = '".$model->schoolKind."' AND ";
        $query .= "     SLIP.SLIP_NO        = '{$model->mst_field["SLIP_NO"]}' AND ";
        $query .= "     SLIP.SCHREGNO       = '{$model->schregno}' ";

        return $query;
    }

    //生徒伝票総計取得
    function getSlipDataALL($model) {
        $query  = " WITH SLIP_M AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHOOLCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         YEAR, ";
        $query .= "         SLIP_NO, ";
        $query .= "         SUM(VALUE(COLLECT_MONEY, 0) * VALUE(COLLECT_CNT, 0)) AS ALL_MONEY ";
        $query .= "     FROM ";
        $query .= "         COLLECT_SLIP_M_DAT ";
        $query .= "     GROUP BY ";
        $query .= "         SCHOOLCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         YEAR, ";
        $query .= "         SLIP_NO ";
        $query .= " ), SLIP_PLAN_PAID AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHOOLCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         YEAR, ";
        $query .= "         SLIP_NO, ";
        $query .= "         SUM(VALUE(PLAN_PAID_MONEY,0)) AS ALL_PAID_MONEY ";
        $query .= "     FROM ";
        $query .= "         COLLECT_SLIP_PLAN_PAID_DAT ";
        $query .= "     GROUP BY ";
        $query .= "         SCHOOLCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         YEAR, ";
        $query .= "         SLIP_NO ";
        $query .= " ), MAIN_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         SUM(VALUE(SLIP_M.ALL_MONEY, 0)) AS ALL_MONEY, ";
        $query .= "         SUM(VALUE(PLAN_PAID.ALL_PAID_MONEY, 0)) AS ALL_PAID_MONEY ";
        $query .= "     FROM ";
        $query .= "         COLLECT_SLIP_DAT SLIP ";
        $query .= "         LEFT JOIN SLIP_M ";
        $query .= "              ON SLIP.YEAR           = SLIP_M.YEAR ";
        $query .= "             AND SLIP.SCHOOLCD       = SLIP_M.SCHOOLCD ";
        $query .= "             AND SLIP.SCHOOL_KIND    = SLIP_M.SCHOOL_KIND ";
        $query .= "             AND SLIP.SLIP_NO        = SLIP_M.SLIP_NO ";
        $query .= "         LEFT JOIN SLIP_PLAN_PAID PLAN_PAID ";
        $query .= "              ON SLIP_M.YEAR         = PLAN_PAID.YEAR ";
        $query .= "             AND SLIP_M.SCHOOLCD     = PLAN_PAID.SCHOOLCD ";
        $query .= "             AND SLIP_M.SCHOOL_KIND  = PLAN_PAID.SCHOOL_KIND ";
        $query .= "             AND SLIP_M.SLIP_NO      = PLAN_PAID.SLIP_NO ";
        $query .= "     WHERE ";
        $query .= "         SLIP.YEAR           = '{$model->mst_field["SELECT_YEAR"]}' AND ";
        $query .= "         SLIP.SCHOOLCD       = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "         SLIP.SCHOOL_KIND    = '".$model->schoolKind."' AND ";
        $query .= "         SLIP.SCHREGNO       = '{$model->schregno}' AND ";
        $query .= "         SLIP.CANCEL_DATE IS NULL ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     ALL_MONEY, ";
        $query .= "     ALL_PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";

        return $query;
    }

    //入金グループコンボ
    function getGroupList($model) {
        $query  = " SELECT ";
        $query .= "     GD.COLLECT_GRP_CD AS VALUE, ";
        $query .= "     GD.COLLECT_GRP_CD || ':' || GM.COLLECT_GRP_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLECT_GRP_DAT GD ";
        $query .= "     INNER JOIN COLLECT_GRP_MST GM ";
        $query .= "          ON GD.YEAR = GM.YEAR ";
        $query .= "         AND GD.SCHOOLCD         = GM.SCHOOLCD ";
        $query .= "         AND GD.SCHOOL_KIND      = GM.SCHOOL_KIND ";
        $query .= "         AND GD.COLLECT_GRP_CD   = GM.COLLECT_GRP_CD ";
        $query .= " WHERE ";
        $query .= "     GD.YEAR         = '{$model->mst_field["SELECT_YEAR"]}' AND ";
        $query .= "     GD.SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "     GD.SCHOOL_KIND  = '".$model->schoolKind."' AND ";
        $query .= "     NOT EXISTS( SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     COLLECT_SLIP_DAT S1 ";
        $query .= "                 WHERE ";
        $query .= "                     S1.SCHOOLCD         = GD.SCHOOLCD AND ";
        $query .= "                     S1.SCHOOL_KIND      = GD.SCHOOL_KIND AND ";
        $query .= "                     S1.YEAR             = GD.YEAR AND ";
        $query .= "                     S1.SCHREGNO         = '".$model->schregno."' AND ";
        $query .= "                     S1.CANCEL_DATE      IS NULL AND ";
        $query .= "                     S1.COLLECT_GRP_CD   = GD.COLLECT_GRP_CD) ";
        $query .= " GROUP BY ";
        $query .= "     GD.COLLECT_GRP_CD, ";
        $query .= "     GM.COLLECT_GRP_NAME ";
        $query .= " ORDER BY ";
        $query .= "     GD.COLLECT_GRP_CD ";

        return $query;
    }

    //入金パターンデータ取得
    function getPatternList($model, $collect_pattern_cd="") {
        $query  = " SELECT DISTINCT ";
        if ($collect_pattern_cd) {
            $query .= "     * ";
        } else {
            $query .= "     COLLECT_PATTERN_CD AS VALUE, ";
            $query .= "     COLLECT_PATTERN_CD || ':' || COLLECT_PATTERN_NAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     COLLECT_DEFAULT_SETTINGS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->mst_field["SELECT_YEAR"]}' AND ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolKind."' ";
        if ($collect_pattern_cd) {
            $query .= " AND COLLECT_PATTERN_CD = '".$collect_pattern_cd."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //グループ選択
    function groupMstData($model, $groupcd) {
        $query  = " SELECT ";
        $query .= "     '1' AS ORDERCD, ";
        $query .= "     GD.COLLECT_L_CD, ";
        $query .= "     GD.COLLECT_M_CD, ";
        $query .= "     CM.TEXTBOOKDIV, ";
        $query .= "     CM.COLLECT_M_MONEY AS COLLECT_MONEY, ";
        $query .= "     CM.IS_CREDITCNT ";
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
        $query .= " WHERE ";
        $query .= "     GD.YEAR             = '{$model->mst_field["SELECT_YEAR"]}' AND ";
        $query .= "     GD.SCHOOLCD         = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "     GD.SCHOOL_KIND      = '".$model->schoolKind."' AND ";
        $query .= "     GD.COLLECT_GRP_CD   = '".$groupcd."' ";
        $query .= " ORDER BY ";
        $query .= "     ORDERCD, ";
        $query .= "     COLLECT_L_CD, ";
        $query .= "     COLLECT_M_CD ";

        return $query;
    }

    //入金項目一覧取得
    function getCollectM($model) {
        $query  = " SELECT ";
        $query .= "     COLLECT_L_CD || ':' || COLLECT_M_CD AS VALUE, ";
        $query .= "     COLLECT_L_CD || COLLECT_M_CD || ':' || COLLECT_M_NAME AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     COLLECT_M_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->mst_field["SELECT_YEAR"]}' AND ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //教科書データ
    function getTextBook($model, $textbookDiv, $selectDiv = "") {
        $query  = " SELECT ";
        if ($selectDiv == "TOTAL") {
            $query .= "     VALUE(SUM(VALUE(TEXT_M.TEXTBOOKUNITPRICE, 0)), 0) AS TEXTBOOKUNITPRICE ";
        } else {
            $query .= "     TEXT_SUB.TEXTBOOKCD, ";
            $query .= "     TEXT_M.TEXTBOOKUNITPRICE ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_TEXTBOOK_SUBCLASS_DAT TEXT_SUB ";
        $query .= "     INNER JOIN TEXTBOOK_MST TEXT_M ON TEXT_SUB.TEXTBOOKCD = TEXT_M.TEXTBOOKCD ";
        $query .= "           AND TEXT_M.TEXTBOOKDIV = '{$textbookDiv}' ";
        $query .= " WHERE ";
        $query .= "     TEXT_SUB.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND TEXT_SUB.YEAR = '{$model->mst_field["SELECT_YEAR"]}' ";
        $query .= "     AND NOT EXISTS (SELECT ";
        $query .= "                         'X' ";
        $query .= "                     FROM ";
        $query .= "                         COLLECT_SLIP_TEXTBOOK_DAT S1 ";
        $query .= "                     WHERE ";
        $query .= "                         S1.SCHOOLCD             = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "                         AND S1.SCHOOL_KIND      = '{$model->schoolKind}' ";
        $query .= "                         AND TEXT_SUB.YEAR       = S1.YEAR ";
        $query .= "                         AND TEXT_SUB.SCHREGNO   = S1.SCHREGNO ";
        $query .= "                         AND TEXT_SUB.TEXTBOOKCD = S1.TEXTBOOKCD ";
        $query .= "                     ) ";
        if ($selectDiv == "") {
            $query .= " ORDER BY ";
            $query .= "     TEXT_SUB.TEXTBOOKCD ";
        }

        return $query;
    }

    //生徒伝票明細
    function getMeisaiData($model) {
        $query  = " WITH MAIN_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         SLIP.SLIP_NO, ";
        $query .= "         SLIP_MD.COLLECT_L_CD, ";
        $query .= "         SLIP_MD.COLLECT_M_CD, ";
        $query .= "         CM.TEXTBOOKDIV, ";
        $query .= "         MAX(SLIP_MD.COLLECT_CNT) AS COLLECT_CNT, ";
        $query .= "         SUM(VALUE(SLIP_MD.COLLECT_MONEY, 0)) AS COLLECT_MONEY ";
        $query .= "     FROM ";
        $query .= "         COLLECT_SLIP_DAT SLIP ";
        $query .= "         LEFT JOIN COLLECT_SLIP_M_DAT SLIP_MD ON SLIP.YEAR = SLIP_MD.YEAR ";
        $query .= "              AND SLIP.SCHOOLCD      = SLIP_MD.SCHOOLCD ";
        $query .= "              AND SLIP.SCHOOL_KIND   = SLIP_MD.SCHOOL_KIND ";
        $query .= "              AND SLIP.SLIP_NO       = SLIP_MD.SLIP_NO ";
        $query .= "     LEFT JOIN COLLECT_M_MST CM ON SLIP_MD.YEAR = CM.YEAR ";
        $query .= "          AND SLIP_MD.SCHOOLCD     = CM.SCHOOLCD ";
        $query .= "          AND SLIP_MD.SCHOOL_KIND  = CM.SCHOOL_KIND ";
        $query .= "          AND SLIP_MD.COLLECT_L_CD = CM.COLLECT_L_CD ";
        $query .= "          AND SLIP_MD.COLLECT_M_CD = CM.COLLECT_M_CD ";
        $query .= "     WHERE ";
        $query .= "         SLIP.YEAR = '{$model->mst_field["SELECT_YEAR"]}' ";
        $query .= "         AND SLIP.SCHOOLCD       = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "         AND SLIP.SCHOOL_KIND    = '".$model->schoolKind."' ";
        $query .= "         AND SLIP.SLIP_NO        = '{$model->mst_field["SLIP_NO"]}' ";
        $query .= "         AND SLIP.SCHREGNO       = '{$model->schregno}' ";
        $query .= "     GROUP BY ";
        $query .= "         SLIP.SLIP_NO, ";
        $query .= "         SLIP_MD.COLLECT_L_CD, ";
        $query .= "         SLIP_MD.COLLECT_M_CD, ";
        $query .= "         CM.TEXTBOOKDIV ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " ORDER BY ";
        $query .= "     SLIP_NO, ";
        $query .= "     COLLECT_L_CD, ";
        $query .= "     COLLECT_M_CD ";

        return $query;
    }

    //削除
    function &deleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        // COLLECT_SLIP_DAT
        $query  = " DELETE FROM ";
        $query .= "     COLLECT_SLIP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->mst_field["SELECT_YEAR"]}' AND ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolKind."' AND ";
        $query .= "     SLIP_NO     = '{$model->mst_field["SLIP_NO"]}' ";
        $db->query($query);

        // COLLECT_SLIP_M_DAT
        $query  = " DELETE FROM ";
        $query .= "     COLLECT_SLIP_M_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->mst_field["SELECT_YEAR"]}' AND ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolKind."' AND ";
        $query .= "     SLIP_NO     = '{$model->mst_field["SLIP_NO"]}' ";
        $db->query($query);

        // COLLECT_SLIP_PLAN_M_DAT
        $query  = " DELETE FROM ";
        $query .= "     COLLECT_SLIP_PLAN_M_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->mst_field["SELECT_YEAR"]}' AND ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolKind."' AND ";
        $query .= "     SLIP_NO     = '{$model->mst_field["SLIP_NO"]}' ";
        $db->query($query);

        // COLLECT_SLIP_PLAN_LIMITDATE_DAT
        $query  = " DELETE FROM ";
        $query .= "     COLLECT_SLIP_PLAN_LIMITDATE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->mst_field["SELECT_YEAR"]}' AND ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolKind."' AND ";
        $query .= "     SLIP_NO     = '{$model->mst_field["SLIP_NO"]}' ";
        $db->query($query);

        // COLLECT_SLIP_TEXTBOOK_DAT
        $query  = " DELETE FROM ";
        $query .= "     COLLECT_SLIP_TEXTBOOK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->mst_field["SELECT_YEAR"]}' AND ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolKind."' AND ";
        $query .= "     SLIP_NO     = '{$model->mst_field["SLIP_NO"]}' ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }

    //更新
    function &updateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //新規作成FLG
        $isNewData = true;

        //MAX伝票番号取得
        $slipNoArray = array();
        $result = $db->query(knjp715Query::getMaxSlipNo($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $slipNoArray[$row["SCHREGNO"]] = substr($row["SLIP_NO"],0,12).sprintf("%03d", (substr($row["SLIP_NO"],12) + 1));
        }
        $result->free();

        //銀行休業日取得
        $holidayArray = array();
        $result = $db->query(knjp715Query::getBankHolidayDat($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $holidayArray[] = $row["EXECUTEDATE"];
        }
        $result->free();

        //項目データ取得
        $colM = array();
        $result = $db->query(knjp715Query::getCollectM($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $lmcd = $row["COLLECT_L_CD"].":".$row["COLLECT_M_CD"];
            $colM[$lmcd]["DIVIDE_PROCESS"]  = $row["DIVIDE_PROCESS"];
            $colM[$lmcd]["ROUND_DIGIT"]     = $row["ROUND_DIGIT"];
        }
        $result->free();

        //伝票番号あり
        if ($model->mst_field["SLIP_NO"] != "") {
            $isNewData = false;
        }

        if ($isNewData) {
            //伝票番号
            $slipNo = ($slipNoArray[$model->schregno]) ? $slipNoArray[$model->schregno] : $model->mst_field["SELECT_YEAR"].$model->schregno."001";

            $groupcd = $model->mst_field["GROUPCD"];
            $patterncd = $model->mst_field["COLLECT_PATTERN_CD"];
        } else {
            //伝票番号
            $slipNo = $model->mst_field["SLIP_NO"];

            //伝票データ取得
            $slipInfo = $db->getRow(knjp715Query::getSlipInfo($model), DB_FETCHMODE_ASSOC);
            $groupcd = $slipInfo["COLLECT_GRP_CD"];
            $patterncd = $slipInfo["COLLECT_PATTERN_CD"];
        }

        //入金グループコード設定
        if ((int)$groupcd > 0) {
            //グループの項目取得
            $array = array();
            $result = $db->query(knjp715Query::groupMstData($model, $groupcd));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $lm_cd = $row["COLLECT_L_CD"].":".$row["COLLECT_M_CD"];
                if ($row["TEXTBOOKDIV"]) {
                    $array[$lm_cd]["COLLECT_MONEY"] = $model->collectM[$lm_cd]["COLLECT_M_MONEY"];
                    $array[$lm_cd]["COLLECT_CNT"]   = "1";
                } else {
                    $array[$lm_cd]["COLLECT_MONEY"] = $row["COLLECT_MONEY"];
                    $array[$lm_cd]["COLLECT_CNT"]   = ($row["IS_CREDITCNT"] == 1) ? $model->mst_field["CREDITS"] : "1";
                }
            }
            $result->free();

            //項目の不一致チェック
            $flg = true;
            $checkField = array("COLLECT_MONEY", "COLLECT_CNT");
            foreach ($model->updField as $seq => $setData) {
                foreach ($checkField as $field) {
                    if ($setData[$field] != $array[$setData["COLLECT_LM_CD"]][$field]) $flg = false;
                }
            }

            //件数の不一致チェック
            if (get_count($model->updField) != get_count($array)) $flg = false;

            $setGroupcd = ($flg) ? $groupcd : '0000';
        } else {
            $setGroupcd = '0000';
        }

        //入金パターンコード設定
        if ((int)$patterncd > 0) {
            //チェック対象フィールド
            $checkField = array();
            $checkField["PAY_DIV"] = 1;
            if ($isNewData) {
                $checkField["DIRECT_DEBIT_DATE"] = 2;
                for ($i = 1; $i <= 12; $i++) $checkField["COLLECT_MONTH_".$i] = 2;
            }
            //入金パターンデータ取得
            $pattern = $db->getRow(knjp715Query::getPatternList($model, $patterncd), DB_FETCHMODE_ASSOC);

            //入金月の不一致チェック
            $flg = true;
            foreach ($checkField as $field => $size) {
                if (sprintf("%0{$size}d",$model->mst_field[$field]) != sprintf("%0{$size}d",$pattern[$field])) $flg = false;
            }
            $setPatterncd = ($flg) ? $patterncd : '00';
        } else {
            $setPatterncd = '00';
        }

        //入金計画取得
        $monthGData = array();
        $result = $db->query(knjp715Query::getCollectMonthGrpDat($model, $setGroupcd));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $monthGData[$row["COLLECT_L_CD"].":".$row["COLLECT_M_CD"]] = $row;
        }
        $result->free();

        //追加/更新 -- COLLECT_SLIP_DAT
        $data = array();
        $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]      = $model->schoolKind;
        $data["YEAR"][TEXT]             = $model->mst_field["SELECT_YEAR"];
        $data["SLIP_NO"][TEXT]          = $slipNo;
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["SLIP_STAFFCD"][TEXT]     = STAFFCD;
        if (!strlen($model->mst_field["CANCEL_DATE"])) {
            $data["PAY_DIV"][TEXT]          = $model->mst_field["PAY_DIV"];
            $data["SLIP_DATE"][DATENC]      = str_replace("/", "-", $model->mst_field["SLIP_DATE"]);
            $data["COLLECT_GRP_CD"][TEXT]   = $setGroupcd;
            $data["COLLECT_PATTERN_CD"][TEXT] = $setPatterncd;
            $data["CANCEL_STAFFCD"][TEXT]   = NULL;
        } else {
            $data["CANCEL_STAFFCD"][TEXT]   = STAFFCD;
        }
        $data["CANCEL_DATE"][DATE]      = str_replace("/", "-", $model->mst_field["CANCEL_DATE"]);
        $data["CANCEL_REASON"][TEXT]    = $model->mst_field["CANCEL_REASON"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        if ($isNewData) {
            $query = Query::insertSQL($data, "COLLECT_SLIP_DAT");
            $db->query($query);
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR            = '{$model->mst_field["SELECT_YEAR"]}' ";
            $where .= "     AND SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $where .= "     AND SCHOOL_KIND = '".$model->schoolKind."' ";
            $where .= "     AND SLIP_NO     = '".$slipNo."' ";
            $query = Query::updateSQL($data, "COLLECT_SLIP_DAT", $where);
            $db->query($query);
        }

        if (!strlen($model->mst_field["CANCEL_DATE"])) {

            //削除 -- COLLECT_SLIP_M_DAT
            $query  = " DELETE FROM ";
            $query .= "     COLLECT_SLIP_M_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '{$model->mst_field["SELECT_YEAR"]}' AND ";
            $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' AND ";
            $query .= "     SCHOOL_KIND = '".$model->schoolKind."' AND ";
            $query .= "     SLIP_NO     = '{$model->mst_field["SLIP_NO"]}' ";
            $db->query($query);

            //削除 -- COLLECT_SLIP_TEXTBOOK_DAT
            $query  = " DELETE FROM ";
            $query .= "     COLLECT_SLIP_TEXTBOOK_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '{$model->mst_field["SELECT_YEAR"]}' AND ";
            $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' AND ";
            $query .= "     SCHOOL_KIND = '".$model->schoolKind."' AND ";
            $query .= "     SLIP_NO     = '{$model->mst_field["SLIP_NO"]}' ";
            $db->query($query);

            $tmpArray = array();
            foreach ($model->updField as $seq => $setData) {

                list($lcd, $mcd) = explode(":", $setData["COLLECT_LM_CD"]);

                $textBookArray = array();
                $totalTextBookMoney = 0;
                if ($setData["TEXTBOOKDIV"]) {
                    $query = knjp715Query::getTextBook($model, $setData["TEXTBOOKDIV"]);
                    $result = $db->query($query);
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $textBookArray[$row["TEXTBOOKCD"]] = $row["TEXTBOOKUNITPRICE"];
                        $totalTextBookMoney += $row["TEXTBOOKUNITPRICE"];
                    }
                    $result->free();

                    foreach ($textBookArray as $textBookCd => $textBookMoney) {

                        // COLLECT_SLIP_TEXTBOOK_DAT
                        $data = array();
                        $data["SCHOOLCD"][TEXT]             = sprintf("%012d", SCHOOLCD);
                        $data["SCHOOL_KIND"][TEXT]          = $model->schoolKind;
                        $data["YEAR"][TEXT]                 = $model->mst_field["SELECT_YEAR"];
                        $data["SLIP_NO"][TEXT]              = $slipNo;
                        $data["TEXTBOOKCD"][TEXT]           = $textBookCd;
                        $data["SCHREGNO"][TEXT]             = $model->schregno;
                        $data["TEXTBOOKUNITPRICE"][NUMBER]  = $textBookMoney;
                        $data["REGISTERCD"][TEXT]           = STAFFCD;
                        $data["UPDATED"][NUMBER]            = "sysdate()";

                        $query = Query::insertSQL($data, "COLLECT_SLIP_TEXTBOOK_DAT");
                        $db->query($query);
                    }
                    $setData["COLLECT_MONEY"] = $totalTextBookMoney;
                }

                //追加 -- COLLECT_SLIP_M_DAT
                $data = array();
                $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
                $data["SCHOOL_KIND"][TEXT]      = $model->schoolKind;
                $data["YEAR"][TEXT]             = $model->mst_field["SELECT_YEAR"];
                $data["SLIP_NO"][TEXT]          = $slipNo;
                $data["SCHREGNO"][TEXT]         = $model->schregno;
                $data["COLLECT_L_CD"][TEXT]     = $lcd;
                $data["COLLECT_M_CD"][TEXT]     = $mcd;
                $data["COLLECT_MONEY"][NUMBER]  = $setData["COLLECT_MONEY"];
                $data["COLLECT_CNT"][NUMBER]    = $setData["COLLECT_CNT"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";

                $query = Query::insertSQL($data, "COLLECT_SLIP_M_DAT");
                $db->query($query);

                //項目ごとに年額を格納
                $tmpArray[$setData["COLLECT_LM_CD"]] += $setData["COLLECT_MONEY"] * $setData["COLLECT_CNT"];

            }

            //削除 -- COLLECT_SLIP_PLAN_M_DAT
            $query  = " DELETE FROM ";
            $query .= "     COLLECT_SLIP_PLAN_M_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '{$model->mst_field["SELECT_YEAR"]}' AND ";
            $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' AND ";
            $query .= "     SCHOOL_KIND = '".$model->schoolKind."' AND ";
            $query .= "     SLIP_NO     = '{$model->mst_field["SLIP_NO"]}' ";
            $db->query($query);

            //入金額のまるめ変換用
            $round_digit = array("1" => "1", "2" => "10", "3" => "100");

            foreach ($tmpArray as $lmcd => $total_money) {
                list($lcd, $mcd) = explode(":", $lmcd);

                if ($monthGData[$lmcd]["MONTH_CNT"]) {

                    if ($colM[$lmcd]["DIVIDE_PROCESS"] == "3") {
                        $tukiwari = number_format($total_money / $monthGData[$lmcd]["MONTH_CNT"], 4, '.', '');
                        list ($int, $decimal) = explode(".", $tukiwari);
                        $decimal = "0.".$decimal;
                    } else {
                        $amari = $total_money % ($monthGData[$lmcd]["MONTH_CNT"] * $round_digit[$colM[$lmcd]["ROUND_DIGIT"]]);
                        $tukiwari = ($total_money - $amari) / $monthGData[$lmcd]["MONTH_CNT"];
                    }

                    $hasuu = $decimal;
                    $counter = 0;
                    $money_sum = 0;
                    for ($i = 4; $i <= 15; $i++) {
                        $month = ($i > 12) ? ($i - 12) : $i;

                        if ($monthGData[$lmcd]["COLLECT_MONTH_".$month] != "1") {
                            $plan_year0 = ($i > 12) ? ($model->mst_field["SELECT_YEAR"] + 1) : $model->mst_field["SELECT_YEAR"];
                            $plan_month0 = sprintf("%02d", $month);

                            //追加 -- COLLECT_SLIP_PLAN_M_DAT
                            $data = array();
                            $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
                            $data["SCHOOL_KIND"][TEXT]      = $model->schoolKind;
                            $data["YEAR"][TEXT]             = $model->mst_field["SELECT_YEAR"];
                            $data["SCHREGNO"][TEXT]         = $model->schregno;
                            $data["SLIP_NO"][TEXT]          = $slipNo;
                            $data["COLLECT_L_CD"][TEXT]     = $lcd;
                            $data["COLLECT_M_CD"][TEXT]     = $mcd;
                            $data["PLAN_YEAR"][TEXT]        = $plan_year0;
                            $data["PLAN_MONTH"][TEXT]       = $plan_month0;
                            $data["PLAN_MONEY"][NUMBER]     = 0;
                            $data["REGISTERCD"][TEXT]       = STAFFCD;
                            $data["UPDATED"][NUMBER]        = "sysdate()";

                            $query = Query::insertSQL($data, "COLLECT_SLIP_PLAN_M_DAT");
                            $db->query($query);
                        }

                        if ($monthGData[$lmcd]["COLLECT_MONTH_".$month] != "1") continue;

                        $plan_year = ($i > 12) ? ($model->mst_field["SELECT_YEAR"] + 1) : $model->mst_field["SELECT_YEAR"];
                        $plan_month = sprintf("%02d", $month);

                        $plan_money = "";
                        if ($colM[$lmcd]["DIVIDE_PROCESS"] == "1") {
                            $plan_money = ($counter == 0) ? ($tukiwari + $amari) : $tukiwari;
                        } else if ($colM[$lmcd]["DIVIDE_PROCESS"] == "2") {
                            $plan_money = $tukiwari;
                        } else if ($colM[$lmcd]["DIVIDE_PROCESS"] == "3") {
                            $plan_money = $int + floor($hasuu);
                        }

                        //追加 -- COLLECT_SLIP_PLAN_M_DAT
                        $data = array();
                        $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
                        $data["SCHOOL_KIND"][TEXT]      = $model->schoolKind;
                        $data["YEAR"][TEXT]             = $model->mst_field["SELECT_YEAR"];
                        $data["SCHREGNO"][TEXT]         = $model->schregno;
                        $data["SLIP_NO"][TEXT]          = $slipNo;
                        $data["COLLECT_L_CD"][TEXT]     = $lcd;
                        $data["COLLECT_M_CD"][TEXT]     = $mcd;
                        $data["PLAN_YEAR"][TEXT]        = $plan_year;
                        $data["PLAN_MONTH"][TEXT]       = $plan_month;
                        $data["PLAN_MONEY"][NUMBER]     = $plan_money;
                        $data["REGISTERCD"][TEXT]       = STAFFCD;
                        $data["UPDATED"][NUMBER]        = "sysdate()";

                        $query = Query::insertSQL($data, "COLLECT_SLIP_PLAN_M_DAT");
                        $db->query($query);

                        $money_sum += $plan_money;
                        $hasuu -= floor($hasuu);
                        $hasuu += $decimal;

                        $counter++;
                    }

                    if (($colM[$lmcd]["DIVIDE_PROCESS"] == "2" || ($colM[$lmcd]["DIVIDE_PROCESS"] == "3" && $total_money != $money_sum)) && $counter > 0) {
                        $plan_money = ($colM[$lmcd]["DIVIDE_PROCESS"] == "2") ? ($tukiwari + $amari) : ($total_money - $money_sum + $plan_money);

                        //更新 -- COLLECT_SLIP_PLAN_M_DAT
                        $data = array();
                        $data["PLAN_MONEY"][NUMBER]     = $plan_money;
                        $data["REGISTERCD"][TEXT]       = STAFFCD;
                        $data["UPDATED"][FUNC]          = "sysdate()";

                        $where  = " WHERE ";
                        $where .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' AND ";
                        $where .= "     SCHOOL_KIND     = '".$model->schoolKind."' AND ";
                        $where .= "     YEAR            = '".$model->mst_field["SELECT_YEAR"]."' AND ";
                        $where .= "     SCHREGNO        = '".$model->schregno."' AND ";
                        $where .= "     SLIP_NO         = '".$slipNo."' AND ";
                        $where .= "     COLLECT_L_CD    = '".$lcd."' AND ";
                        $where .= "     COLLECT_M_CD    = '".$mcd."' AND ";
                        $where .= "     PLAN_YEAR       = '".$plan_year."' AND ";
                        $where .= "     PLAN_MONTH      = '".$plan_month."' ";

                        $query = Query::updateSQL($data, "COLLECT_SLIP_PLAN_M_DAT", $where);
                        $db->query($query);
                    }
                }
            }
        }

        if ($isNewData) {

            //追加 -- COLLECT_SLIP_PLAN_LIMITDATE_DAT
            for ($i = 1; $i <= 12; $i++) {
                $paid_limit_date = $month = "";
                if (strlen($model->mst_field["COLLECT_MONTH_".$i])) {
                    $month = intval($model->mst_field["COLLECT_MONTH_".$i], 10);
                    $year = ($month <= 3) ? ($model->mst_field["SELECT_YEAR"] + 1) : $model->mst_field["SELECT_YEAR"];
                    //月末日
                    $lastday = date("t", mktime( 0, 0, 0, $month, 1, $year));
                    $day = ($model->mst_field["DIRECT_DEBIT_DATE"] > $lastday) ? $lastday : $model->mst_field["DIRECT_DEBIT_DATE"];

                    $date = $year."-".sprintf("%02d", $month)."-".sprintf("%02d", $day);

                    //土日祝日の場合は翌営業日
                    $paid_limit_date = $date;
                    for ($d = 0; $d < 100; $d++) {
                        $paid_limit_date = date("Y-m-d", strtotime("$date $d day"));
                        //土(6)・日(0)・祝日以外
                        if (!in_array(date('w', strtotime($paid_limit_date)), array(0,6)) && !in_array($paid_limit_date, $holidayArray)) {
                            break;
                        }
                    }
                }

                $data = array();
                $data["SCHOOLCD"][TEXT]              = sprintf("%012d", SCHOOLCD);
                $data["SCHOOL_KIND"][TEXT]           = $model->schoolKind;
                $data["YEAR"][TEXT]                  = $model->mst_field["SELECT_YEAR"];
                $data["SCHREGNO"][TEXT]              = $model->schregno;
                $data["SLIP_NO"][TEXT]               = $slipNo;
                $data["PLAN_YEAR"][TEXT]             = ($i <= 3) ? ($model->mst_field["SELECT_YEAR"] + 1) : $model->mst_field["SELECT_YEAR"];
                $data["PLAN_MONTH"][TEXT]            = sprintf("%02d", $i);
                $data["PAID_LIMIT_MONTH"][TEXT]      = ($month) ? sprintf("%02d", $month) : "";
                $data["PAID_LIMIT_DATE"][TEXT]       = $paid_limit_date;
                $data["PAID_LIMIT_MONTH_CALC"][TEXT] = ($month) ? sprintf("%02d", $month) : "";
                $data["PAID_LIMIT_DATE_CALC"][TEXT]  = $paid_limit_date;
                $data["REGISTERCD"][TEXT]            = STAFFCD;
                $data["UPDATED"][NUMBER]             = "sysdate()";

                $query = Query::insertSQL($data, "COLLECT_SLIP_PLAN_LIMITDATE_DAT");
                $db->query($query);
            }
        }

        //納入月の変更
        //例：4月～9月 11月支払い、10月～03月 01月支払い
        //4月～11月 11月支払いに変更
        //4月～11月 11月支払い、12月～03月 01月支払い
        $query = knjp715Query::getLimit($model, $slipNo);
        $result = $db->query($query);
        $limitArray = array();
        $setLimitCnt = 0;
        while ($limitRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($setLimitCnt == 0 && $limitRow["MAX_YM"] < $limitRow["PAID_LIMIT_Y"].$limitRow["PAID_LIMIT_MONTH"]) {
                $limitArray[] = $limitRow;
            }
            if ($setLimitCnt > 0 && $limitRow["MIN_YM"] < $limitRow["PAID_LIMIT_Y"].$limitRow["PAID_LIMIT_MONTH"]) {
                $limitArray[] = $limitRow;
            }
            $setLimitCnt++;
        }
        $result->free();

        if (get_count($limitArray) > 1) {
            $limitCnt = 1;
            $befLimit = "";
            foreach ($limitArray as $limitKey => $limitVal) {
                $limitYM = $limitVal["PAID_LIMIT_Y"].$limitVal["PAID_LIMIT_MONTH"];
                $data = array();
                $data["PAID_LIMIT_MONTH"][TEXT]      = $limitVal["PAID_LIMIT_MONTH"];
                $data["PAID_LIMIT_DATE"][TEXT]       = $limitVal["PAID_LIMIT_DATE"];
                $data["REGISTERCD"][TEXT]            = STAFFCD;
                $data["UPDATED"][NUMBER]             = "sysdate()";

                $where  = " WHERE ";
                $where .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
                $where .= "     AND SCHOOL_KIND     = '".$model->schoolKind."' ";
                $where .= "     AND YEAR            = '".$model->mst_field["SELECT_YEAR"]."' ";
                $where .= "     AND SCHREGNO        = '".$model->schregno."' ";
                $where .= "     AND SLIP_NO         = '".$slipNo."' ";
                $where .= "     AND PLAN_YEAR || PLAN_MONTH <= '{$limitYM}' ";
                if ($befLimit) {
                    $where .= "     AND PLAN_YEAR || PLAN_MONTH > '{$befLimit}' ";
                }

                $query = Query::updateSQL($data, "COLLECT_SLIP_PLAN_LIMITDATE_DAT", $where);
                $db->query($query);

                $befLimit = $limitYM;
                $limitCnt++;
                if (get_count($limitArray) <= $limitCnt) {
                    break;
                }
            }
        }

        /************************/
        /* 金額を支払月に寄せる */
        /************************/
        $query = knjp715Query::getMonthMoney($model, $slipNo);
        $result = $db->query($query);
        $updMonthMoney = array();
        while ($updMMrow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $updMonthMoney[] = $updMMrow;
        }
        $result->free();
        //金額クリア
        $data = array();
        $data["PLAN_MONEY"][NUMBER]     = 0;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ";
        $where .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $where .= "     SCHOOL_KIND     = '{$model->schoolKind}' AND ";
        $where .= "     YEAR            = '{$model->mst_field["SELECT_YEAR"]}' AND ";
        $where .= "     SCHREGNO        = '{$model->schregno}' AND ";
        $where .= "     SLIP_NO         = '{$slipNo}' ";

        $query = Query::updateSQL($data, "COLLECT_SLIP_PLAN_M_DAT", $where);
        $db->query($query);

        //支払月の金額を変更
        foreach ($updMonthMoney as $key => $updMoneyArray) {
            $data = array();
            $data["PLAN_MONEY"][NUMBER]     = $updMoneyArray["PLAN_MONEY"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $where  = " WHERE ";
            $where .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' AND ";
            $where .= "     SCHOOL_KIND     = '{$model->schoolKind}' AND ";
            $where .= "     YEAR            = '{$model->mst_field["SELECT_YEAR"]}' AND ";
            $where .= "     SCHREGNO        = '{$model->schregno}' AND ";
            $where .= "     SLIP_NO         = '{$slipNo}' AND ";
            $where .= "     COLLECT_L_CD    = '{$updMoneyArray["COLLECT_L_CD"]}' AND ";
            $where .= "     COLLECT_M_CD    = '{$updMoneyArray["COLLECT_M_CD"]}' AND ";
            $where .= "     PLAN_YEAR       = '{$updMoneyArray["PLAN_YEAR"]}' AND ";
            $where .= "     PLAN_MONTH      = '{$updMoneyArray["PLAN_MONTH"]}' ";

            $query = Query::updateSQL($data, "COLLECT_SLIP_PLAN_M_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $slipNo;
    }

    //金額を支払月に寄せる
    function getMonthMoney($model, $slipNo) {
        $query .= " SELECT ";
        $query .= "     PLAN_M.SCHOOLCD, ";
        $query .= "     PLAN_M.SCHOOL_KIND, ";
        $query .= "     PLAN_LIMIT.YEAR, ";
        $query .= "     PLAN_M.SCHREGNO, ";
        $query .= "     PLAN_M.SLIP_NO, ";
        $query .= "     PLAN_M.COLLECT_L_CD, ";
        $query .= "     PLAN_M.COLLECT_M_CD, ";
        $query .= "     YEAR(PAID_LIMIT_DATE) AS PLAN_YEAR, ";
        $query .= "     PLAN_LIMIT.PAID_LIMIT_MONTH AS PLAN_MONTH, ";
        $query .= "     PLAN_LIMIT.PAID_LIMIT_DATE, ";
        $query .= "     SUM(PLAN_M.PLAN_MONEY) AS PLAN_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PLAN_M_DAT PLAN_M ";
        $query .= "     INNER JOIN COLLECT_SLIP_PLAN_LIMITDATE_DAT PLAN_LIMIT ON PLAN_M.SCHOOLCD = PLAN_LIMIT.SCHOOLCD ";
        $query .= "           AND PLAN_M.SCHOOL_KIND = PLAN_LIMIT.SCHOOL_KIND ";
        $query .= "           AND PLAN_M.YEAR = PLAN_LIMIT.YEAR ";
        $query .= "           AND PLAN_M.SCHREGNO = PLAN_LIMIT.SCHREGNO ";
        $query .= "           AND PLAN_M.SLIP_NO = PLAN_LIMIT.SLIP_NO ";
        $query .= "           AND PLAN_M.PLAN_YEAR = PLAN_LIMIT.PLAN_YEAR ";
        $query .= "           AND PLAN_M.PLAN_MONTH = PLAN_LIMIT.PLAN_MONTH ";
        $query .= "           AND PLAN_LIMIT.PAID_LIMIT_MONTH IS NOT NULL ";
        $query .= " WHERE ";
        $query .= "     PLAN_M.SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND PLAN_M.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND PLAN_M.YEAR = '{$model->mst_field["SELECT_YEAR"]}' ";
        $query .= "     AND PLAN_M.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND PLAN_M.SLIP_NO = '{$slipNo}' ";
        $query .= " GROUP BY ";
        $query .= "     PLAN_M.SCHOOLCD, ";
        $query .= "     PLAN_M.SCHOOL_KIND, ";
        $query .= "     PLAN_LIMIT.YEAR, ";
        $query .= "     PLAN_M.SCHREGNO, ";
        $query .= "     PLAN_M.SLIP_NO, ";
        $query .= "     PLAN_M.COLLECT_L_CD, ";
        $query .= "     PLAN_M.COLLECT_M_CD, ";
        $query .= "     YEAR(PAID_LIMIT_DATE), ";
        $query .= "     PLAN_LIMIT.PAID_LIMIT_MONTH, ";
        $query .= "     PLAN_LIMIT.PAID_LIMIT_DATE ";

        return $query;
    }

    //納期限
    function getLimit($model, $slipNo) {

        $query  = " SELECT ";
        $query .= "     SCHOOLCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     SLIP_NO, ";
        $query .= "     MIN(PLAN_YEAR || PLAN_MONTH) AS MIN_YM, ";
        $query .= "     MAX(PLAN_YEAR || PLAN_MONTH) AS MAX_YM, ";
        $query .= "     YEAR(PAID_LIMIT_DATE) AS PAID_LIMIT_Y, ";
        $query .= "     PAID_LIMIT_MONTH, ";
        $query .= "     PAID_LIMIT_DATE ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PLAN_LIMITDATE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND YEAR = '{$model->mst_field["SELECT_YEAR"]}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND SLIP_NO = '{$slipNo}' ";
        $query .= " GROUP BY ";
        $query .= "     SCHOOLCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     SLIP_NO, ";
        $query .= "     PAID_LIMIT_MONTH, ";
        $query .= "     PAID_LIMIT_DATE ";
        $query .= " ORDER BY ";
        $query .= "     PAID_LIMIT_DATE ";

        return $query;
    }

    //MAX伝票番号
    function getMaxSlipNo($model) {
        $query  = " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     MAX(SLIP_NO) AS SLIP_NO ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR           = '{$model->mst_field["SELECT_YEAR"]}' AND ";
        $query .= "     SCHOOLCD       = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "     SCHOOL_KIND    = '".$model->schoolKind."' ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";

        return $query;
    }

    //入金計画取得
    function getCollectMonthGrpDat($model, $collect_grp_cd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COLLECT_MONTH_GRP_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD        = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     SCHOOL_KIND     = '".$model->schoolKind."' AND ";
        $query .= "     YEAR            = '".$model->mst_field["SELECT_YEAR"]."' AND ";
        $query .= "     COLLECT_GRP_CD  = '".$collect_grp_cd."' ";

        return $query;
    }

    //銀行休業日取得
    function getBankHolidayDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     BANK_HOLIDAY_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolKind."' ";

        return $query;
    }
}
?>
