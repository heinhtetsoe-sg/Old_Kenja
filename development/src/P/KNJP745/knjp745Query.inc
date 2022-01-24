<?php

require_once('for_php7.php');

class knjp745Query extends Query {
    //校種
    function getSchoolKind($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     GDAT.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = REGD.YEAR ";
        $query .= "                                    AND GDAT.GRADE = REGD.GRADE ";
        $query .= " WHERE ";
        $query .= "         REGD.YEAR     = '{$model->year}' ";
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
            $where .= "   ".$and."MAIN.ENTERYEAR = '".$model->year."' ";
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
    function getStudentInfoData($model, $cnt="", $schKind="", $schno="", $slipNo="") {
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
            $setLimitYear = $model->year + 1;
        } else {
            $setLimitYear = $model->year;
        }
        $setLimitDate = $setLimitYear."-".$setLmonth."-01";

        $where = knjp745Query::getStudentSearch($model);

        $query  = " WITH PAID_T0 AS ( ";
        $query .= "     SELECT ";
        $query .= "         PAID.SCHOOLCD, ";
        $query .= "         PAID.SCHOOL_KIND, ";
        $query .= "         PAID.YEAR, ";
        $query .= "         PAID.SLIP_NO, ";
        $query .= "         PAID.PLAN_PAID_MONEY_DATE, ";
        $query .= "         sum(value(PLAN_PAID_MONEY, 0)) as PAID_MONEY, ";
        $query .= "         PAID.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         COLLECT_SLIP_PLAN_PAID_DAT PAID ";
        $query .= "         INNER JOIN COLLECT_SLIP_PLAN_LIMITDATE_DAT LIMI ON LIMI.SCHOOLCD         = PAID.SCHOOLCD ";
        $query .= "                                                        AND LIMI.SCHOOL_KIND      = PAID.SCHOOL_KIND ";
        $query .= "                                                        AND LIMI.YEAR             = PAID.YEAR ";
        $query .= "                                                        AND LIMI.SCHREGNO         = PAID.SCHREGNO ";
        $query .= "                                                        AND LIMI.SLIP_NO          = PAID.SLIP_NO ";
        $query .= "                                                        AND LIMI.PLAN_YEAR        = PAID.PLAN_YEAR ";
        $query .= "                                                        AND LIMI.PLAN_MONTH       = PAID.PLAN_MONTH ";
        $query .= "                                                        AND LIMI.PAID_LIMIT_DATE < '{$setLimitDate}' ";
        $query .= "     WHERE ";
        $query .= "             PAID.PLAN_PAID_MONEY_DATE < '{$setLimitDate}' ";
        $query .= "         AND PAID.PLAN_PAID_MONEY_DIV <> '1' "; // 1:引落
        $query .= "     GROUP BY ";
        $query .= "         PAID.SCHOOLCD, ";
        $query .= "         PAID.SCHOOL_KIND, ";
        $query .= "         PAID.YEAR, ";
        $query .= "         PAID.SLIP_NO, ";
        $query .= "         PAID.PLAN_PAID_MONEY_DATE, ";
        $query .= "         PAID.SCHREGNO ";
        $query .= " ), PAID_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         PAID.SLIP_NO, ";
        $query .= "         PAID.SCHREGNO, ";
        $query .= "         PAID.PAID_MONEY, ";
        $query .= "         GRP_M.COLLECT_GRP_NAME, ";
        $query .= "         PAID.PLAN_PAID_MONEY_DATE ";
        $query .= "     FROM ";
        $query .= "         PAID_T0 PAID ";
        $query .= "         LEFT JOIN COLLECT_SLIP_DAT SL_D ON SL_D.SCHOOLCD    = PAID.SCHOOLCD ";
        $query .= "                                        AND SL_D.SCHOOL_KIND = PAID.SCHOOL_KIND ";
        $query .= "                                        AND SL_D.YEAR        = PAID.YEAR ";
        $query .= "                                        AND SL_D.SLIP_NO     = PAID.SLIP_NO ";
        $query .= "         LEFT JOIN COLLECT_GRP_MST GRP_M ON SL_D.SCHOOLCD       = GRP_M.SCHOOLCD ";
        $query .= "                                        AND SL_D.SCHOOL_KIND    = GRP_M.SCHOOL_KIND ";
        $query .= "                                        AND SL_D.YEAR           = GRP_M.YEAR ";
        $query .= "                                        AND SL_D.COLLECT_GRP_CD = GRP_M.COLLECT_GRP_CD ";
        $query .= "     WHERE ";
        $query .= "             PAID.SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        if ($cnt != '') {
            $query .= "         AND PAID.SCHOOL_KIND = '{$schKind}' ";
            $query .= "         AND PAID.SCHREGNO    = '{$schno}' ";
            if ($cnt == 'cnt2') {
                $query .= "         AND PAID.SLIP_NO     = '{$slipNo}' ";
            }
        } else {
            if ($model->schFlg) {
                if ($model->selectSchoolKind) {
                    $query .= "         AND PAID.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else {
                $query .= "         AND PAID.SCHOOL_KIND = '{$model->schoolKind}' ";
            }
        }
        $query .= "         AND PAID.YEAR = '{$model->year}' ";
        $query .= "         AND SL_D.CANCEL_DATE is null ";
        $query .= " ) ";
        //rowspanカウント用
        if ($cnt != '') {
            $query .= " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     PAID_T ";
        } else {
            //メイン
            $query .= " SELECT DISTINCT ";
            $query .= "     GDAT.SCHOOL_KIND, ";
            $query .= "     SCH_R.GRADE || SCH_R.HR_CLASS || SCH_R.ATTENDNO, ";
            $query .= "     MAIN.SCHREGNO, ";
            $query .= "     MAIN.NAME, ";
            $query .= "     PAID_T.COLLECT_GRP_NAME, ";
            if ($model->search["SEARCH_DIV"] == "1") {
                $query .= "     VALUE(MAIN.GRADE, '00') || '-' || VALUE(MAIN.HR_CLASS, '000') || '-' || VALUE(MAIN.ATTENDNO, '000') || '番' AS HR_ATTEND_NAME, ";
            } else {
                $query .= "     SCH_RH.HR_NAME || ' ' || SCH_R.ATTENDNO || '番' AS HR_ATTEND_NAME, ";
            }
            $query .= "     PAID_T.SLIP_NO, ";
            $query .= "     PAID_T.PAID_MONEY, ";
            $query .= "     PAID_T.PLAN_PAID_MONEY_DATE ";
            $query .= " FROM ";
            $query .= "     {$tableName} MAIN ";
            $query .= "     LEFT JOIN SCHREG_REGD_DAT SCH_R ON MAIN.SCHREGNO  = SCH_R.SCHREGNO ";
            $query .= "                                    AND SCH_R.YEAR     = '".$model->year."' ";
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
            $query .= "     INNER JOIN PAID_T ON PAID_T.SCHREGNO = MAIN.SCHREGNO ";
            $query .= "     LEFT JOIN SCHREG_BASE_MST SCH_B ON MAIN.SCHREGNO = SCH_B.SCHREGNO ";

            if ($where != "") {
                $query .= " WHERE ".$where;
            }

            $query .= " ORDER BY ";
            if ($model->field["SORT_DIV"] == "1") {
                $query .= "   SCH_R.GRADE || SCH_R.HR_CLASS || SCH_R.ATTENDNO, ";
            } else {
                $query .= "   MAIN.SCHREGNO, ";
            }
            $query .= "   PAID_T.SLIP_NO, ";
            $query .= "   PAID_T.PLAN_PAID_MONEY_DATE ";
        }

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //更新
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        list($pYear, $pMonth, $pDay) = explode("/", $model->field["PAID_DATE"]);
        $setPaidYearMonth = $pYear.$pMonth;

        //入金作成
        foreach ($model->arr_schregData as $key => $val) {
            //チェックonのみ処理
            if ($model->field["DEL_FLG-".$val] == "1") {
                list($schKind, $schNo, $slipNo, $pYear, $pMonth, $pDay) = explode("-", $val);

                $padeDate = $pYear."-".$pMonth."-".$pDay;

                // COLLECT_SLIP_PLAN_M_DAT(update)
                $data = array();
                $data["PAID_MONEY"][NUMBER]     = null;
                $data["PAID_YEARMONTH"][TEXT]   = null;
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";

                $where  = " WHERE SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' ";
                $where .= "   AND SCHOOL_KIND  = '{$schKind}' ";
                $where .= "   AND YEAR         = '{$model->year}' ";
                $where .= "   AND SCHREGNO     = '{$schNo}' ";
                $where .= "   AND SLIP_NO      = '{$slipNo}' ";
                $where .= "   AND COLLECT_L_CD || COLLECT_M_CD || ";
                $where .= "       PLAN_YEAR    || PLAN_MONTH in ( ";
                $where .= "                                       SELECT ";
                $where .= "                                           COLLECT_L_CD || COLLECT_M_CD || PLAN_YEAR || PLAN_MONTH ";
                $where .= "                                       FROM ";
                $where .= "                                           COLLECT_SLIP_PLAN_PAID_M_DAT ";
                $where .= "                                       WHERE ";
                $where .= "                                           SCHOOLCD             = '".(sprintf("%012d", SCHOOLCD))."' ";
                $where .= "                                       AND SCHOOL_KIND          = '{$schKind}' ";
                $where .= "                                       AND YEAR                 = '{$model->year}' ";
                $where .= "                                       AND SLIP_NO              = '{$slipNo}' ";
                $where .= "                                       AND SCHREGNO             = '{$schNo}' ";
                $where .= "                                       AND PLAN_PAID_MONEY_DATE = '{$padeDate}' ";
                $where .= "                                      ) ";

                $query = Query::updateSQL($data, "COLLECT_SLIP_PLAN_M_DAT", $where);
                $db->query($query);

                // COLLECT_SLIP_PLAN_PAID_DAT(delete)
                $query  = " DELETE FROM ";
                $query .= "     COLLECT_SLIP_PLAN_PAID_DAT ";
                $query .= " WHERE ";
                $query .= "         SCHOOLCD             = '".(sprintf("%012d", SCHOOLCD))."' ";
                $query .= "     AND SCHOOL_KIND          = '{$schKind}' ";
                $query .= "     AND YEAR                 = '{$model->year}' ";
                $query .= "     AND SCHREGNO             = '{$schNo}' ";
                $query .= "     AND SLIP_NO              = '{$slipNo}' ";
                $query .= "     AND PLAN_PAID_MONEY_DATE = '{$padeDate}' ";
                $db->query($query);

                // COLLECT_SLIP_PLAN_PAID_M_DAT(delete)
                $query  = " DELETE FROM ";
                $query .= "     COLLECT_SLIP_PLAN_PAID_M_DAT ";
                $query .= " WHERE ";
                $query .= "         SCHOOLCD             = '".(sprintf("%012d", SCHOOLCD))."' ";
                $query .= "     AND SCHOOL_KIND          = '{$schKind}' ";
                $query .= "     AND YEAR                 = '{$model->year}' ";
                $query .= "     AND SCHREGNO             = '{$schNo}' ";
                $query .= "     AND SLIP_NO              = '{$slipNo}' ";
                $query .= "     AND PLAN_PAID_MONEY_DATE = '{$padeDate}' ";
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>
