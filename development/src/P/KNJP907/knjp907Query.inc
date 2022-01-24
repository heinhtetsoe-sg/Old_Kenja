<?php

require_once('for_php7.php');

class knjp907Query extends Query {

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
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

    //転退学日コンボ
    function getTentaigaku($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     BASE.GRD_DATE AS VALUE, ";
        $query .= "     BASE.GRD_DATE AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "                                    AND BASE.GRD_DIV IN ('2', '3', '6', '7') ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR  = GDAT.YEAR ";
        $query .= "                                    AND REGD.GRADE = GDAT.GRADE ";
        $query .= " WHERE ";
        $query .= "         REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND BASE.GRD_DATE IS NOT NULL ";
        $query .= "     AND GDAT.SCHOOL_KIND = '".$model->field["SCHOOLKIND"]."' ";
        $query .= " ORDER BY ";
        $query .= "     BASE.GRD_DATE DESC ";

        return $query;
    }

    //クラスコンボ
    function getGradeHrClass($model, $grdDate) {
        $grdDate= ($grdDate == "") ? "0001-01-01": $grdDate;

        $query  = " SELECT DISTINCT ";
        $query .= "     REGD.GRADE || REGD.HR_CLASS AS VALUE, ";
        $query .= "     HDAT.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "                                    AND BASE.GRD_DIV IN ('2', '3', '6', '7') ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT HDAT ON REGD.YEAR     = HDAT.YEAR ";
        $query .= "                                    AND REGD.SEMESTER = HDAT.SEMESTER ";
        $query .= "                                    AND REGD.GRADE    = HDAT.GRADE ";
        $query .= "                                    AND REGD.HR_CLASS = HDAT.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR  = GDAT.YEAR ";
        $query .= "                                    AND REGD.GRADE = GDAT.GRADE ";
        $query .= " WHERE ";
        $query .= "         REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND BASE.GRD_DATE = '".str_replace('/', '-', $grdDate)."' ";
        $query .= "     AND GDAT.SCHOOL_KIND = '".$model->field["SCHOOLKIND"]."' ";
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE || REGD.HR_CLASS ";

        return $query;
    }

