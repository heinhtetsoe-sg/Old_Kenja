<?php

require_once('for_php7.php');

class knjp739Query extends Query {

    //名称マスタ
    function getNameMst($namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == 'P004') {
            $query .= "     AND NAMECD2 <> '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種
    function getSchoolKind($model) {
        if ($model->search["SEARCH_DIV"] == "1") {
            $setYear = (CTRL_YEAR + 1);
        } else {
            $setYear = CTRL_YEAR;
        }

        $query  = " SELECT DISTINCT ";
        $query .= "     GDAT.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = REGD.YEAR ";
        $query .= "                                    AND GDAT.GRADE = REGD.GRADE ";
        $query .= " WHERE ";
        $query .= "         REGD.YEAR     = '{$setYear}' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        //学年
        if ($model->search["GRADE"]) {
            $query .= "     AND GDAT.GRADE = '".$model->search["GRADE"]."' ";
        }
        //年組
        if ($model->search["HR_CLASS"]) {
            $query .= "     AND GDAT.GRADE || REGD.HR_CLASS = '".$model->search["HR_CLASS"]."' ";
        }
        //学籍番号
        if ($model->search["SCHREGNO"]) {
            $query .= "     AND REGD.SCHREGNO = '".$model->search["SCHREGNO"]."' ";
        }
        //氏名
        if ($model->search["NAME"]) {
            $query .= "     AND BASE.NAME LIKE '%" .str_replace("'","''",$model->search["NAME"]). "%' ";
        }
        //氏名かな
        if ($model->search["NAME_KANA"]) {
            $query .= "     AND BASE.NAME_KANA LIKE '%" .str_replace("'","''",$model->search["NAME_KANA"]). "%' ";
        }

        return $query;
    }

    function getStudentSearch($model) {
        $and = "";
        $where = "";

        if ($model->search["SEARCH_DIV"] == "1") {
            $where .= "   ".$and."MAIN.ENTERYEAR = '".(CTRL_YEAR + 1)."' ";
            $and = "AND ";
        } else {
            $where .= "   ".$and."VALUE(MAIN.GRD_DIV, '0') NOT IN ('1', '2', '3', '9') ";
            $and = "AND ";
        }
        //入学年度
        if ($model->search["ENT_YEAR"]) {
            if ($model->search["SEARCH_DIV"] == "1") {
                $where .= "   ".$and."MAIN.ENTERYEAR = '".$model->search["ENT_YEAR"]."' ";
                $and = "AND ";
            } else {
                $where .= "   ".$and."FISCALYEAR(MAIN.ENT_DATE) = '".$model->search["ENT_YEAR"]."' ";
                $and = "AND ";
            }
        }
        //入学区分
        if ($model->search["A028"]) {
            $where .= "   ".$and."substr(MAIN.EXAMNO, 2) BETWEEN A028.NAMESPARE1 AND A028.NAMESPARE2 ";
            $and = "AND ";
        }
        //卒業予定年度
        if ($model->search["GRD_YEAR"]) {
            if ($model->search["SEARCH_DIV"] == "1") {
                $where .= "   ".$and."(CAST(MAIN.ENTERYEAR AS SMALLINT) + 3) = ".$model->search["GRD_YEAR"]." ";
                $and = "AND ";
            } else {
                $where .= "   ".$and."FISCALYEAR(MAIN.GRD_DATE) = '".$model->search["GRD_YEAR"]."' ";
                $and = "AND ";
            }
        }
        //学年
        if ($model->search["GRADE"]) {
            $where .= "   ".$and."SCH_R.GRADE = '".$model->search["GRADE"]."' ";
            $and = "AND ";
        }
        //年組
        if ($model->search["HR_CLASS"]) {
            $where .= "   ".$and."SCH_R.GRADE || SCH_R.HR_CLASS = '".$model->search["HR_CLASS"]."' ";
            $and = "AND ";
        }
        //学籍番号
        if ($model->search["SCHREGNO"]) {
            $where .= "   ".$and."MAIN.SCHREGNO = '".$model->search["SCHREGNO"]."' ";
            $and = "AND ";
        }
        //氏名
        if ($model->search["NAME"]) {
            $where .= "   ".$and."MAIN.NAME LIKE '%" .str_replace("'","''",$model->search["NAME"]). "%' ";
            $and = "AND ";
        }
        //氏名かな
        if ($model->search["NAME_KANA"]) {
            $where .= "   ".$and."MAIN.NAME_KANA LIKE '%" .str_replace("'","''",$model->search["NAME_KANA"]). "%' ";
            $and = "AND ";
        }

        return $where;
    }

    //生徒情報取得
    function getStudentInfoData($model) {
        if ($model->search["SEARCH_DIV"] == "1") {
            $setYear = (CTRL_YEAR + 1);
        } else {
            $setYear = CTRL_YEAR;
        }

        $tableName = "V_SCHREG_BASE_MST";
        if ($model->search["SEARCH_DIV"] == "1") {
            $tableName = "FRESHMAN_DAT";
        }
        if ($model->field["PAID_LIMIT_MONTH"] == "12") {
            $setLmonth = "01";
        } else {
            $setLmonth = sprintf("%02d", ($model->field["PAID_LIMIT_MONTH"] + 1));
        }
        if ($model->field["PAID_LIMIT_MONTH"] < "04" || $model->field["PAID_LIMIT_MONTH"] == "12") {
            $setLimitYear = $setYear + 1;
        } else {
            $setLimitYear = $setYear;
        }
        $setLimitDate = $setLimitYear."-".$setLmonth."-01";

        $where = knjp739Query::getStudentSearch($model);
        $and = $where == "" ? "" : "AND ";
        $query  = " WITH PRINT_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         PLAN_M.SLIP_NO, ";
        $query .= "         PLAN_M.SCHREGNO, ";
        $query .= "         SUM(VALUE(PLAN_M.PLAN_MONEY, 0)) AS PLAN_MONEY, ";
        $query .= "         SUM(CASE WHEN COL_M.TEXTBOOKDIV IS NOT NULL THEN 1 ELSE 0 END) AS TEXTBOOK_CNT, ";
        $query .= "         LIMIT_D.PAID_LIMIT_DATE ";
        $query .= "     FROM ";
        $query .= "         COLLECT_SLIP_PLAN_M_DAT PLAN_M ";
        $query .= "         LEFT JOIN COLLECT_SLIP_DAT SL_D ON SL_D.SCHOOLCD    = PLAN_M.SCHOOLCD ";
        $query .= "                                        AND SL_D.SCHOOL_KIND = PLAN_M.SCHOOL_KIND ";
        $query .= "                                        AND SL_D.YEAR        = PLAN_M.YEAR ";
        $query .= "                                        AND SL_D.SLIP_NO     = PLAN_M.SLIP_NO ";
        $query .= "         INNER JOIN COLLECT_SLIP_PLAN_LIMITDATE_DAT LIMIT_D ON PLAN_M.SCHOOLCD         = LIMIT_D.SCHOOLCD ";
        $query .= "                                                           AND PLAN_M.SCHOOL_KIND      = LIMIT_D.SCHOOL_KIND ";
        $query .= "                                                           AND PLAN_M.YEAR             = LIMIT_D.YEAR ";
        $query .= "                                                           AND PLAN_M.SCHREGNO         = LIMIT_D.SCHREGNO ";
        $query .= "                                                           AND PLAN_M.SLIP_NO          = LIMIT_D.SLIP_NO ";
        $query .= "                                                           AND PLAN_M.PLAN_YEAR        = LIMIT_D.PLAN_YEAR ";
        $query .= "                                                           AND PLAN_M.PLAN_MONTH       = LIMIT_D.PLAN_MONTH ";
        $query .= "                                                           AND LIMIT_D.PAID_LIMIT_DATE < '{$setLimitDate}' ";
        $query .= "         LEFT JOIN COLLECT_M_MST COL_M ON PLAN_M.SCHOOLCD = COL_M.SCHOOLCD ";
        $query .= "              AND PLAN_M.SCHOOL_KIND = COL_M.SCHOOL_KIND ";
        $query .= "              AND PLAN_M.YEAR = COL_M.YEAR ";
        $query .= "              AND PLAN_M.COLLECT_L_CD = COL_M.COLLECT_L_CD ";
        $query .= "              AND PLAN_M.COLLECT_M_CD = COL_M.COLLECT_M_CD ";
        $query .= "     WHERE ";
        $query .= "             PLAN_M.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        if ($model->schFlg) {
            if ($model->selectSchoolKind) {
                $query .= "         AND PLAN_M.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else {
            $query .= "         AND PLAN_M.SCHOOL_KIND = '{$model->schoolKind}' ";
        }
        $query .= "         AND PLAN_M.YEAR        = '{$setYear}' ";
        $query .= "         AND SL_D.CANCEL_DATE IS NULL ";
        $query .= "     GROUP BY ";
        $query .= "         PLAN_M.SLIP_NO, ";
        $query .= "         PLAN_M.SCHREGNO, ";
        $query .= "         LIMIT_D.PAID_LIMIT_DATE ";
        $query .= "     HAVING ";
        $query .= "         SUM(VALUE(PLAN_M.PAID_MONEY, 0)) <= 0 ";
        $query .= "         AND SUM(CASE WHEN COL_M.TEXTBOOKDIV IS NOT NULL THEN 1 ELSE 0 END) > 0 ";
        $query .= "     ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     GDAT.SCHOOL_KIND, ";
        $query .= "     SCH_R.GRADE || SCH_R.HR_CLASS || SCH_R.ATTENDNO, ";
        $query .= "     MAIN.SCHREGNO, ";
        $query .= "     MAIN.NAME, ";
        if ($model->search["SEARCH_DIV"] == "1") {
            $query .= "     VALUE(MAIN.GRADE, '00') || '-' || VALUE(MAIN.HR_CLASS, '000') || '-' || VALUE(MAIN.ATTENDNO, '000') || '番' AS HR_ATTEND_NAME, ";
        } else {
            $query .= "     SCH_RH.HR_NAME || ' ' || SCH_R.ATTENDNO || '番' AS HR_ATTEND_NAME, ";
        }
        $query .= "     PRINT_T.SLIP_NO, ";
        $query .= "     PRINT_T.PLAN_MONEY, ";
        $query .= "     PRINT_T.PAID_LIMIT_DATE ";
        $query .= " FROM ";
        $query .= "     {$tableName} MAIN ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT SCH_R ON MAIN.SCHREGNO  = SCH_R.SCHREGNO ";
        $query .= "                                    AND SCH_R.YEAR     = '".$setYear."' ";
        $query .= "                                    AND SCH_R.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT SCH_RH ON SCH_R.YEAR     = SCH_RH.YEAR ";
        $query .= "                                      AND SCH_R.SEMESTER = SCH_RH.SEMESTER ";
        $query .= "                                      AND SCH_R.GRADE    = SCH_RH.GRADE ";
        $query .= "                                      AND SCH_R.HR_CLASS = SCH_RH.HR_CLASS ";
        if ($model->search["SEARCH_DIV"] == "1") {
            $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = MAIN.ENTERYEAR ";
            $query .= "                                    AND GDAT.GRADE = MAIN.GRADE ";
        } else {
            $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = SCH_R.YEAR ";
            $query .= "                                    AND GDAT.GRADE = SCH_R.GRADE ";
        }
        $query .= "     INNER JOIN PRINT_T ON PRINT_T.SCHREGNO = MAIN.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST SCH_B ON MAIN.SCHREGNO = SCH_B.SCHREGNO ";

        $query .= " WHERE ".$where;

        $query .= " ORDER BY ";
        if ($model->field["SORT_DIV"] == "1") {
            $query .= "   SCH_R.GRADE || SCH_R.HR_CLASS || SCH_R.ATTENDNO, ";
        } else {
            $query .= "   MAIN.SCHREGNO, ";
        }
        $query .= "   PRINT_T.SLIP_NO, ";
        $query .= "   PRINT_T.PAID_LIMIT_DATE ";

        return $query;
    }

    //ROWSPANの値のためのカウント
    function getSlipNoCnt($model, $schKind, $schno, $slipNo) {
        if ($model->search["SEARCH_DIV"] == "1") {
            $setYear = (CTRL_YEAR + 1);
        } else {
            $setYear = CTRL_YEAR;
        }
        if ($model->field["PAID_LIMIT_MONTH"] == "12") {
            $setLmonth = "01";
        } else {
            $setLmonth = sprintf("%02d", ($model->field["PAID_LIMIT_MONTH"] + 1));
        }
        if ($model->field["PAID_LIMIT_MONTH"] < "04" || $model->field["PAID_LIMIT_MONTH"] == "12") {
            $setLimitYear = $setYear + 1;
        } else {
            $setLimitYear = $setYear;
        }
        $setLimitDate = $setLimitYear."-".$setLmonth."-01";

        $query  = " WITH COUNT_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         PLAN_M.SLIP_NO, ";
        $query .= "         PLAN_M.SCHREGNO, ";
        $query .= "         SUM(VALUE(PLAN_M.PLAN_MONEY, 0)) AS PLAN_MONEY, ";
        $query .= "         LIMIT_D.PAID_LIMIT_DATE ";
        $query .= "     FROM ";
        $query .= "         COLLECT_SLIP_PLAN_M_DAT PLAN_M ";
        $query .= "         LEFT JOIN COLLECT_SLIP_DAT SL_D ON SL_D.SCHOOLCD    = PLAN_M.SCHOOLCD ";
        $query .= "                                        AND SL_D.SCHOOL_KIND = PLAN_M.SCHOOL_KIND ";
        $query .= "                                        AND SL_D.YEAR        = PLAN_M.YEAR ";
        $query .= "                                        AND SL_D.SLIP_NO     = PLAN_M.SLIP_NO ";
        $query .= "         INNER JOIN COLLECT_SLIP_PLAN_LIMITDATE_DAT LIMIT_D ON PLAN_M.SCHOOLCD         = LIMIT_D.SCHOOLCD ";
        $query .= "                                                           AND PLAN_M.SCHOOL_KIND      = LIMIT_D.SCHOOL_KIND ";
        $query .= "                                                           AND PLAN_M.YEAR             = LIMIT_D.YEAR ";
        $query .= "                                                           AND PLAN_M.SCHREGNO         = LIMIT_D.SCHREGNO ";
        $query .= "                                                           AND PLAN_M.SLIP_NO          = LIMIT_D.SLIP_NO ";
        $query .= "                                                           AND PLAN_M.PLAN_YEAR        = LIMIT_D.PLAN_YEAR ";
        $query .= "                                                           AND PLAN_M.PLAN_MONTH       = LIMIT_D.PLAN_MONTH ";
        $query .= "                                                           AND LIMIT_D.PAID_LIMIT_DATE < '{$setLimitDate}' ";
        $query .= "     WHERE ";
        $query .= "             PLAN_M.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "         AND PLAN_M.SCHOOL_KIND = '{$schKind}' ";
        $query .= "         AND PLAN_M.YEAR        = '{$setYear}' ";
        $query .= "         AND PLAN_M.SCHREGNO    = '{$schno}' ";
        $query .= "         AND PLAN_M.SLIP_NO     = '{$slipNo}' ";
        $query .= "         AND SL_D.CANCEL_DATE IS NULL ";
        $query .= "     GROUP BY ";
        $query .= "         PLAN_M.SLIP_NO, ";
        $query .= "         PLAN_M.SCHREGNO, ";
        $query .= "         LIMIT_D.PAID_LIMIT_DATE ";
        $query .= "     HAVING ";
        $query .= "         SUM(VALUE(PLAN_M.PAID_MONEY, 0)) <= 0 ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COUNT_T ";

        return $query;
    }

    /**
     * 更新処理
     */

    //生徒ごとの納期限よりPLAN_YEAR, PLAN_MONTH取得
    function getLMlimitDatInfo($schKind, $setYear, $schno, $slipNo, $padeLimitDate) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PLAN_LIMITDATE_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD       = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND     = '{$schKind}' ";
        $query .= "     AND YEAR            = '{$setYear}' ";
        $query .= "     AND SCHREGNO        = '{$schno}' ";
        $query .= "     AND SLIP_NO         = '{$slipNo}' ";
        $query .= "     AND PAID_LIMIT_DATE = '{$padeLimitDate}' ";

        return $query;
   }

