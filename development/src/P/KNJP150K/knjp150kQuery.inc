<?php

require_once('for_php7.php');

class knjp150kQuery extends Query {

    //生徒の名前の取得
    function getStudentName($schregno)
    {
        return "SELECT schregno,name_show,name_kana FROM schreg_base_mst WHERE schregno ='".$schregno."'";
    }

    //名称マスタより取得
    function getNamecd($year, $cd, $lavelNameFlg = false)
    {
        $query  = " SELECT ";
        if ($lavelNameFlg) {
            $query .= "     NAME1 AS LABEL, ";
        } else {
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        }
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$cd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リストの取得
    function getList($model)
    {
        $query  = " SELECT ";
        $query .= "     T5.expense_l_cd || T1.expense_m_cd || T1.expense_s_cd AS titlecd,";
        $query .= "     T5.expense_l_cd,T1.expense_m_cd,T1.expense_s_cd, ";
        $query .= "     T4.expense_s_name,T2.money_due,T3.paid_money, ";
        $query .= "     MEISYOU_GET(T3.paid_money_div, 'G205', 1) AS paid_money_div, ";
        $query .= "     T3.repay_money,T3.repay_money_date, ";
        $query .= "     MEISYOU_GET(T3.repay_money_div, 'G209', 1) AS repay_money_div, ";
        $query .= "     T3.repay_flg,T3.remark,T3.paid_input_flg, ";
        $query .= "     T6.inst_cd ";
        $query .= " FROM ";
        $query .= "     (SELECT year,schregno,expense_m_cd,expense_s_cd ";
        $query .= "        FROM money_due_s_dat ";
        $query .= "       WHERE year            = '".$model->year."' ";
        $query .= "         AND schregno        = '".$model->schregno."' ";
        $query .= "       UNION ";
        $query .= "      SELECT year,schregno,expense_m_cd,expense_s_cd ";
        $query .= "        FROM money_paid_s_dat ";
        $query .= "       WHERE year        = '".$model->year."' ";
        $query .= "         AND schregno    = '".$model->schregno."' ";
        $query .= "     ) T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     money_due_s_dat T2 ";
        $query .= " ON ";
        $query .= "     T1.year         = T2.year AND ";
        $query .= "     T1.schregno     = T2.schregno AND ";
        $query .= "     T1.expense_m_cd = T2.expense_m_cd AND ";
        $query .= "     T1.expense_s_cd = T2.expense_s_cd ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     money_paid_s_dat T3 ";
        $query .= " ON ";
        $query .= "     T1.year         = T3.year AND ";
        $query .= "     T1.schregno     = T3.schregno AND ";
        $query .= "     T1.expense_m_cd = T3.expense_m_cd AND ";
        $query .= "     T1.expense_s_cd = T3.expense_s_cd ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     v_expense_s_mst T4 ";
        $query .= " ON ";
        $query .= "     T1.year         = T4.year AND ";
        $query .= "     T1.expense_m_cd = T4.expense_m_cd AND ";
        $query .= "     T1.expense_s_cd = T4.expense_s_cd ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     expense_m_mst T5 ";
        $query .= " ON ";
        $query .= "     T1.year         = T5.year AND ";
        $query .= "     T1.expense_m_cd = T5.expense_m_cd ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     money_due_m_dat T6 ";
        $query .= " ON ";
        $query .= "     T1.year         = T6.year AND ";
        $query .= "     T1.schregno     = T6.schregno AND ";
        $query .= "     T1.expense_m_cd = T6.expense_m_cd ";
        $query .= " ORDER BY ";
        $query .= "     T1.expense_m_cd,T1.expense_s_cd ";

        return $query;
    }

    //入金必要金額の取得
    function getMoneyDue($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.expense_m_cd, T2.expense_s_exist_flg, T1.money_due ";
        $query .= " FROM ";
        $query .= "     money_due_m_dat T1 ";
        $query .= " INNER JOIN ";
        $query .= "     expense_m_mst T2 ";
        $query .= " ON ";
        $query .= "     T1.year         = T2.year AND ";
        $query .= "     T1.expense_m_cd = T2.expense_m_cd ";
        $query .= " WHERE ";
        $query .= "     T1.year     = '".$model->year."' AND ";
        $query .= "     T1.schregno = '".$model->schregno."' AND ";
        $query .= "     T1.inst_cd  = '".$model->inst_cd."' ";

        return $query;
    }

    //入金必要金額の取得(小分類)
    function getMoneyDue2($model, $m_cd)
    {
        $query  = " SELECT ";
        $query .= "     sum(money_due) ";
        $query .= "   FROM ";
        $query .= "     money_due_s_dat ";
        $query .= "  WHERE ";
        $query .= "     year = '".$model->year."' AND ";
        $query .= "     schregno = '".$model->schregno."' AND ";
        $query .= "     expense_m_cd = '".$m_cd."'";

        return $query;
    }

    //分納の金額を取得
    function getInstMoney($model)
    {
        $query  = " SELECT ";
        $query .= "     SUM(paid_money)  AS paid_money, ";
        $query .= "     SUM(repay_money) AS repay_money ";
        $query .= " FROM ";
        $query .= "     installment_dat ";
        $query .= " WHERE ";
        $query .= "     year     = '".$model->year."' AND ";
        $query .= "     schregno = '".$model->schregno."' AND ";
        $query .= "     inst_cd  = '".$model->inst_cd."' ";

        return $query;
    }

    //名称取得
    function getName($year, $cd, $cd2)
    {
        $query  = " SELECT name1 ";
        $query .= "   FROM v_name_mst ";
        $query .= "  WHERE year = '".$year."' ";
        $query .= "    AND namecd1 = '".$cd."' ";
        $query .= "    AND namecd2 = '".$cd2."' ";
        return $query;
    }

    //１レコード取得
    function getRow(&$db, $model)
    {
        $repaySeq = $model->field["REPAY_SEQ"] ? $model->field["REPAY_SEQ"] : "-1";

        $query  = "SELECT ";
        $query .= "     T1.PAID_MONEY, ";
        $query .= "     T1.PAID_MONEY_DATE, ";
        $query .= "     T1.PAID_MONEY_DIV, ";
        $query .= "     L1.REPAY_MONEY, ";
        $query .= "     L1.REPAY_MONEY_DATE, ";
        $query .= "     L1.REPAY_MONEY_DIV, ";
        $query .= "     L1.REPAY_FLG, ";
        $query .= "     L1.REMARK ";
        $query .= " FROM ";
        $query .= "     MONEY_PAID_S_DAT T1 ";
        $query .= "     LEFT JOIN MONEY_REPAY_S_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "          AND L1.REPAY_SEQ = ".$repaySeq." ";
        $query .= "          AND T1.EXPENSE_M_CD = L1.EXPENSE_M_CD ";
        $query .= "          AND T1.EXPENSE_S_CD = L1.EXPENSE_S_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR             = '".$model->year."' AND ";
        $query .= "     T1.SCHREGNO         = '".$model->schregno."' AND ";
        $query .= "     T1.EXPENSE_M_CD     = '".$model->exp_mcd."' AND ";
        $query .= "     T1.EXPENSE_S_CD     = '".$model->exp_scd."' AND ";
        $query .= "     T1.PAID_INPUT_FLG   = '".$model->paid_input_flg."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        return $row;
    }

    //返金詳細
    function getRepaySeq($model)
    {
        $query  = " SELECT ";
        $query .= "     REPAY_SEQ AS VALUE, ";
        $query .= "     CAST(REPAY_MONEY_DATE AS CHAR(10)) || ':' || rtrim(CAST(REPAY_MONEY AS CHAR(10))) AS LABEL ";
        $query .= " FROM ";
        $query .= "     MONEY_REPAY_S_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND EXPENSE_M_CD = '".$model->exp_mcd."' ";
        $query .= "     AND EXPENSE_S_CD = '".$model->exp_scd."' ";
        $query .= " ORDER BY ";
        $query .= "     REPAY_MONEY_DATE, ";
        $query .= "     REPAY_SEQ ";

        return $query;
    }

    //返金詳細
    function getMaxRePayKey($model, $year, $schregno, $exp_mcd, $exp_scd)
    {

        $query  = " WITH MAX_DATE AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     MAX(REPAY_MONEY_DATE) AS MAX_DATE ";
        $query .= " FROM ";
        $query .= "     MONEY_REPAY_S_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND SCHREGNO = '".$schregno."' ";
        $query .= "     AND EXPENSE_M_CD = '".$exp_mcd."' ";
        $query .= "     AND EXPENSE_S_CD = '".$exp_scd."' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO ";
        $query .= " ), MAX_KEY AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.MAX_DATE, ";
        $query .= "     MAX(L1.REPAY_SEQ) AS MAX_SEQ ";
        $query .= " FROM ";
        $query .= "     MAX_DATE T1 ";
        $query .= "     LEFT JOIN MONEY_REPAY_S_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "     AND T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     AND T1.MAX_DATE = L1.REPAY_MONEY_DATE ";
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.MAX_DATE ";
        $query .= " ), MAX_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     MONEY_REPAY_S_DAT T1, ";
        $query .= "     MAX_KEY T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     AND T1.REPAY_SEQ = T2.MAX_SEQ ";
        $query .= " ) ";

        return $query;
    }

    //返金詳細
    function getTotalRePay($model, $year, $schregno, $exp_mcd, $exp_scd, $div)
    {
        $repaySeq = $model->field["REPAY_SEQ"] ? $model->field["REPAY_SEQ"] : "-1";
        $query = knjp150kQuery::getMaxRePayKey($model, $year, $schregno, $exp_mcd, $exp_scd);
        $query .= " SELECT ";
        $query .= "     SUM(VALUE(T1.REPAY_MONEY, 0)) AS TOTAL_REPAY, ";
        $query .= "     L1.REPAY_MONEY_DATE, ";
        $query .= "     L1.REPAY_MONEY_DIV, ";
        $query .= "     L1.REMARK ";
        $query .= " FROM ";
        $query .= "     MONEY_REPAY_S_DAT T1 ";
        $query .= "     LEFT JOIN MAX_DATA L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.SCHREGNO = L1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' ";
        $query .= "     AND T1.SCHREGNO = '".$schregno."' ";
        $query .= "     AND T1.EXPENSE_M_CD = '".$exp_mcd."' ";
        $query .= "     AND T1.EXPENSE_S_CD = '".$exp_scd."' ";
        if ($div != "TOTAL") {
            $query .= "     AND T1.REPAY_SEQ != ".$repaySeq." ";
        }
        $query .= " GROUP BY ";
        $query .= "     L1.REPAY_MONEY_DATE, ";
        $query .= "     L1.REPAY_MONEY_DIV, ";
        $query .= "     L1.REMARK ";

        return $query;
    }

    //返金詳細存在
    function ExistRePay($model, $year, $schregno)
    {
        $repaySeq = $model->field["REPAY_SEQ"] ? $model->field["REPAY_SEQ"] : "-1";

        $query  = " SELECT ";
        $query .= "     'X' ";
        $query .= " FROM ";
        $query .= "     MONEY_REPAY_S_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND SCHREGNO = '".$schregno."' ";
        $query .= "     AND REPAY_SEQ = ".$repaySeq." ";

        return $query;
    }

    //返金詳細SEQ
    function getMaxRepaySeq($year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     VALUE(MAX(REPAY_SEQ), 0) + 1 AS MAX_SEQ ";
        $query .= " FROM ";
        $query .= "     MONEY_REPAY_S_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //追加
    function &getInsertRePayQuery($db, $model, $schregno)
    {
        $repaySeq = $db->getOne(knjp150kQuery::getMaxRepaySeq($model->year, $schregno));
        $data = array();
        $data["YEAR"][TEXT]             = $model->year;
        $data["SCHREGNO"][TEXT]         = $schregno;
        $data["REPAY_SEQ"][NUMBER]      = $repaySeq;
        if ($model->cmd != "all_update") {
            $data["EXPENSE_M_CD"][TEXT]     = $model->exp_mcd;
            $data["EXPENSE_S_CD"][TEXT]     = $model->exp_scd;
            $data["REPAY_MONEY_DATE"][TEXT] = str_replace("/", "-", $model->field["REPAY_MONEY_DATE"]);
            $data["REPAY_MONEY"][NUMBER]    = $model->field["REPAY_MONEY"];
            $data["REPAY_MONEY_DIV"][TEXT]  = $model->field["REPAY_MONEY_DIV"];
            $data["REPAY_FLG"][TEXT]        = (strlen($model->field["REPAY_FLG"])) ? $model->field["REPAY_FLG"] : '0';
            $data["REMARK"][TEXT]           = $model->field["REMARK"];
        } else {
            $data["EXPENSE_M_CD"][TEXT]     = $model->field["EXPENSE_M_CD"];
            $data["EXPENSE_S_CD"][TEXT]     = $model->field["EXPENSE_S_CD"];
            if (strlen($model->field["CHECKED2"])) {
                if (!$model->repayFlgOnly) {
                    $data["REPAY_MONEY_DATE"][TEXT] = str_replace("/", "-", $model->field["REPAY_MONEY_DATE"]);
                    $data["REPAY_MONEY"][NUMBER]    = $model->field["REPAY_MONEY"];
                    $data["REPAY_MONEY_DIV"][TEXT]  = $model->field["REPAY_MONEY_DIV"];
                }
                $data["REPAY_FLG"][TEXT]        = (strlen($model->field["REPAY_FLG"])) ? $model->field["REPAY_FLG"] : '0';
            }
            if (strlen($model->field["CHECKED3"])) {
                $data["REMARK"][TEXT]           = $model->field["REMARK"];
            }
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "MONEY_REPAY_S_DAT");
        return $query;
    }

    //更新
    function &getUpdateRePayQuery($db, $model, $schregno)
    {
        $repaySeq = $model->field["REPAY_SEQ"] ? $model->field["REPAY_SEQ"] : "-1";

        $data = array();
        if ($model->cmd != "all_update") {
            $data["EXPENSE_M_CD"][TEXT]     = $model->exp_mcd;
            $data["EXPENSE_S_CD"][TEXT]     = $model->exp_scd;
            $data["REPAY_MONEY_DATE"][TEXT] = str_replace("/", "-", $model->field["REPAY_MONEY_DATE"]);
            $data["REPAY_MONEY"][NUMBER]    = $model->field["REPAY_MONEY"];
            $data["REPAY_MONEY_DIV"][TEXT]  = $model->field["REPAY_MONEY_DIV"];
            $data["REPAY_FLG"][TEXT]        = (strlen($model->field["REPAY_FLG"])) ? $model->field["REPAY_FLG"] : '0';
            $data["REMARK"][TEXT]           = $model->field["REMARK"];
        } else {
            $data["EXPENSE_M_CD"][TEXT]     = $model->field["EXPENSE_M_CD"];
            $data["EXPENSE_S_CD"][TEXT]     = $model->field["EXPENSE_S_CD"];
            if (strlen($model->field["CHECKED2"])) {
                if (!$model->repayFlgOnly) {
                    $data["REPAY_MONEY_DATE"][TEXT] = str_replace("/", "-", $model->field["REPAY_MONEY_DATE"]);
                    $data["REPAY_MONEY"][NUMBER]    = $model->field["REPAY_MONEY"];
                    $data["REPAY_MONEY_DIV"][TEXT]  = $model->field["REPAY_MONEY_DIV"];
                }
                $data["REPAY_FLG"][TEXT]        = (strlen($model->field["REPAY_FLG"])) ? $model->field["REPAY_FLG"] : '0';
            }
            if (strlen($model->field["CHECKED3"])) {
                $data["REMARK"][TEXT]           = $model->field["REMARK"];
            }
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR = '".$model->year."' ";
        $where .= "     AND SCHREGNO = '".$schregno."' ";
        $where .= "     AND REPAY_SEQ = ".$repaySeq." ";

        $query = Query::updateSQL($data, "MONEY_REPAY_S_DAT", $where);

        return $query;
    }

    //データ存在チェック(入金済み小分類データ)
    function ExistData($year, $schregno, $exp_mcd, $exp_scd, $input_flg)
    {
        $query  = " SELECT 'X' ";
        $query .= "   FROM money_paid_s_dat ";
        $query .= "  WHERE year             = '".$year."' ";
        $query .= "    AND schregno         = '".$schregno."' ";
        $query .= "    AND expense_m_cd     = '".$exp_mcd."'";
        $query .= "    AND expense_s_cd     = '".$exp_scd."'";
        $query .= "    AND paid_input_flg   = '".$input_flg."'";

        return $query;
    }

    //追加
    function &getInsertQuery($db, $model, $schregno)
    {
        $data = array();
        $data["YEAR"][TEXT]             = $model->year;
        $data["SCHREGNO"][TEXT]         = $schregno;
        if ($model->cmd != "all_update") {
            $repayRow = $db->getRow(knjp150kQuery::getTotalRePay($model, $model->year, $schregno, $model->exp_mcd, $model->exp_scd, "TOTAL"), DB_FETCHMODE_ASSOC);
            $data["EXPENSE_M_CD"][TEXT]     = $model->exp_mcd;
            $data["EXPENSE_S_CD"][TEXT]     = $model->exp_scd;
            $data["PAID_INPUT_FLG"][TEXT]   = $model->paid_input_flg;
            $data["PAID_MONEY_DATE"][TEXT]  = str_replace("/", "-", $model->field["PAID_MONEY_DATE"]);
            $data["PAID_MONEY"][NUMBER]     = $model->field["PAID_MONEY"];
            $data["PAID_MONEY_DIV"][TEXT]   = $model->field["PAID_MONEY_DIV"];
            $data["REPAY_MONEY_DATE"][TEXT] = $repayRow["REPAY_MONEY_DATE"];
            $data["REPAY_MONEY"][NUMBER]    = $repayRow["TOTAL_REPAY"];
            $data["REPAY_MONEY_DIV"][TEXT]  = $repayRow["REPAY_MONEY_DIV"];
            $data["REMARK"][TEXT]           = $repayRow["REMARK"];
        } else {
            $repayRow = $db->getRow(knjp150kQuery::getTotalRePay($model, $model->year, $schregno, $model->field["EXPENSE_M_CD"], $model->field["EXPENSE_S_CD"], "TOTAL"), DB_FETCHMODE_ASSOC);
            $data["EXPENSE_M_CD"][TEXT]     = $model->field["EXPENSE_M_CD"];
            $data["EXPENSE_S_CD"][TEXT]     = $model->field["EXPENSE_S_CD"];
            $data["PAID_INPUT_FLG"][TEXT]   = $model->div;
            if (strlen($model->field["CHECKED1"])) {
                $data["PAID_MONEY_DATE"][TEXT]  = str_replace("/", "-", $model->field["PAID_MONEY_DATE"]);
                $data["PAID_MONEY"][NUMBER]     = $model->field["PAID_MONEY"];
                $data["PAID_MONEY_DIV"][TEXT]   = $model->field["PAID_MONEY_DIV"];
            }
            if (strlen($model->field["CHECKED2"])) {
                $data["REPAY_MONEY_DATE"][TEXT] = $repayRow["REPAY_MONEY_DATE"];
                $data["REPAY_MONEY"][NUMBER]    = $repayRow["TOTAL_REPAY"];
                $data["REPAY_MONEY_DIV"][TEXT]  = $repayRow["REPAY_MONEY_DIV"];
                $data["REMARK"][TEXT]           = $repayRow["REMARK"];
            }
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "money_paid_s_dat");
        return $query;
    }

    //更新
    function &getUpdateQuery($db, $model, $schregno)
    {
        $data = array();
        if ($model->cmd != "all_update") {
            $repayRow = $db->getRow(knjp150kQuery::getTotalRePay($model, $model->year, $schregno, $model->exp_mcd, $model->exp_scd, "TOTAL"), DB_FETCHMODE_ASSOC);
            if (!strlen($model->disabled1)) {
                $data["PAID_MONEY_DATE"][TEXT]  = str_replace("/", "-", $model->field["PAID_MONEY_DATE"]);
                $data["PAID_MONEY"][NUMBER]     = $model->field["PAID_MONEY"];
                $data["PAID_MONEY_DIV"][TEXT]   = $model->field["PAID_MONEY_DIV"];
            }
            $data["REPAY_MONEY_DATE"][TEXT] = $repayRow["REPAY_MONEY_DATE"];
            $data["REPAY_MONEY"][NUMBER]    = $repayRow["TOTAL_REPAY"];
            $data["REPAY_MONEY_DIV"][TEXT]  = $repayRow["REPAY_MONEY_DIV"];
            $data["REMARK"][TEXT]           = $repayRow["REMARK"];

            $where  = " WHERE year           = '".$model->year."'";
            $where .= "   AND schregno       = '".$schregno."'";
            $where .= "   AND expense_m_cd   = '".$model->exp_mcd."'";
            $where .= "   AND expense_s_cd   = '".$model->exp_scd."'";
            $where .= "   AND paid_input_flg = '".$model->paid_input_flg."'";
        } else {
            $repayRow = $db->getRow(knjp150kQuery::getTotalRePay($model, $model->year, $schregno, $model->field["EXPENSE_M_CD"], $model->field["EXPENSE_S_CD"], "TOTAL"), DB_FETCHMODE_ASSOC);
            if ($model->div == "2" && strlen($model->field["CHECKED1"])) {
                $data["PAID_MONEY_DATE"][TEXT]  = str_replace("/", "-", $model->field["PAID_MONEY_DATE"]);
                $data["PAID_MONEY"][NUMBER]     = $model->field["PAID_MONEY"];
                $data["PAID_MONEY_DIV"][TEXT]   = $model->field["PAID_MONEY_DIV"];
            }
            if (strlen($model->field["CHECKED2"])) {
                $data["REPAY_MONEY_DATE"][TEXT] = $repayRow["REPAY_MONEY_DATE"];
                $data["REPAY_MONEY"][NUMBER]    = $repayRow["TOTAL_REPAY"];
                $data["REPAY_MONEY_DIV"][TEXT]  = $repayRow["REPAY_MONEY_DIV"];
                $data["REMARK"][TEXT]           = $repayRow["REMARK"];
            }

            $where  = " WHERE year           = '".$model->year."'";
            $where .= "   AND schregno       = '".$schregno."'";
            $where .= "   AND expense_m_cd   = '".$model->field["EXPENSE_M_CD"]."'";
            $where .= "   AND expense_s_cd   = '".$model->field["EXPENSE_S_CD"]."'";
            $where .= "   AND paid_input_flg = '".$model->div."'";
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::updateSQL($data, "money_paid_s_dat", $where);
        return $query;
    }

    function getPaidSdata($year, $schregno, $exp_mcd, $input_flg)
    {
        $query  = " SELECT ";
        $query .= "     MAX(paid_money_date)  AS paid_money_date, ";
        $query .= "     SUM(paid_money)       AS paid_money, ";
        $query .= "     MIN(paid_money_div)   AS paid_money_div, ";
        $query .= "     MAX(repay_money_date) AS repay_money_date, ";
        $query .= "     SUM(repay_money)      AS repay_money, ";
        $query .= "     MIN(repay_money_div)  AS repay_money_div ";
        $query .= " FROM ";
        $query .= "     money_paid_s_dat ";
        $query .= " WHERE ";
        $query .= "     year            = '".$year."' AND ";
        $query .= "     schregno        = '".$schregno."' AND ";
        $query .= "     expense_m_cd    = '".$exp_mcd."' AND ";
        $query .= "     paid_input_flg  = '".$input_flg."'";

        return $query;
    }

    //データ存在チェック(入金済み中分類データ)
    function ExistData2($year, $schregno, $exp_lcd, $exp_mcd, $input_flg)
    {
        $query  = " SELECT 'X' ";
        $query .= "   FROM money_paid_m_dat ";
        $query .= "  WHERE year             = '".$year."' ";
        $query .= "    AND schregno         = '".$schregno."' ";
        $query .= "    AND expense_l_cd     = '".$exp_lcd."' ";
        $query .= "    AND expense_m_cd     = '".$exp_mcd."' ";
        $query .= "    AND paid_input_flg   = '".$input_flg."'";

        return $query;
    }

    //データ存在チェック(入金済み中分類データ)
    function getPaidDiv($model, $schregno)
    {
        $query  = " SELECT paid_input_flg ";
        $query .= "   FROM money_paid_m_dat ";
        $query .= "  WHERE year             = '".$model->year."' ";
        $query .= "    AND schregno         = '".$schregno."' ";
        $query .= "    AND expense_l_cd     = '".$model->field["EXPENSE_L_CD"]."' ";
        $query .= "    AND expense_m_cd     = '".$model->field["EXPENSE_M_CD"]."' ";

        return $query;
    }

    //追加(入金済み中分類データ)
    function &getInsertQuery2($model, $schregno, $row)
    {
        $data = array();
        if ($model->cmd != "all_update") {
            $data["YEAR"][TEXT]             = $model->year;
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["EXPENSE_L_CD"][TEXT]     = $model->exp_lcd;
            $data["EXPENSE_M_CD"][TEXT]     = $model->exp_mcd;
            $data["PAID_INPUT_FLG"][TEXT]   = $model->paid_input_flg;
            $data["PAID_MONEY_DATE"][TEXT]  = $row["PAID_MONEY_DATE"];
            $data["PAID_MONEY"][NUMBER]     = $row["PAID_MONEY"];
            $data["PAID_MONEY_DIV"][TEXT]   = $row["PAID_MONEY_DIV"];
            $data["REPAY_DATE"][TEXT]       = $row["REPAY_MONEY_DATE"];
            $data["REPAY_MONEY"][NUMBER]    = $row["REPAY_MONEY"];
#            $data["REPAY_DEV"][TEXT]        = $row["REPAY_MONEY_DIV"]; #2005/12/13
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";
        } else {
            $data["YEAR"][TEXT]             = $model->year;
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["EXPENSE_L_CD"][TEXT]     = $model->field["EXPENSE_L_CD"];
            $data["EXPENSE_M_CD"][TEXT]     = $model->field["EXPENSE_M_CD"];
            $data["PAID_INPUT_FLG"][TEXT]   = $model->div;
            $data["PAID_MONEY_DATE"][TEXT]  = $row["PAID_MONEY_DATE"];
            $data["PAID_MONEY"][NUMBER]     = $row["PAID_MONEY"];
            $data["PAID_MONEY_DIV"][TEXT]   = $row["PAID_MONEY_DIV"];
            $data["REPAY_DATE"][TEXT]       = $row["REPAY_MONEY_DATE"];
            $data["REPAY_MONEY"][NUMBER]    = $row["REPAY_MONEY"];
#            $data["REPAY_DEV"][TEXT]        = $row["REPAY_MONEY_DIV"]; #2005/12/13
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";
        }

        $query = Query::insertSQL($data, "money_paid_m_dat");
        return $query;
    }

    //更新(入金済み中分類データ)
    function &getUpdateQuery2($model, $schregno, $row)
    {
        $data = array();
        $data["PAID_MONEY_DATE"][TEXT]  = $row["PAID_MONEY_DATE"];
        $data["PAID_MONEY"][NUMBER]     = $row["PAID_MONEY"];
        $data["PAID_MONEY_DIV"][TEXT]   = $row["PAID_MONEY_DIV"];
        $data["REPAY_DATE"][TEXT]       = $row["REPAY_MONEY_DATE"];
        $data["REPAY_MONEY"][NUMBER]    = $row["REPAY_MONEY"];
#        $data["REPAY_DEV"][TEXT]        = $row["REPAY_MONEY_DIV"]; #2005/12/13
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($model->cmd != "all_update") {
            $where  = " WHERE year           = '".$model->year."'";
            $where .= "   AND schregno       = '".$schregno."'";
            $where .= "   AND expense_l_cd   = '".$model->exp_lcd."'";
            $where .= "   AND expense_m_cd   = '".$model->exp_mcd."'";
            $where .= "   AND paid_input_flg = '".$model->paid_input_flg."'";
        } else {
            $where  = " WHERE year           = '".$model->year."'";
            $where .= "   AND schregno       = '".$schregno."'";
            $where .= "   AND expense_l_cd   = '".$model->field["EXPENSE_L_CD"]."'";
            $where .= "   AND expense_m_cd   = '".$model->field["EXPENSE_M_CD"]."'";
            $where .= "   AND paid_input_flg = '".$model->div."'";
        }

        $query = Query::updateSQL($data, "money_paid_m_dat", $where);
        #echo $query;
        return $query;
    }

    //削除クエリ(入金済み小分類データ)
    function &getDeleteRePayQuery($model, $schregno)
    {
        if ($model->cmd != "all_update") {
            $query  = "DELETE FROM MONEY_REPAY_S_DAT ";
            $query .= " WHERE year           = '".$model->year."'";
            $query .= "   AND schregno       = '".$schregno."'";
            $query .= "   AND expense_m_cd   = '".$model->exp_mcd."'";
            $query .= "   AND expense_s_cd   = '".$model->exp_scd."'";
        } else {
            $query  = "DELETE FROM MONEY_REPAY_S_DAT ";
            $query .= " WHERE year           = '".$model->year."'";
            $query .= "   AND schregno       = '".$schregno."'";
            $query .= "   AND expense_m_cd   = '".$model->field["EXPENSE_M_CD"]."'";
            $query .= "   AND expense_s_cd   = '".$model->field["EXPENSE_S_CD"]."'";
        }

        return $query;
    }

    //削除クエリ(入金済み小分類データ)
    function &getDeleteRePaySiteiQuery($row)
    {
        $query  = "DELETE FROM MONEY_REPAY_S_DAT ";
        $query .= " WHERE YEAR          = '".$row["YEAR"]."' ";
        $query .= "   AND SCHREGNO      = '".$row["SCHREGNO"]."' ";
        $query .= "   AND REPAY_SEQ     = ".$row["REPAY_SEQ"]." ";

        return $query;
    }

    //削除クエリ(入金済み小分類データ)
    function &getDeleteQuery($model, $schregno)
    {
        if ($model->cmd != "all_update") {
            $query  = "DELETE FROM money_paid_s_dat ";
            $query .= " WHERE year           = '".$model->year."'";
            $query .= "   AND schregno       = '".$schregno."'";
            $query .= "   AND expense_m_cd   = '".$model->exp_mcd."'";
            $query .= "   AND expense_s_cd   = '".$model->exp_scd."'";
            $query .= "   AND paid_input_flg = '".$model->paid_input_flg."'";
        } else {
            $query  = "DELETE FROM money_paid_s_dat ";
            $query .= " WHERE year           = '".$model->year."'";
            $query .= "   AND schregno       = '".$schregno."'";
            $query .= "   AND expense_m_cd   = '".$model->field["EXPENSE_M_CD"]."'";
            $query .= "   AND expense_s_cd   = '".$model->field["EXPENSE_S_CD"]."'";
            $query .= "   AND paid_input_flg = '".$model->div."'";
        }

        return $query;
    }

    //入金済み小分類データ存在チェック
    function Paid_S_Data($model, $schregno)
    {
        if ($model->cmd != "all_update") {
            $query  = " SELECT * ";
            $query .= "   FROM money_paid_s_dat ";
            $query .= "  WHERE year           = '".$model->year."'";
            $query .= "    AND schregno       = '".$schregno."'";
            $query .= "    AND expense_m_cd   = '".$model->exp_mcd."'";
            $query .= "    AND paid_input_flg = '".$model->paid_input_flg."'";
        } else {
            $query  = " SELECT * ";
            $query .= "   FROM money_paid_s_dat ";
            $query .= "  WHERE year           = '".$model->year."'";
            $query .= "    AND schregno       = '".$schregno."'";
            $query .= "    AND expense_m_cd   = '".$model->field["EXPENSE_M_CD"]."'";
            $query .= "    AND paid_input_flg = '".$model->div."'";
        }

        return $query;
    }

    //削除クエリ(入金済み中分類データ)
    function &getDeleteQuery2($model, $schregno)
    {
        if ($model->cmd != "all_update") {
            $query  = "DELETE FROM money_paid_m_dat ";
            $query .= " WHERE year           = '".$model->year."'";
            $query .= "   AND schregno       = '".$schregno."'";
            $query .= "   AND expense_l_cd   = '".$model->exp_lcd."'";
            $query .= "   AND expense_m_cd   = '".$model->exp_mcd."'";
            $query .= "   AND paid_input_flg = '".$model->paid_input_flg."'";
        } else {
            $query  = "DELETE FROM money_paid_m_dat ";
            $query .= " WHERE year           = '".$model->year."'";
            $query .= "   AND schregno       = '".$schregno."'";
            $query .= "   AND expense_l_cd   = '".$model->field["EXPENSE_L_CD"]."'";
            $query .= "   AND expense_m_cd   = '".$model->field["EXPENSE_M_CD"]."'";
            $query .= "   AND paid_input_flg = '".$model->div."'";
        }

        return $query;
    }

    //学年毎のクラスの取得
    function GetHrclass()
    {
        $query  = " SELECT ";
        $query .= "     GRADE || '-' || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一括更新画面の生徒一覧
    function GetStudent($hrClass,$sex)
    {
        $arr = explode("-",$hrClass);
        $query  = "SELECT T1.name_show, ";
        $query .= "       T2.schregno,T2.attendno, ";
        $query .= "       T3.hr_name ";
        $query .= "  FROM schreg_base_mst T1 RIGHT OUTER JOIN ";
        $query .= "       schreg_regd_dat T2 ";
        $query .= "    ON T1.schregno = T2.schregno ";
        $query .= "       INNER JOIN schreg_regd_hdat T3 ";
        $query .= "               ON T2.year     = T3.year ";
        $query .= "              AND T2.semester = T3.semester ";
        $query .= "              AND T2.grade    = T3.grade ";
        $query .= "              AND T2.hr_class = T3.hr_class ";
        if ($sex != 99){
            $query .= " WHERE T1.sex      = '".$sex."'";
            $query .= "   AND T2.year     = '".CTRL_YEAR."'";
        }else {
            $query .= " WHERE T2.year     = '".CTRL_YEAR."'";
        }
        $query .= "   AND T2.semester = '".CTRL_SEMESTER."'";
        $query .= "   AND T2.grade    = '".$arr[0]."'";
        $query .= "   AND T2.hr_class = '".$arr[1]."'";
        $query .= " ORDER BY attendno";

        return $query;
    }

    //対象費目の取得
    function getScd($year)
    {
        $query  = " SELECT ";
        $query .= "     T2.expense_l_cd || T2.expense_m_cd || T1.expense_s_cd AS totalcd, ";
        $query .= "     T1.expense_s_name, ";
        $query .= "     T1.expense_s_money, ";
        $query .= "     T2.bank_trans_sdate ";
        $query .= " FROM ";
        $query .= "     v_expense_s_mst T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     expense_m_mst T2 ";
        $query .= " ON ";
        $query .= "     T1.year = T2.year AND ";
        $query .= "     T1.expense_m_cd = T2.expense_m_cd ";
        $query .= " WHERE ";
        $query .= "     T1.year = '".$year."'";

        return $query;
    }

    //入金予定データチェック
    function MoneyDueData($model, $schregno)
    {
        $query  = " SELECT 'X' ";
        $query .= "   FROM money_due_s_dat ";
        $query .= "  WHERE year         = '".$model->year."' ";
        $query .= "    AND schregno     = '".$schregno."' ";
        $query .= "    AND expense_m_cd = '".$model->field["EXPENSE_M_CD"]."' ";
        $query .= "    AND expense_s_cd = '".$model->field["EXPENSE_S_CD"]."' ";
        
        return $query;
    }

    //分納データチェック
    function MoneyInstData($model, $schregno)
    {
        $query  = " SELECT 'X' ";
        $query .= "   FROM money_due_m_dat ";
        $query .= "  WHERE year         = '".$model->year."' ";
        $query .= "    AND schregno     = '".$schregno."' ";
        $query .= "    AND expense_m_cd = '".$model->field["EXPENSE_M_CD"]."' ";
        $query .= "    AND inst_cd IS NOT NULL ";
        
        return $query;
    }
}
?>