    //校種
    function getSchoolKind($model) {
        $grade = substr($model->field["GRADE_HR_CLASS"], 0, 2);
        $query  = " SELECT DISTINCT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '{$grade}' ";

        return $query;
    }

/******リストtoリスト*******/
    //生徒取得(右側に表示する生徒)
    function getStudent($model) {
        $model->field["TENTAI_DATE"] = ($model->field["TENTAI_DATE"] == "") ? "0001-01-01": $model->field["TENTAI_DATE"];

        $query  = " WITH MAX_SEME AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         YEAR, ";
        $query .= "         MAX(SEMESTER) AS SEMESTER ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "             YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND GRADE || HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         YEAR ";
        $query .= " ), LEVYO AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         OUTS.SCHREGNO, ";
        $query .= "         case when OUTD.HENKIN_DATE is not null then '済' else '有' end HENKIN_INFO ";
        $query .= "     FROM ";
        $query .= "         LEVY_REQUEST_OUTGO_DAT OUTD ";
        $query .= "         INNER JOIN LEVY_REQUEST_OUTGO_SCHREG_DAT OUTS ON OUTD.SCHOOLCD    = OUTS.SCHOOLCD ";
        $query .= "                                                      AND OUTD.SCHOOL_KIND = OUTS.SCHOOL_KIND ";
        $query .= "                                                      AND OUTD.YEAR        = OUTS.YEAR ";
        $query .= "                                                      AND OUTD.OUTGO_L_CD  = OUTS.OUTGO_L_CD ";
        $query .= "                                                      AND OUTD.OUTGO_M_CD  = OUTS.OUTGO_M_CD ";
        $query .= "                                                      AND OUTD.REQUEST_NO  = OUTS.REQUEST_NO ";
        $query .= "     WHERE ";
        $query .= "             OUTD.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "         AND OUTD.SCHOOL_KIND = '".$model->field["SCHOOLKIND"]."' ";
        $query .= "         AND OUTD.YEAR        = '".CTRL_YEAR."' ";
        $query .= "         AND VALUE(OUTD.OUTGO_APPROVAL, '0') = '1' ";
        $query .= "         AND VALUE(OUTD.OUTGO_CANCEL, '0')   = '0' ";
        $query .= "         AND VALUE(OUTD.HENKIN_FLG, '0')     = '1' ";
        $query .= "         AND OUTS.OUTGO_L_CD  = '99' ";
        $query .= "         AND OUTS.OUTGO_M_CD  = '99' ";
        $query .= "         AND OUTS.OUTGO_S_CD  = '999' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     REGD.SCHREGNO AS VALUE, ";
        $query .= "     REGDG.SCHOOL_KIND, ";
        $query .= "     BASE.NAME, ";
        $query .= "     CASE WHEN LEVYO.HENKIN_INFO IS NULL THEN '　' ELSE LEVYO.HENKIN_INFO END AS HENKIN_INFO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "                                    AND BASE.GRD_DIV IN ('2', '3', '6', '7') ";
        $query .= "                                    AND BASE.GRD_DATE = '".str_replace('/', '-', $model->field["TENTAI_DATE"])."' ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGD.YEAR  = REGDG.YEAR ";
        $query .= "                                     AND REGD.GRADE = REGDG.GRADE ";
        $query .= "     LEFT JOIN LEVYO ON REGD.SCHREGNO = LEVYO.SCHREGNO, ";
        $query .= "     MAX_SEME ";
        $query .= " WHERE ";
        $query .= "         REGD.YEAR     = MAX_SEME.YEAR ";
        $query .= "     AND REGD.SEMESTER = MAX_SEME.SEMESTER ";
        $query .= "     AND REGD.SCHREGNO = MAX_SEME.SCHREGNO ";
        if ($model->field["OUTPUT_DIV"] == '2') {
            $query .= "     AND LEVYO.SCHREGNO IS NOT NULL ";
        } else if ($model->field["OUTPUT_DIV"] == '1') {
            $query .= "     AND LEVYO.SCHREGNO IS NULL ";
        }
        $query .= " ORDER BY ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }

    //更新（返金実行）
    function updLevyOutgo($model, $schregNo) {

        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $chkIncomeTotal = 0;
        $chkOutgoTotal  = 0;

        //収入
        $query = knjp907Query::getIncomeData($model, $schregNo);
        $result = $db->query($query);
        $incomeArray = array();
        while ($incRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $incomeArray[$incRow["INCOME_L_CD"]."-".$incRow["INCOME_M_CD"]] = $incRow["INCOME_MONEY"];
            $chkIncomeTotal += $incRow["INCOME_MONEY"];
        }
        $result->free();

        //支出
        $query = knjp907Query::getOutgoData($model, $schregNo);
        $result = $db->query($query);
        $outgoArray = array();
        while ($incRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $outgoArray[$incRow["INCOME_L_CD"]."-".$incRow["INCOME_M_CD"]] = $incRow["OUTGO_MONEY"];
            $chkOutgoTotal += $incRow["OUTGO_MONEY"];
        }
        $result->free();

        // 「収入 > 支出」の時のみ処理実行
        if ($chkIncomeTotal > $chkOutgoTotal) {
            foreach ($incomeArray as $lmcd => $incMoney) {
                $outMoney = $outgoArray[$lmcd];
                $setMoney = $incMoney - $outMoney;

                list($lcd, $mcd) = explode("-", $lmcd);

                $getMaxRequestNo = $db->getOne(knjp907Query::getMaxRequestNo($model));
                //対象年度の伝票番号がない場合
                if (!$getMaxRequestNo) {
                    $this->getRequestNo = CTRL_YEAR.'B00001';
                } else {
                    $setNo = sprintf("%05d", intval(substr($getMaxRequestNo, 5, 5)) + 1);
                    $setRequestNo = CTRL_YEAR.'B'.$setNo;
                }

                //LEVY_REQUEST_OUTGO_DAT
                $data = array();
                $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
                $data["SCHOOL_KIND"][TEXT]      = $model->field["SCHOOLKIND"];
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["OUTGO_L_CD"][TEXT]       = '99';
                $data["OUTGO_M_CD"][TEXT]       = '99';
                $data["REQUEST_NO"][TEXT]       = $setRequestNo;
                $data["REQUEST_DATE"][DATE]     = str_replace('/', '-', $model->field["HENKIN_KAKUTEI"]);
                $data["REQUEST_STAFF"][TEXT]    = STAFFCD;
                $data["INCOME_L_CD"][TEXT]      = $lcd;
                $data["INCOME_M_CD"][TEXT]      = $mcd;
                $data["PAY_DIV"][TEXT]          = '1'; // 固定
                $data["REQUEST_GK"][NUMBER]     = $setMoney;
                $data["OUTGO_APPROVAL"][TEXT]   = '1';
                $data["HENKIN_FLG"][TEXT]       = '1';

                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "SYSDATE()";

                $query = Query::insertSQL($data, "LEVY_REQUEST_OUTGO_DAT");
                $db->query($query);

                //LEVY_REQUEST_OUTGO_MEISAI_DAT
                $data = array();
                $data["SCHOOLCD"][TEXT]                 = sprintf("%012d", SCHOOLCD);
                $data["SCHOOL_KIND"][TEXT]              = $model->field["SCHOOLKIND"];
                $data["YEAR"][TEXT]                     = CTRL_YEAR;
                $data["REQUEST_NO"][TEXT]               = $setRequestNo;
                $data["LINE_NO"][NUMBER]                = 1;
                $data["OUTGO_L_CD"][TEXT]               = '99';
                $data["OUTGO_M_CD"][TEXT]               = '99';
                $data["OUTGO_S_CD"][TEXT]               = '999';
                $data["COMMODITY_PRICE"][NUMBER]        = $setMoney;
                $data["COMMODITY_CNT"][NUMBER]          = 1;
                $data["TOTAL_PRICE_ZEINUKI"][NUMBER]    = $setMoney;
                $data["TOTAL_TAX"][NUMBER]              = 0;
                $data["TOTAL_PRICE"][NUMBER]            = $setMoney;
                $data["SCH_PRICE"][NUMBER]              = $setMoney;
                $data["SCH_CNT"][NUMBER]                = 1;
                $data["HASUU"][NUMBER]                  = 0;
                $data["WARIHURI_DIV"][TEXT]             = '1';

                $data["REGISTERCD"][TEXT]               = STAFFCD;
                $data["UPDATED"][FUNC]                  = "SYSDATE()";

                $query = Query::insertSQL($data, "LEVY_REQUEST_OUTGO_MEISAI_DAT");
                $db->query($query);

                //LEVY_REQUEST_OUTGO_SCHREG_DAT
                $data = array();
                $data["SCHOOLCD"][TEXT]                 = sprintf("%012d", SCHOOLCD);
                $data["SCHOOL_KIND"][TEXT]              = $model->field["SCHOOLKIND"];
                $data["YEAR"][TEXT]                     = CTRL_YEAR;
                $data["REQUEST_NO"][TEXT]               = $setRequestNo;
                $data["SCHREGNO"][TEXT]                 = $schregNo;
                $data["LINE_NO"][NUMBER]                = 1;
                $data["OUTGO_L_CD"][TEXT]               = '99';
                $data["OUTGO_M_CD"][TEXT]               = '99';
                $data["OUTGO_S_CD"][TEXT]               = '999';
                $data["OUTGO_DATE"][DATE]               = str_replace('/', '-', $model->field["HENKIN_KAKUTEI"]);
                $data["OUTGO_MONEY"][NUMBER]            = $setMoney;

                $data["REGISTERCD"][TEXT]               = STAFFCD;
                $data["UPDATED"][FUNC]                  = "SYSDATE()";

                $query = Query::insertSQL($data, "LEVY_REQUEST_OUTGO_SCHREG_DAT");
                $db->query($query);
            }
        }

        //DB切断
        $db->commit();
        Query::dbCheckIn($db);
    }

    //収入
    function getIncomeData($model, $schregNo) {

        $query  = " SELECT ";
        $query .= "     INCS.INCOME_L_CD, ";
        $query .= "     INCS.INCOME_M_CD, ";
        $query .= "     SUM(VALUE(INCS.INCOME_MONEY, 0)) AS INCOME_MONEY ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT INCD ";
        $query .= "     INNER JOIN LEVY_REQUEST_INCOME_SCHREG_DAT INCS ON INCD.SCHOOLCD = INCS.SCHOOLCD ";
        $query .= "           AND INCD.SCHOOL_KIND = INCS.SCHOOL_KIND ";
        $query .= "           AND INCD.YEAR        = INCS.YEAR ";
        $query .= "           AND INCD.INCOME_L_CD = INCS.INCOME_L_CD ";
        $query .= "           AND INCD.INCOME_M_CD = INCS.INCOME_M_CD ";
        $query .= "           AND INCD.REQUEST_NO  = INCS.REQUEST_NO ";
        $query .= "           AND INCS.SCHREGNO    = '{$schregNo}' ";
        $query .= "     LEFT JOIN LEVY_M_MST LEVYM ON INCD.SCHOOLCD = LEVYM.SCHOOLCD ";
        $query .= "          AND INCD.SCHOOL_KIND = LEVYM.SCHOOL_KIND ";
        $query .= "          AND INCD.YEAR        = LEVYM.YEAR ";
        $query .= "          AND INCD.INCOME_L_CD = LEVYM.LEVY_L_CD ";
        $query .= "          AND INCD.INCOME_M_CD = LEVYM.LEVY_M_CD ";
        $query .= " WHERE ";
        $query .= "         INCD.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND INCD.SCHOOL_KIND = '{$model->field["SCHOOLKIND"]}' ";
        $query .= "     AND INCD.YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND VALUE(INCD.INCOME_APPROVAL, '0') = '1' ";
        $query .= "     AND VALUE(INCD.INCOME_CANCEL, '0')   = '0' ";
        $query .= "     AND INCD.INCOME_L_CD NOT IN ('99') ";
        $query .= " GROUP BY ";
        $query .= "     INCS.INCOME_L_CD, ";
        $query .= "     INCS.INCOME_M_CD ";
        $query .= " ORDER BY ";
        $query .= "     INCS.INCOME_L_CD, ";
        $query .= "     INCS.INCOME_M_CD ";

        return $query;
    }

    //支出
    function getOutgoData($model, $schregNo) {

        $query  = " SELECT ";
        $query .= "     OUTD.INCOME_L_CD, ";
        $query .= "     OUTD.INCOME_M_CD, ";
        $query .= "     SUM(VALUE(OUTS.OUTGO_MONEY, 0)) AS OUTGO_MONEY ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT OUTD ";
        $query .= "     INNER JOIN LEVY_REQUEST_OUTGO_SCHREG_DAT OUTS ON OUTD.SCHOOLCD = OUTS.SCHOOLCD ";
        $query .= "           AND OUTD.SCHOOL_KIND = OUTS.SCHOOL_KIND ";
        $query .= "           AND OUTD.YEAR        = OUTS.YEAR ";
        $query .= "           AND OUTD.OUTGO_L_CD  = OUTS.OUTGO_L_CD ";
        $query .= "           AND OUTD.OUTGO_M_CD  = OUTS.OUTGO_M_CD ";
        $query .= "           AND OUTD.REQUEST_NO  = OUTS.REQUEST_NO ";
        $query .= "           AND OUTS.SCHREGNO    = '{$schregNo}' ";
        $query .= "     LEFT JOIN LEVY_S_MST LEVYS ON OUTS.SCHOOLCD = LEVYS.SCHOOLCD ";
        $query .= "          AND OUTS.SCHOOL_KIND = LEVYS.SCHOOL_KIND ";
        $query .= "          AND OUTS.YEAR        = LEVYS.YEAR ";
        $query .= "          AND OUTS.OUTGO_L_CD  = LEVYS.LEVY_L_CD ";
        $query .= "          AND OUTS.OUTGO_M_CD  = LEVYS.LEVY_M_CD ";
        $query .= "          AND OUTS.OUTGO_S_CD  = LEVYS.LEVY_S_CD ";
        $query .= " WHERE ";
        $query .= "         OUTD.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND OUTD.SCHOOL_KIND = '{$model->field["SCHOOLKIND"]}' ";
        $query .= "     AND OUTD.YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND VALUE(OUTD.OUTGO_APPROVAL, '0') = '1' ";
        $query .= "     AND VALUE(OUTD.OUTGO_CANCEL, '0')   = '0' ";
        $query .= "     AND OUTD.OUTGO_L_CD NOT IN ('99') ";
        $query .= " GROUP BY ";
        $query .= "     OUTD.INCOME_L_CD, ";
        $query .= "     OUTD.INCOME_M_CD ";

        return $query;
    }

    //伝票番号を取得
    function getMaxRequestNo($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     MAX(REQUEST_NO) AS NO ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND SCHOOL_KIND = '".$model->field["SCHOOLKIND"]."'";
        $query .= "     AND YEAR        = '".CTRL_YEAR."' ";

        return $query;
    }

    //更新（キャンセル実行）
    function cancelLevyOutgo($model, $schregNo) {

        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //伝票番号取得
        $query = knjp907Query::getOutgoDataCancel($model, $schregNo);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            //LEVY_REQUEST_OUTGO_DAT
            $data = array();
            $data["OUTGO_CANCEL"][TEXT]     = '1';

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $where .= "   AND SCHOOL_KIND = '".$model->field["SCHOOLKIND"]."' ";
            $where .= "   AND YEAR        = '".CTRL_YEAR."' ";
            $where .= "   AND OUTGO_L_CD  = '".$row["OUTGO_L_CD"]."' ";
            $where .= "   AND OUTGO_M_CD  = '".$row["OUTGO_M_CD"]."' ";
            $where .= "   AND REQUEST_NO  = '".$row["REQUEST_NO"]."' ";

            $query = Query::updateSQL($data, "LEVY_REQUEST_OUTGO_DAT", $where);
            $db->query($query);
        }
        $result->free();

        //DB切断
        $db->commit();
        Query::dbCheckIn($db);
    }

