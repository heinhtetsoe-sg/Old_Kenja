<?php

require_once('for_php7.php');

class knjp730Query extends Query
{
    /************/
    /**  画面  **/
    /************/
    //生徒情報取得
    public function getSchInfo($model)
    {
        $setTable = "SCHREG_BASE_MST";
        if ($model->search_div == "1") {
            $setTable = "FRESHMAN_DAT";
        }

        $query  = " SELECT ";
        $query .= "     SCHREGNO || ' ' || NAME ";
        $query .= " FROM ";
        $query .= "     {$setTable} ";
        $query .= " WHERE ";
        $query .= "      SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //校種
    public function getSchoolKind($model)
    {
        $db = Query::dbCheckOut();

        if ($model->search_div == "1") {
            $query  = " SELECT ";
            $query .= "     GDAT.SCHOOL_KIND ";
            $query .= " FROM ";
            $query .= "     FRESHMAN_DAT FRSH ";
            $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = FRSH.ENTERYEAR ";
            $query .= "                                    AND GDAT.GRADE = FRSH.GRADE ";
            $query .= " WHERE ";
            $query .= "         FRSH.ENTERYEAR = '{$model->year}' ";
            $query .= "     AND FRSH.SCHREGNO  = '{$model->schregno}' ";
        } else {
            $query  = " SELECT ";
            $query .= "     REGD.GRADE || REGD.HR_CLASS AS GRADE_HR_CLASS, ";
            $query .= "     GDAT.SCHOOL_KIND ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT REGD ";
            $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = REGD.YEAR ";
            $query .= "                                    AND GDAT.GRADE = REGD.GRADE ";
            $query .= " WHERE ";
            $query .= "         REGD.SCHREGNO = '{$model->schregno}' ";
            $query .= "     AND REGD.YEAR     = '{$model->year}' ";
            $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        }

        $schKind = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $schKind;
    }

    //支援、補助、学校減免取得
    public function getReductionInfo($model)
    {
        //("REDUCTION_COUNTRY_PLAN_DAT", "REDUCTION_PLAN_DAT", "REDUCTION_SCHOOL_PLAN_DAT", "REDUCTION_BURDEN_CHARGE_PLAN_DAT")
        $setTable = array("COUNTRY_PLAN", "PLAN", "SCHOOL_PLAN", "BURDEN_CHARGE_PLAN");
        $sep = "";

        $query  = " WITH ";
        foreach ($setTable as $key => $tableName) {
            $query .= " {$sep}{$tableName} AS ( ";
            $query .= "         SELECT ";
            $query .= "             PLAN.* ";
            $query .= "         FROM ";
            $query .= "             REDUCTION_{$tableName}_DAT PLAN ";
            $query .= "             LEFT JOIN COLLECT_SLIP_DAT SL_D ON SL_D.SCHOOLCD    = PLAN.SCHOOLCD ";
            $query .= "                                            AND SL_D.SCHOOL_KIND = PLAN.SCHOOL_KIND ";
            $query .= "                                            AND SL_D.YEAR        = PLAN.YEAR ";
            $query .= "                                            AND SL_D.SLIP_NO     = PLAN.SLIP_NO ";
            $query .= "         WHERE ";
            $query .= "                 PLAN.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "             AND PLAN.SCHOOL_KIND = '{$model->schoolKind}' ";
            $query .= "             AND PLAN.YEAR        = '{$model->year}' ";
            $query .= "             AND PLAN.SCHREGNO    = '{$model->schregno}' ";
            $query .= "             AND SL_D.CANCEL_DATE IS NULL ";
            $sep = " ), ";
        }
        $query .= " ) ";

        $setTable2 = array("1" => "COUNTRY_PLAN:就学支援金:基本額",
                           "2" => "COUNTRY_PLAN:就学支援金:加算額",
                           "3" => "PLAN:補助金",
                           "4" => "SCHOOL_PLAN:学校減免",
                           "5" => "BURDEN_CHARGE_PLAN:学校負担金");

        $union = "";
        foreach ($setTable2 as $sort => $tableInfo) {
            list($tableName, $title, $subTitle) = explode(":", $tableInfo);
            if ($sort == "2") {
                $fieldName = "ADD_DECISION_MONEY";
            } elseif ($sort == "5") {
                $fieldName = "BURDEN_CHARGE";
            } else {
                $fieldName = "DECISION_MONEY";
            }
            $setSort = ($sort == "3" || $sort == "4") ? "INT('{$sort}' || REDUCTION_TARGET)": $sort;

            $query .= " {$union} ";
            $query .= " SELECT ";
            $query .= "     {$sort} AS BASE_SORT, ";
            $query .= "     {$setSort} AS SORT, ";
            $query .= "     SLIP_NO, ";
            $query .= "     '{$title}' AS KOUMOKU, ";
            if ($sort == "3" || $sort == "4") {
                $query .= "     CASE WHEN REDUCTION_TARGET = '1' THEN '授業料等' ";
                $query .= "          WHEN REDUCTION_TARGET = '2' THEN '入学金' ";
                $query .= "          ELSE '' ";
                $query .= "     END AS SUB_TATLE, ";
            } else {
                $query .= "     '{$subTitle}' AS SUB_TATLE, ";
            }
            if ($sort == "2") {
                $query .= "     ADD_OFFSET_FLG AS OFFSET_FLG, ";
                $query .= "     ADD_REFUND_FLG AS REFUND_FLG, ";
            } elseif ($sort == "5") {
                $query .= "     '1' AS OFFSET_FLG, ";
                $query .= "     '' AS REFUND_FLG, ";
            } else {
                $query .= "     OFFSET_FLG, ";
                $query .= "     REFUND_FLG, ";
            }
            $query .= "     PLAN_MONTH, ";
            $query .= "     SUM(VALUE({$fieldName}, 0)) AS PLAN_MONEY ";
            $query .= " FROM ";
            $query .= "     {$tableName} ";
            $query .= " GROUP BY ";
            $query .= "     SLIP_NO, ";
            if ($sort == "3" || $sort == "4") {
                $query .= "     REDUCTION_TARGET, ";
            }
            if ($sort == "2") {
                $query .= "     ADD_OFFSET_FLG, ";
                $query .= "     ADD_REFUND_FLG, ";
            } elseif ($sort == "5") {
            } else {
                $query .= "     OFFSET_FLG, ";
                $query .= "     REFUND_FLG, ";
            }
            $query .= "     PLAN_MONTH ";
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "     {$sort} AS BASE_SORT, ";
            $query .= "     {$setSort} AS SORT, ";
            $query .= "     SLIP_NO, ";
            $query .= "     '{$title}' AS KOUMOKU, ";
            if ($sort == "3" || $sort == "4") {
                $query .= "     CASE WHEN REDUCTION_TARGET = '1' THEN '授業料等' ";
                $query .= "          WHEN REDUCTION_TARGET = '2' THEN '入学金' ";
                $query .= "          ELSE '' ";
                $query .= "     END AS SUB_TATLE, ";
            } else {
                $query .= "     '{$subTitle}' AS SUB_TATLE, ";
            }
            $query .= "     '99' AS OFFSET_FLG, ";
            $query .= "     '99' AS REFUND_FLG, ";
            $query .= "     '99' AS PLAN_MONTH, ";
            $query .= "     sum({$fieldName}) AS PLAN_MONEY ";
            $query .= " FROM ";
            $query .= "     {$tableName} ";
            $query .= " GROUP BY ";
            $query .= "     SLIP_NO ";
            if ($sort == "3" || $sort == "4") {
                $query .= "     ,REDUCTION_TARGET ";
            }
            $union = " UNION ";
        }

        $query .= " ORDER BY ";
        $query .= "     BASE_SORT, ";
        $query .= "     SLIP_NO, ";
        $query .= "     SORT ";

        return $query;
    }

    //各伝票情報
    public function getMainQuery($model)
    {
        $query  = " WITH MAIN_T AS ( ";
        $query .= "         SELECT ";
        $query .= "             PM_D.*, ";
        $query .= "             INT(CASE WHEN CM_M.SHOW_ORDER IS NULL THEN '100' ELSE CM_M.SHOW_ORDER END) AS SHOW_ORDER, ";
        $query .= "             CM_M.COLLECT_M_NAME, ";
        $query .= "             CM_M.GAKUNOKIN_DIV ";
        $query .= "         FROM ";
        $query .= "             COLLECT_SLIP_PLAN_M_DAT PM_D ";
        $query .= "             LEFT JOIN COLLECT_SLIP_DAT SL_D ON SL_D.SCHOOLCD    = PM_D.SCHOOLCD ";
        $query .= "                                            AND SL_D.SCHOOL_KIND = PM_D.SCHOOL_KIND ";
        $query .= "                                            AND SL_D.YEAR        = PM_D.YEAR ";
        $query .= "                                            AND SL_D.SCHREGNO    = PM_D.SCHREGNO ";
        $query .= "                                            AND SL_D.SLIP_NO     = PM_D.SLIP_NO ";
        $query .= "             LEFT JOIN COLLECT_M_MST CM_M ON CM_M.SCHOOLCD     = PM_D.SCHOOLCD ";
        $query .= "                                         AND CM_M.SCHOOL_KIND  = PM_D.SCHOOL_KIND ";
        $query .= "                                         AND CM_M.YEAR         = PM_D.YEAR ";
        $query .= "                                         AND CM_M.COLLECT_L_CD = PM_D.COLLECT_L_CD ";
        $query .= "                                         AND CM_M.COLLECT_M_CD = PM_D.COLLECT_M_CD ";
        $query .= "         WHERE ";
        $query .= "                 PM_D.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "             AND PM_D.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "             AND PM_D.YEAR        = '{$model->year}' ";
        $query .= "             AND PM_D.SCHREGNO    = '{$model->schregno}' ";
        $query .= "             AND SL_D.CANCEL_DATE IS NULL ";
        $query .= " ), PAID_T AS ( ";
        $query .= "         SELECT ";
        $query .= "             MAIN.*, ";
        $query .= "             VALUE(PAID.PLAN_PAID_MONEY, 0) AS PLAN_PAID_MONEY ";
        $query .= "         FROM ";
        $query .= "             MAIN_T MAIN ";
        $query .= "             LEFT JOIN COLLECT_SLIP_PLAN_PAID_M_DAT PAID ON MAIN.SCHOOLCD     = PAID.SCHOOLCD ";
        $query .= "                                                        AND MAIN.SCHOOL_KIND  = PAID.SCHOOL_KIND ";
        $query .= "                                                        AND MAIN.YEAR         = PAID.YEAR ";
        $query .= "                                                        AND MAIN.SCHREGNO     = PAID.SCHREGNO ";
        $query .= "                                                        AND MAIN.SLIP_NO      = PAID.SLIP_NO ";
        $query .= "                                                        AND MAIN.COLLECT_L_CD = PAID.COLLECT_L_CD ";
        $query .= "                                                        AND MAIN.COLLECT_M_CD = PAID.COLLECT_M_CD ";
        $query .= "                                                        AND MAIN.PLAN_YEAR    = PAID.PLAN_YEAR ";
        $query .= "                                                        AND MAIN.PLAN_MONTH   = PAID.PLAN_MONTH ";
        $query .= " ) ";

        //伝票毎の各項目(計画)
        $query .= " SELECT ";
        $query .= "     SLIP_NO, ";
        $query .= "     SHOW_ORDER, ";
        $query .= "     INT(COLLECT_L_CD) AS COLLECT_L_CD, ";
        $query .= "     INT(COLLECT_M_CD) AS COLLECT_M_CD, ";
        $query .= "     COLLECT_M_NAME, ";
        $query .= "     1 AS PLAN_PAID, ";
        $query .= "     GAKUNOKIN_DIV AS JUGYOURYOU_FLG, ";
        $query .= "     PLAN_MONTH, ";
        $query .= "     '' AS PAID_LIMIT_MONTH, ";
        $query .= "     PLAN_MONEY ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        //各項目の横計(計画)
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     SLIP_NO, ";
        $query .= "     SHOW_ORDER, ";
        $query .= "     INT(COLLECT_L_CD) AS COLLECT_L_CD, ";
        $query .= "     INT(COLLECT_M_CD) AS COLLECT_M_CD, ";
        $query .= "     COLLECT_M_NAME, ";
        $query .= "     1 AS PLAN_PAID, ";
        $query .= "     '' AS JUGYOURYOU_FLG, ";
        $query .= "     '99' AS PLAN_MONTH, ";
        $query .= "     '' AS PAID_LIMIT_MONTH, ";
        $query .= "     sum(PLAN_MONEY) AS PLAN_MONEY ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " GROUP BY ";
        $query .= "     SLIP_NO,   ";
        $query .= "     SHOW_ORDER, ";
        $query .= "     COLLECT_L_CD, ";
        $query .= "     COLLECT_M_CD, ";
        $query .= "     COLLECT_M_NAME ";

        //伝票毎の各項目(入金)
        $query .= " UNION  ";
        $query .= " SELECT ";
        $query .= "     SLIP_NO, ";
        $query .= "     SHOW_ORDER, ";
        $query .= "     INT(COLLECT_L_CD) AS COLLECT_L_CD, ";
        $query .= "     INT(COLLECT_M_CD) AS COLLECT_M_CD, ";
        $query .= "     COLLECT_M_NAME, ";
        $query .= "     2 AS PLAN_PAID, ";
        $query .= "     GAKUNOKIN_DIV AS JUGYOURYOU_FLG, ";
        $query .= "     PLAN_MONTH, ";
        $query .= "     '' AS PAID_LIMIT_MONTH, ";
        $query .= "     PLAN_PAID_MONEY AS PLAN_MONEY ";
        $query .= " FROM ";
        $query .= "     PAID_T ";
        //各項目の横計(入金)
        $query .= " UNION  ";
        $query .= " SELECT ";
        $query .= "     SLIP_NO, ";
        $query .= "     SHOW_ORDER, ";
        $query .= "     INT(COLLECT_L_CD) AS COLLECT_L_CD, ";
        $query .= "     INT(COLLECT_M_CD) AS COLLECT_M_CD, ";
        $query .= "     COLLECT_M_NAME, ";
        $query .= "     2 AS PLAN_PAID, ";
        $query .= "     '' AS JUGYOURYOU_FLG, ";
        $query .= "     '99' AS PLAN_MONTH, ";
        $query .= "     '' AS PAID_LIMIT_MONTH, ";
        $query .= "     sum(PLAN_PAID_MONEY) AS PLAN_MONEY ";
        $query .= " FROM ";
        $query .= "     PAID_T ";
        $query .= " GROUP BY ";
        $query .= "     SLIP_NO, ";
        $query .= "     SHOW_ORDER, ";
        $query .= "     COLLECT_L_CD, ";
        $query .= "     COLLECT_M_CD, ";
        $query .= "     COLLECT_M_NAME ";

        //電票毎の各月の縦計
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     SLIP_NO, ";
        $query .= "     101 AS SHOW_ORDER, ";
        $query .= "     101 AS COLLECT_L_CD, ";
        $query .= "     101 AS COLLECT_M_CD, ";
        $query .= "     '伝票合計（' || substr(SLIP_NO, 13, 3) || '）' AS COLLECT_M_NAME, ";
        $query .= "     1 AS PLAN_PAID, ";
        $query .= "     '' AS JUGYOURYOU_FLG, ";
        $query .= "     PLAN_MONTH, ";
        $query .= "     '' AS PAID_LIMIT_MONTH, ";
        $query .= "     sum(PLAN_MONEY) AS PLAN_MONEY ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " GROUP BY ";
        $query .= "     SLIP_NO,   ";
        $query .= "     PLAN_MONTH ";
        //電票毎の縦計の横計
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     SLIP_NO, ";
        $query .= "     101 AS SHOW_ORDER, ";
        $query .= "     101 AS COLLECT_L_CD, ";
        $query .= "     101 AS COLLECT_M_CD, ";
        $query .= "     '伝票合計（' || substr(SLIP_NO, 13, 3) || '）' AS COLLECT_M_NAME, ";
        $query .= "     1 AS PLAN_PAID, ";
        $query .= "     '' AS JUGYOURYOU_FLG, ";
        $query .= "     '99' AS PLAN_MONTH, ";
        $query .= "     '' AS PAID_LIMIT_MONTH, ";
        $query .= "     sum(PLAN_MONEY) AS PLAN_MONEY ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " GROUP BY ";
        $query .= "     SLIP_NO ";

        //納期限
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     MAIN.SLIP_NO, ";
        $query .= "     102 AS SHOW_ORDER, ";
        $query .= "     102 AS COLLECT_L_CD, ";
        $query .= "     102 AS COLLECT_M_CD, ";
        $query .= "     '納期限' AS COLLECT_M_NAME, ";
        $query .= "     2 AS PLAN_PAID, ";
        $query .= "     '' AS JUGYOURYOU_FLG, ";
        $query .= "     MAIN.PLAN_MONTH, ";
        $query .= "     LIMI.PAID_LIMIT_MONTH, ";
        $query .= "     INT(LIMI.PAID_LIMIT_DATE) AS PLAN_MONEY ";
        $query .= " FROM ";
        $query .= "     MAIN_T MAIN ";
        $query .= "     LEFT JOIN COLLECT_SLIP_PLAN_LIMITDATE_DAT LIMI ON MAIN.SCHOOLCD    = LIMI.SCHOOLCD ";
        $query .= "                                                   AND MAIN.SCHOOL_KIND = LIMI.SCHOOL_KIND ";
        $query .= "                                                   AND MAIN.YEAR        = LIMI.YEAR ";
        $query .= "                                                   AND MAIN.SCHREGNO    = LIMI.SCHREGNO ";
        $query .= "                                                   AND MAIN.SLIP_NO     = LIMI.SLIP_NO ";
        $query .= "                                                   AND MAIN.PLAN_YEAR   = LIMI.PLAN_YEAR ";
        $query .= "                                                   AND MAIN.PLAN_MONTH  = LIMI.PLAN_MONTH ";

        $query .= " ORDER BY ";
        $query .= "     SLIP_NO, ";
        $query .= "     SHOW_ORDER, ";
        $query .= "     INT(COLLECT_L_CD), ";
        $query .= "     INT(COLLECT_M_CD), ";
        $query .= "     PLAN_PAID ";

        return $query;
    }

    //伝票合算情報
    public function getMainQuery2($model, $slipNo = "")
    {
        $query  = " WITH MAIN_T AS ( ";
        $query .= "         SELECT ";
        $query .= "             PM_D.*, ";
        $query .= "             CM_M.SHOW_ORDER, ";
        $query .= "             CM_M.COLLECT_M_NAME, ";
        $query .= "             CM_M.GAKUNOKIN_DIV ";
        $query .= "         FROM ";
        $query .= "             COLLECT_SLIP_PLAN_M_DAT PM_D ";
        $query .= "             LEFT JOIN COLLECT_SLIP_DAT SL_D ON SL_D.SCHOOLCD    = PM_D.SCHOOLCD ";
        $query .= "                                            AND SL_D.SCHOOL_KIND = PM_D.SCHOOL_KIND ";
        $query .= "                                            AND SL_D.YEAR        = PM_D.YEAR ";
        $query .= "                                            AND SL_D.SCHREGNO    = PM_D.SCHREGNO ";
        $query .= "                                            AND SL_D.SLIP_NO     = PM_D.SLIP_NO ";
        $query .= "             LEFT JOIN COLLECT_M_MST CM_M ON CM_M.SCHOOLCD     = PM_D.SCHOOLCD ";
        $query .= "                                         AND CM_M.SCHOOL_KIND  = PM_D.SCHOOL_KIND ";
        $query .= "                                         AND CM_M.YEAR         = PM_D.YEAR ";
        $query .= "                                         AND CM_M.COLLECT_L_CD = PM_D.COLLECT_L_CD ";
        $query .= "                                         AND CM_M.COLLECT_M_CD = PM_D.COLLECT_M_CD ";
        $query .= "         WHERE ";
        $query .= "                 PM_D.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "             AND PM_D.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "             AND PM_D.YEAR        = '{$model->year}' ";
        if ($slipNo) {
            $query .= "             AND PM_D.SLIP_NO     = '{$slipNo}' ";
        }
        $query .= "             AND PM_D.SCHREGNO    = '{$model->schregno}' ";
        $query .= "             AND SL_D.CANCEL_DATE IS NULL ";
        //入金データ
        $query .= " ), PAID_DATA AS ( ";
        $query .= "         SELECT ";
        $query .= "             PAID.* ";
        $query .= "         FROM ";
        $query .= "             COLLECT_SLIP_PLAN_PAID_DAT PAID ";
        $query .= "             LEFT JOIN COLLECT_SLIP_DAT SL_D ";
        $query .= "                  ON SL_D.SCHOOLCD    = PAID.SCHOOLCD ";
        $query .= "                 AND SL_D.SCHOOL_KIND = PAID.SCHOOL_KIND ";
        $query .= "                 AND SL_D.YEAR        = PAID.YEAR ";
        $query .= "                 AND SL_D.SLIP_NO     = PAID.SLIP_NO ";
        $query .= "         WHERE ";
        $query .= "                 PAID.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "             AND PAID.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "             AND PAID.YEAR        = '{$model->year}' ";
        if ($slipNo) {
            $query .= "             AND PAID.SLIP_NO     = '{$slipNo}' ";
        }
        $query .= "             AND PAID.SCHREGNO    = '{$model->schregno}' ";
        $query .= "             AND SL_D.CANCEL_DATE IS NULL ";
        $query .= " ) ";
        //入金必要額(各月)
        $query .= " SELECT ";
        $query .= "     1 AS SORT, ";
        $query .= "     '入金必要額' AS M_NAME, ";
        $query .= "     PLAN_MONTH, ";
        $query .= "     sum(PLAN_MONEY) AS ELEMENT ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " GROUP BY ";
        $query .= "     PLAN_MONTH ";
        //入金必要額(各月)の横計
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     1 AS SORT, ";
        $query .= "     '入金必要額' AS M_NAME, ";
        $query .= "     '99' AS PLAN_MONTH, ";
        $query .= "     sum(PLAN_MONEY) AS ELEMENT ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        //入金(各月)
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     2 AS SORT, ";
        $query .= "     '入金額計' AS M_NAME, ";
        $query .= "     PLAN_MONTH, ";
        $query .= "     sum(PLAN_PAID_MONEY) AS ELEMENT ";
        $query .= " FROM ";
        $query .= "     PAID_DATA ";
        $query .= " GROUP BY ";
        $query .= "     PLAN_MONTH ";
        //入金(各月)の横計
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     2 AS SORT, ";
        $query .= "     '入金額計' AS M_NAME, ";
        $query .= "     '99' AS PLAN_MONTH, ";
        $query .= "     sum(PLAN_PAID_MONEY) AS ELEMENT ";
        $query .= " FROM ";
        $query .= "     PAID_DATA ";
        //入金日(各月)
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     3 AS SORT, ";
        $query .= "     '入金日付' AS M_NAME, ";
        $query .= "     PLAN_MONTH, ";
        $query .= "     INT(max(PLAN_PAID_MONEY_DATE)) AS ELEMENT ";
        $query .= " FROM ";
        $query .= "     PAID_DATA ";
        $query .= " GROUP BY ";
        $query .= "     PLAN_MONTH ";
        $query .= " ORDER BY ";
        $query .= "     SORT ";

        return $query;
    }

    //伝票+各項目毎の入金情報
    public function getSlipMonthLMPaidMoney($model)
    {
        $query  = " SELECT ";
        $query .= "     PAID.SLIP_NO || '-' || PAID.COLLECT_L_CD || '-' || PAID.COLLECT_M_CD || '-' || PAID.PLAN_MONTH AS KEY_ID, ";
        $query .= "     sum(PAID.PLAN_PAID_MONEY) AS PLAN_PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PLAN_PAID_M_DAT PAID ";
        $query .= "     LEFT JOIN COLLECT_SLIP_DAT SL_D ";
        $query .= "          ON SL_D.SCHOOLCD    = PAID.SCHOOLCD ";
        $query .= "         AND SL_D.SCHOOL_KIND = PAID.SCHOOL_KIND ";
        $query .= "         AND SL_D.YEAR        = PAID.YEAR ";
        $query .= "         AND SL_D.SLIP_NO     = PAID.SLIP_NO ";
        $query .= " WHERE ";
        $query .= "         PAID.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND PAID.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND PAID.YEAR        = '{$model->year}' ";
        $query .= "     AND PAID.SCHREGNO    = '{$model->schregno}' ";
        $query .= "     AND SL_D.CANCEL_DATE IS NULL ";
        $query .= " GROUP BY ";
        $query .= "     PAID.SLIP_NO, ";
        $query .= "     PAID.COLLECT_L_CD, ";
        $query .= "     PAID.COLLECT_M_CD, ";
        $query .= "     PAID.PLAN_YEAR, ";
        $query .= "     PAID.PLAN_MONTH ";
        $query .= " UNION ";
        //伝票毎の入金情報(納期限テキストボックス制限に使用)
        $query .= " SELECT ";
        $query .= "     PAID.SLIP_NO || '-102-102-' || PAID.PLAN_MONTH AS KEY_ID, ";
        $query .= "     sum(PAID.PLAN_PAID_MONEY) AS PLAN_PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PLAN_PAID_DAT PAID ";
        $query .= "     LEFT JOIN COLLECT_SLIP_DAT SL_D ";
        $query .= "          ON SL_D.SCHOOLCD    = PAID.SCHOOLCD ";
        $query .= "         AND SL_D.SCHOOL_KIND = PAID.SCHOOL_KIND ";
        $query .= "         AND SL_D.YEAR        = PAID.YEAR ";
        $query .= "         AND SL_D.SLIP_NO     = PAID.SLIP_NO ";
        $query .= " WHERE ";
        $query .= "         PAID.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND PAID.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND PAID.YEAR        = '{$model->year}' ";
        $query .= "     AND PAID.SCHREGNO    = '{$model->schregno}' ";
        $query .= "     AND SL_D.CANCEL_DATE IS NULL ";
        $query .= " GROUP BY ";
        $query .= "     PAID.SLIP_NO, ";
        $query .= "     PAID.PLAN_YEAR, ";
        $query .= "     PAID.PLAN_MONTH ";

        return $query;
    }

    //伝票毎の入金情報
    public function getSlipMonthPaidMoney($model)
    {
        $query  = " SELECT ";
        $query .= "     PAID.SLIP_NO || '-' || PAID.PLAN_MONTH AS KEY_ID, ";
        $query .= "     sum(PAID.PLAN_PAID_MONEY) AS PLAN_PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PLAN_PAID_DAT PAID ";
        $query .= "     LEFT JOIN COLLECT_SLIP_DAT SL_D ";
        $query .= "          ON SL_D.SCHOOLCD    = PAID.SCHOOLCD ";
        $query .= "         AND SL_D.SCHOOL_KIND = PAID.SCHOOL_KIND ";
        $query .= "         AND SL_D.YEAR        = PAID.YEAR ";
        $query .= "         AND SL_D.SLIP_NO     = PAID.SLIP_NO ";
        $query .= " WHERE ";
        $query .= "         PAID.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND PAID.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND PAID.YEAR        = '{$model->year}' ";
        $query .= "     AND PAID.SCHREGNO    = '{$model->schregno}' ";
        $query .= "     AND SL_D.CANCEL_DATE IS NULL ";
        $query .= " GROUP BY ";
        $query .= "     PAID.SLIP_NO, ";
        $query .= "     PAID.PLAN_YEAR, ";
        $query .= "     PAID.PLAN_MONTH ";

        return $query;
    }

    /****************/
    /**  更新処理  **/
    /****************/
    //UPDATE
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $upDateFlg = false;

        //伝票の項目ごとに更新（COLLECT_SLIP_PLAN_PAID_M_DAT）
        foreach ($model->slipArr as $keyId => $val) {
            list($slipNo, $lcd, $mcd) = explode("-", $keyId);
            $isGenmen = false;

            //チェックありを更新
            if ($model->field["CHECK-".$keyId] == "1") {
                $totalPlanMoney = 0;
                foreach ($model->monthArray as $month) {
                    //納期限は、COLLECT_SLIP_PLAN_LIMITDATE_DATを更新
                    if ($lcd == "102") {
                        $setLimitDate = str_replace("/", "-", $model->field["COLLECT_MONTH_".$keyId."-".$month]);

                        $data = array();
                        $data["PAID_LIMIT_MONTH"][TEXT] = $model->field["PAID_LIMIT_MONTH_".$keyId."-".$month];
                        $data["PAID_LIMIT_DATE"][DATE]  = $setLimitDate;
                        $data["REGISTERCD"][TEXT]       = STAFFCD;
                        $data["UPDATED"][NUMBER]        = "sysdate()";

                        $planYear = ($month == "01" || $month == "02" || $month == "03") ? $model->year + 1: $model->year;
                        $where  = " WHERE SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' ";
                        $where .= "   AND SCHOOL_KIND  = '".$model->schoolKind."' ";
                        $where .= "   AND YEAR         = '{$model->year}' ";
                        $where .= "   AND SCHREGNO     = '{$model->schregno}' ";
                        $where .= "   AND SLIP_NO      = '{$slipNo}' ";
                        $where .= "   AND PLAN_YEAR    = '{$planYear}' ";
                        $where .= "   AND PLAN_MONTH   = '{$month}' ";

                        $query = Query::updateSQL($data, "COLLECT_SLIP_PLAN_LIMITDATE_DAT", $where);
                        $db->query($query);
                    } elseif (strpos($lcd, "GEN") !== false) {
                        $isGenmen = true;
                        $setPlanMoney = ($model->field["COLLECT_MONTH_".$keyId."-".$month]) ? $model->field["COLLECT_MONTH_".$keyId."-".$month]: 0;

                        $tableKey = str_replace("GEN", "", $lcd);

                        $setTable = array("1"  => "REDUCTION_COUNTRY_PLAN_DAT",
                                          "2"  => "REDUCTION_COUNTRY_PLAN_DAT",
                                          "31" => "REDUCTION_PLAN_DAT",
                                          "32" => "REDUCTION_PLAN_DAT",
                                          "41" => "REDUCTION_SCHOOL_PLAN_DAT",
                                          "42" => "REDUCTION_SCHOOL_PLAN_DAT",
                                          "5"  => "REDUCTION_BURDEN_CHARGE_PLAN_DAT");
                        $fieldName = "DECISION_MONEY";
                        if ($tableKey == "2") {
                            $fieldName = "ADD_DECISION_MONEY";
                        } elseif ($tableKey == "5") {
                            $fieldName = "BURDEN_CHARGE";
                        }
                        $target = "";
                        if ($tableKey == "31" || $tableKey == "41") {
                            $target = "1";
                        } elseif ($tableKey == "32" || $tableKey == "42") {
                            $target = "2";
                        }

                        $data = array();
                        $data[$fieldName][NUMBER]       = $setPlanMoney;
                        $data["REGISTERCD"][TEXT]       = STAFFCD;
                        $data["UPDATED"][NUMBER]        = "sysdate()";

                        $planYear = ($month == "01" || $month == "02" || $month == "03") ? $model->year + 1: $model->year;
                        $where  = " WHERE SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' ";
                        $where .= "   AND SCHOOL_KIND  = '".$model->schoolKind."' ";
                        $where .= "   AND YEAR         = '{$model->year}' ";
                        if ($target) {
                            $where .= "   AND REDUCTION_TARGET = '{$target}' ";
                        }
                        $where .= "   AND SLIP_NO      = '{$slipNo}' ";
                        $where .= "   AND PLAN_YEAR    = '{$planYear}' ";
                        $where .= "   AND PLAN_MONTH   = '{$month}' ";
                        $where .= "   AND SCHREGNO     = '{$model->schregno}' ";

                        $query = Query::updateSQL($data, $setTable[$tableKey], $where);
                        $db->query($query);
                    } else {
                        $setPlanMoney = ($model->field["COLLECT_MONTH_".$keyId."-".$month]) ? $model->field["COLLECT_MONTH_".$keyId."-".$month]: 0;
                        $totalPlanMoney += $setPlanMoney;

                        $data = array();
                        $data["PLAN_MONEY"][NUMBER]     = $setPlanMoney;
                        $data["REGISTERCD"][TEXT]       = STAFFCD;
                        $data["UPDATED"][NUMBER]        = "sysdate()";

                        $planYear = ($month == "01" || $month == "02" || $month == "03") ? $model->year + 1: $model->year;
                        $where  = " WHERE SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' ";
                        $where .= "   AND SCHOOL_KIND  = '".$model->schoolKind."' ";
                        $where .= "   AND YEAR         = '{$model->year}' ";
                        $where .= "   AND SCHREGNO     = '{$model->schregno}' ";
                        $where .= "   AND SLIP_NO      = '{$slipNo}' ";
                        $where .= "   AND COLLECT_L_CD = '".sprintf("%02d", $lcd)."' ";
                        $where .= "   AND COLLECT_M_CD = '".sprintf("%02d", $mcd)."' ";
                        $where .= "   AND PLAN_YEAR    = '{$planYear}' ";
                        $where .= "   AND PLAN_MONTH   = '{$month}' ";

                        $query = Query::updateSQL($data, "COLLECT_SLIP_PLAN_M_DAT", $where);
                        $db->query($query);
                    }
                }

                if ($lcd != "102" && !$isGenmen) {
                    //COLLECT_SLIP_M_DAT
                    $data = array();
                    $data["COLLECT_MONEY"][NUMBER]  = $totalPlanMoney;
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][NUMBER]        = "sysdate()";

                    $where  = " WHERE SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' ";
                    $where .= "   AND SCHOOL_KIND  = '".$model->schoolKind."' ";
                    $where .= "   AND YEAR         = '{$model->year}' ";
                    $where .= "   AND SLIP_NO      = '{$slipNo}' ";
                    $where .= "   AND COLLECT_L_CD = '".sprintf("%02d", $lcd)."' ";
                    $where .= "   AND COLLECT_M_CD = '".sprintf("%02d", $mcd)."' ";

                    $query = Query::updateSQL($data, "COLLECT_SLIP_M_DAT", $where);
                    $db->query($query);
                }

                //COLLECT_SLIP_DAT
                if (!$upDateFlg && $model->field["CHANGE_FLG"] == "1" && !$isGenmen) {
                    $data = array();
                    $data["COLLECT_PATTERN_CD"][TEXT]   = '00';
                    $data["REGISTERCD"][TEXT]           = STAFFCD;
                    $data["UPDATED"][NUMBER]            = "sysdate()";

                    $where  = " WHERE SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' ";
                    $where .= "   AND SCHOOL_KIND  = '".$model->schoolKind."' ";
                    $where .= "   AND YEAR         = '{$model->year}' ";
                    $where .= "   AND SLIP_NO      = '{$slipNo}' ";

                    $query = Query::updateSQL($data, "COLLECT_SLIP_DAT", $where);
                    $db->query($query);
                    $upDateFlg = true;
                }
            }
        }
        $db->commit();
        Query::dbCheckIn($db);
    }

    public function getExistsContactCnt($db, $model)
    {
        $query .= "    SELECT ";
        $query .= "        COUNT(*) ";
        $query .= "    FROM ";
        $query .= "        COLLECT_CONTACT_HISTORY_DAT ";
        $query .= "    WHERE ";
        $query .= "        SCHREGNO = '{$model->schregno}' ";

        $rtnVal = $db->getOne($query);
        return $rtnVal;
    }
}