    //PLAN_YEAR, PLAN_MONTHよりLMCD等を取得
    function getLMplanMDatInfo($schKind, $setYear, $schno, $slipNo, $planYear, $planMonth) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PLAN_M_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$schKind}' ";
        $query .= "     AND YEAR        = '{$setYear}' ";
        $query .= "     AND SCHREGNO    = '{$schno}' ";
        $query .= "     AND SLIP_NO     = '{$slipNo}' ";
        $query .= "     AND PLAN_YEAR   = '{$planYear}' ";
        $query .= "     AND PLAN_MONTH  = '{$planMonth}' ";
 
        return $query;
   }

    //maxSEQ取得(COLLECT_SLIP_PLAN_PAID_M_DAT)
    function getMaxSeqMdat($schKind, $setYear, $schno, $slipNo, $lcd, $mcd, $planYear, $planMonth) {
        $query  = " SELECT ";
        $query .= "     max(SEQ) ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PLAN_PAID_M_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND  = '{$schKind}' ";
        $query .= "     AND YEAR         = '{$setYear}' ";
        $query .= "     AND SLIP_NO      = '{$slipNo}' ";
        $query .= "     AND COLLECT_L_CD = '{$lcd}' ";
        $query .= "     AND COLLECT_M_CD = '{$mcd}' ";
        $query .= "     AND PLAN_YEAR    = '{$planYear}' ";
        $query .= "     AND PLAN_MONTH   = '{$planMonth}' ";

        return $query;
   }

    //maxSEQ取得(COLLECT_SLIP_PLAN_PAID_DAT)
    function getMaxSeqPaidDat($schKind, $setYear, $schno, $slipNo, $planYear, $planMonth) {
        $query  = " SELECT ";
        $query .= "     max(SEQ) ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PLAN_PAID_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND  = '{$schKind}' ";
        $query .= "     AND YEAR         = '{$setYear}' ";
        $query .= "     AND SLIP_NO      = '{$slipNo}' ";
        $query .= "     AND PLAN_YEAR    = '{$planYear}' ";
        $query .= "     AND PLAN_MONTH   = '{$planMonth}' ";

        return $query;
   }

    /**
     * 更新
     */
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($model->search["SEARCH_DIV"] == "1") {
            $setYear = (CTRL_YEAR + 1);
        } else {
            $setYear = CTRL_YEAR;
        }

        list($pYear, $pMonth, $pDay) = explode("/", $model->field["PAID_DATE"]);
        $setPaidYearMonth = $pYear.$pMonth;

        //入金作成
        foreach ($model->arr_schregData as $key => $val) {
            if ($model->field["PAID_FLG-".$val] == "1") {
                list($schKind, $schno, $slipNo, $lyear, $lmonth, $lday) = explode("-", $val);
                $padeLimitDate = $lyear."-".$lmonth."-".$lday;

                //生徒ごとの納期限よりPLAN_YEAR, PLAN_MONTH取得
                $planYearMonth = array();
                $query = knjp739Query::getLMlimitDatInfo($schKind, $setYear, $schno, $slipNo, $padeLimitDate);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $planYearMonth[] = $row;
                }

                foreach ($planYearMonth as $key2 => $ymArr) {
                    //PLAN_YEAR, PLAN_MONTHよりLMCD等を取得
                    $query = knjp739Query::getLMplanMDatInfo($schKind, $setYear, $schno, $slipNo, $ymArr["PLAN_YEAR"], $ymArr["PLAN_MONTH"]);
                    $result = $db->query($query);
                    $totalPlanMoney = 0;
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        if ($row["PLAN_MONEY"] == 0) continue;
                        $totalPlanMoney += $row["PLAN_MONEY"];

                        //COLLECT_SLIP_PLAN_M_DAT
                        $data = array();
                        $data["PAID_MONEY"][NUMBER]     = $row["PLAN_MONEY"];
                        $data["PAID_YEARMONTH"][TEXT]   = $setPaidYearMonth;
                        $data["REGISTERCD"][TEXT]       = STAFFCD;
                        $data["UPDATED"][NUMBER]        = "sysdate()";

                        $where  = " WHERE SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' ";
                        $where .= "   AND SCHOOL_KIND  = '".$schKind."' ";
                        $where .= "   AND YEAR         = '".$setYear."' ";
                        $where .= "   AND SCHREGNO     = '".$schno."' ";
                        $where .= "   AND SLIP_NO      = '".$slipNo."' ";
                        $where .= "   AND COLLECT_L_CD = '".$row["COLLECT_L_CD"]."' ";
                        $where .= "   AND COLLECT_M_CD = '".$row["COLLECT_M_CD"]."' ";
                        $where .= "   AND PLAN_YEAR    = '".$ymArr["PLAN_YEAR"]."' ";
                        $where .= "   AND PLAN_MONTH   = '".$ymArr["PLAN_MONTH"]."' ";

                        $query = Query::updateSQL($data, "COLLECT_SLIP_PLAN_M_DAT", $where);
                        $db->query($query);

                        //COLLECT_SLIP_PLAN_PAID_M_DAT
                        //maxSEQ取得
                        $maxSeq = $db->getOne(knjp739Query::getMaxSeqMdat($schKind, $setYear, $schno, $slipNo, $row["COLLECT_L_CD"], $row["COLLECT_M_CD"], $ymArr["PLAN_YEAR"], $ymArr["PLAN_MONTH"]));
                        $data = array();
                        $data["SCHOOLCD"][TEXT]             = sprintf("%012d", SCHOOLCD);
                        $data["SCHOOL_KIND"][TEXT]          = $schKind;
                        $data["YEAR"][TEXT]                 = $setYear;
                        $data["SLIP_NO"][TEXT]              = $slipNo;
                        $data["COLLECT_L_CD"][TEXT]         = $row["COLLECT_L_CD"];
                        $data["COLLECT_M_CD"][TEXT]         = $row["COLLECT_M_CD"];
                        $data["PLAN_YEAR"][TEXT]            = $ymArr["PLAN_YEAR"];
                        $data["PLAN_MONTH"][TEXT]           = $ymArr["PLAN_MONTH"];
                        $data["SEQ"][TEXT]                  = $maxSeq + 1;
                        $data["SCHREGNO"][TEXT]             = $schno;
                        $data["PLAN_PAID_MONEY_DATE"][DATE] = strtr($model->field["PAID_DATE"], "/", "-");
                        $data["PLAN_PAID_MONEY"][NUMBER]    = $row["PLAN_MONEY"];
                        $data["PLAN_PAID_MONEY_DIV"][TEXT]  = $model->field["PLAN_PAID_MONEY_DIV"];
                        $data["REGISTERCD"][TEXT]           = STAFFCD;
                        $data["UPDATED"][NUMBER]            = "sysdate()";

                        $query = Query::insertSQL($data, "COLLECT_SLIP_PLAN_PAID_M_DAT");
                        $db->query($query);
                    }

                    if ($totalPlanMoney == 0) continue;
                    //COLLECT_SLIP_PLAN_PAID_DAT
                    $maxSeq = $db->getOne(knjp739Query::getMaxSeqPaidDat($schKind, $setYear, $schno, $slipNo, $ymArr["PLAN_YEAR"], $ymArr["PLAN_MONTH"]));
                    $data = array();
                    $data["SCHOOLCD"][TEXT]             = sprintf("%012d", SCHOOLCD);
                    $data["SCHOOL_KIND"][TEXT]          = $schKind;
                    $data["YEAR"][TEXT]                 = $setYear;
                    $data["SLIP_NO"][TEXT]              = $slipNo;
                    $data["PLAN_YEAR"][TEXT]            = $ymArr["PLAN_YEAR"];
                    $data["PLAN_MONTH"][TEXT]           = $ymArr["PLAN_MONTH"];
                    $data["SEQ"][TEXT]                  = $maxSeq + 1;
                    $data["SCHREGNO"][TEXT]             = $schno;
                    $data["PLAN_PAID_MONEY_DATE"][DATE] = strtr($model->field["PAID_DATE"], "/", "-");
                    $data["PLAN_PAID_MONEY"][NUMBER]    = $totalPlanMoney;
                    $data["PLAN_PAID_MONEY_DIV"][TEXT]  = $model->field["PLAN_PAID_MONEY_DIV"];
                    $data["REGISTERCD"][TEXT]           = STAFFCD;
                    $data["UPDATED"][NUMBER]            = "sysdate()";

                    $query = Query::insertSQL($data, "COLLECT_SLIP_PLAN_PAID_DAT");
                    $db->query($query);
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>