    //伝票番号取得
    function getOutgoDataCancel($model, $schregNo) {

        $query  = " SELECT ";
        $query .= "     OUTD.OUTGO_L_CD, ";
        $query .= "     OUTD.OUTGO_M_CD, ";
        $query .= "     OUTD.REQUEST_NO ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT OUTD ";
        $query .= "     INNER JOIN LEVY_REQUEST_OUTGO_SCHREG_DAT OUTS ON OUTD.SCHOOLCD    = OUTS.SCHOOLCD ";
        $query .= "                                                  AND OUTD.SCHOOL_KIND = OUTS.SCHOOL_KIND ";
        $query .= "                                                  AND OUTD.YEAR        = OUTS.YEAR ";
        $query .= "                                                  AND OUTD.REQUEST_NO  = OUTS.REQUEST_NO ";
        $query .= "                                                  AND OUTS.SCHREGNO    = '{$schregNo}' ";
        $query .= "                                                  AND OUTS.LINE_NO     = 1 ";
        $query .= "                                                  AND OUTD.OUTGO_L_CD  = OUTS.OUTGO_L_CD ";
        $query .= "                                                  AND OUTD.OUTGO_M_CD  = OUTS.OUTGO_M_CD ";
        $query .= "                                                  AND OUTS.OUTGO_S_CD  = '999' ";
        $query .= " WHERE ";
        $query .= "         OUTD.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND OUTD.SCHOOL_KIND = '{$model->field["SCHOOLKIND"]}' ";
        $query .= "     AND OUTD.YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND OUTD.OUTGO_L_CD  = '99' ";
        $query .= "     AND OUTD.OUTGO_M_CD  = '99' ";
        $query .= "     AND OUTD.HENKIN_FLG  = '1' ";
        $query .= "     AND VALUE(OUTD.OUTGO_APPROVAL, '0') = '1' ";
        $query .= "     AND VALUE(OUTD.OUTGO_CANCEL, '0')   = '0' ";

        return $query;
    }
}
?>
