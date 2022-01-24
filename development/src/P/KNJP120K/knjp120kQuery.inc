<?php

require_once('for_php7.php');

class knjp120kQuery extends Query {

    //生徒の名前の取得
    function getStudentName($schregno)
    {
        return "SELECT schregno,name_show,name_kana FROM schreg_base_mst WHERE schregno ='".$schregno."'";
    }

    //名称マスタより取得
    function getNamecd($year,$cd)
    {
        return " SELECT namecd2,name1 FROM v_name_mst WHERE year = '".$year."' AND namecd1 = '".$cd."' ";
    }

    //リストの取得
    function getList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.expense_l_cd,T1.expense_m_cd,T4.expense_m_name, ";
        $query .= "     T4.bank_trans_sdate,T4.expense_s_exist_flg, ";
        $query .= "     T2.money_due, ";
        $query .= "     T3.paid_money,T3.paid_money_date,T3.paid_money_div,T3.repay_dev, ";
        $query .= "     T3.repay_money,T3.paid_input_flg, ";
        $query .= "     T2.bank_trans_stop_reson,T2.inst_cd, ";
        $query .= "     CASE WHEN T1.expense_m_cd = '13' THEN T5.REDUC_DEC_FLG_1 ELSE '' END AS REDUC_DEC_FLG_1, ";	//NO003
        $query .= "     CASE WHEN T1.expense_m_cd = '13' THEN T5.REDUC_DEC_FLG_2 ELSE '' END AS REDUC_DEC_FLG_2 ";	//NO003
        $query .= " FROM ";
        $query .= "     (SELECT wt1.year,wt1.schregno,wt2.expense_l_cd,wt1.expense_m_cd ";
        $query .= "        FROM money_due_m_dat wt1, ";
        $query .= "             expense_m_mst wt2 ";
        $query .= "       WHERE wt1.year            = wt2.year ";
        $query .= "         AND wt1.expense_m_cd    = wt2.expense_m_cd ";
        $query .= "         AND wt1.year            = '".$model->year."' ";
        $query .= "         AND wt1.schregno        = '".$model->schregno."' ";
        $query .= "       UNION ";
        $query .= "      SELECT year,schregno,expense_l_cd,expense_m_cd ";
        $query .= "        FROM money_paid_m_dat ";
        $query .= "       WHERE year        = '".$model->year."' ";
        $query .= "         AND schregno    = '".$model->schregno."' ";
        $query .= "     ) T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     money_due_m_dat T2 ";
        $query .= " ON ";
        $query .= "     T1.year         = T2.year AND ";
        $query .= "     T1.schregno     = T2.schregno AND ";
        $query .= "     T1.expense_m_cd = T2.expense_m_cd ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     money_paid_m_dat T3 ";
        $query .= " ON ";
        $query .= "     T1.year         = T3.year AND ";
        $query .= "     T1.schregno     = T3.schregno AND ";
        $query .= "     T1.expense_l_cd = T3.expense_l_cd AND ";
        $query .= "     T1.expense_m_cd = T3.expense_m_cd ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     expense_m_mst T4 ";
        $query .= " ON ";
        $query .= "     T1.year         = T4.year AND ";
        $query .= "     T1.expense_m_cd = T4.expense_m_cd ";        
		//NO003-->
        $query .= " LEFT JOIN ";
        $query .= "     REDUCTION_DAT T5 ";
        $query .= " ON ";
        $query .= "     T1.YEAR         = T5.YEAR AND ";
        $query .= "     T1.SCHREGNO = T5.SCHREGNO ";        
		//NO003<--
        $query .= " ORDER BY ";
        $query .= "     T1.expense_m_cd ";

        return $query;
    }

    //リストの取得 NO001 NO005
    function getList2(&$model)
    {

        $query  = "";
        $query .= "  SELECT ";
        $query .= "      T1.SCHREGNO,";
        $query .= "      T1.TRANSFER_SDATE,";
        $query .= "      T1.TRANSFER_EDATE,";
        $query .= "      T1.TRANSFERCD,";
        $query .= "      MEISYOU_GET(T1.TRANSFERCD,'A004',1) AS TRANSFERNAME,";
        $query .= "      T1.TRANSFERREASON,";
        $query .= "      T1.TRANSFERPLACE,";
        $query .= "      T1.TRANSFERADDR,";
        $query .= "      T1.ABROAD_CLASSDAYS,";
        $query .= "      T1.ABROAD_CREDITS";
        $query .= "  FROM";
        $query .= "      SCHREG_TRANSFER_DAT T1";
        $query .= "  WHERE";
        $query .= "      T1.SCHREGNO = '".$model->schregno."'";
        $query .= "  ORDER BY";
        $query .= "      T1.TRANSFER_SDATE,";
        $query .= "      T1.TRANSFERCD";

        return $query;
    }


    //リストの取得 NO001 NO005
    function getList3(&$model)
    {

        $query  = "";
        $query .= "  SELECT ";
        $query .= "      T1.SCHREGNO,";
        $query .= "      T1.GRD_DATE,";
        $query .= "      MEISYOU_GET(T1.GRD_DIV,'A003',1) AS GRDNAME ";
        $query .= "  FROM";
        $query .= "      SCHREG_BASE_MST T1";
        $query .= "  WHERE";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "      AND (T1.GRD_DIV IS NOT NULL OR T1.GRD_DIV != '') ";
        $query .= "      AND T1.GRD_DIV > '1' ";
		//NO005-->
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      T1.SCHREGNO,";
        $query .= "      T1.ENT_DATE AS GRD_DATE,";
        $query .= "      MEISYOU_GET(T1.ENT_DIV,'A003',1) AS GRDNAME ";
        $query .= "  FROM";
        $query .= "      SCHREG_BASE_MST T1";
        $query .= "  WHERE";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "      AND (T1.ENT_DIV IS NOT NULL OR T1.ENT_DIV != '') ";
        $query .= "      AND T1.ENT_DIV > '3' ";
        $query .= "  ORDER BY";
        $query .= "      GRD_DATE";
		//NO005<--

        return $query;
    }

    //入金必要金額の取得
    function getMoneyDue($schregno, $m_cd)
    {
        $query  = " SELECT ";
        $query .= "     SUM(MONEY_DUE) AS MONEY_DUE ";
        $query .= "   FROM ";
        $query .= "     money_due_s_dat ";
        $query .= "  WHERE ";
        $query .= "     year = '".CTRL_YEAR."' AND ";
        $query .= "     schregno = '".$schregno."' AND ";
        $query .= "     expense_m_cd = '".$m_cd."'";

        return $query;
    }

    //小分類返金区分の取得
    function getRepayDiv($schregno, $m_cd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT, ";
        $query .= "     MIN(T1.REPAY_MONEY_DIV) AS MIN_DIV, ";
        $query .= "     MAX(T1.REPAY_MONEY_DIV) AS MAX_DIV ";
        $query .= " FROM ";
        $query .= "     MONEY_PAID_S_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCHREGNO = '".$schregno."' ";
        $query .= "     AND T1.EXPENSE_M_CD = '".$m_cd."' ";
        $query .= "     AND T1.REPAY_MONEY_DIV IS NOT NULL ";

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
    function getRow($model)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT ";
        $query .= "     T1.paid_money, ";
        $query .= "     T1.paid_money_date, ";
        $query .= "     T1.paid_money_div, ";
        $query .= "     T1.repay_money, ";
        $query .= "     T1.repay_dev, ";
        $query .= "     T1.repay_date, ";
        $query .= "     T2.bank_trans_stop_reson ";
        $query .= " FROM ";
        $query .= "     money_paid_m_dat T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     money_due_m_dat T2 ";
        $query .= " ON ";
        $query .= "     T1.year = T2.year AND ";
        $query .= "     T1.schregno = T2.schregno AND ";
        $query .= "     T1.expense_m_cd = T2.expense_m_cd ";        
        $query .= " WHERE ";
        $query .= "     T1.year             = '".$model->year."' AND ";
        $query .= "     T1.schregno         = '".$model->schregno."' AND ";
        $query .= "     T1.expense_l_cd     = '".$model->exp_lcd."' AND ";
        $query .= "     T1.expense_m_cd     = '".$model->exp_mcd."' AND ";
        $query .= "     T1.paid_input_flg   = '".$model->paid_input_flg."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //データ存在チェック
    function ExistData($model)
    {
        $query  = " SELECT 'X' ";
        $query .= "   FROM money_paid_m_dat ";
        $query .= "  WHERE year             = '".$model->year."' ";
        $query .= "    AND schregno         = '".$model->schregno."' ";
        $query .= "    AND expense_l_cd     = '".$model->exp_lcd."'";
        $query .= "    AND expense_m_cd     = '".$model->exp_mcd."'";
        $query .= "    AND paid_input_flg   = '".$model->paid_input_flg."'";
        # echo $query;
        return $query;
    }

    //追加
    function &getInsertQuery($model)
    {
        $data = array();
        $data["YEAR"][TEXT]             = $model->year;
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["EXPENSE_L_CD"][TEXT]     = $model->exp_lcd;
        $data["EXPENSE_M_CD"][TEXT]     = $model->exp_mcd;
        $data["PAID_INPUT_FLG"][TEXT]   = (strlen($model->paid_input_flg)) ? $model->paid_input_flg : '2';
        $data["PAID_MONEY"][NUMBER]     = $model->field["PAID_MONEY"];
        $data["PAID_MONEY_DATE"][TEXT]  = str_replace("/", "-", $model->field["PAID_MONEY_DATE"]);
        $data["PAID_MONEY_DIV"][TEXT]   = $model->field["PAID_MONEY_DIV"];
        $data["REPAY_MONEY"][NUMBER]    = $model->field["REPAY_MONEY"];
        $data["REPAY_DEV"][TEXT]        = $model->field["REPAY_DEV"];
        $data["REPAY_DATE"][TEXT]       = str_replace("/", "-", $model->field["REPAY_DATE"]);
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "money_paid_m_dat");
        return $query;
    }

    //更新
    function &getUpdateQuery($model)
    {
        $data = array();
        
        if ($model->s_exist_flg != "1") {   # 2005/12/13 小分類を有する費目(生活行事)は、返金区分のみの入力を認める
            if (!strlen($model->inst_cd) && !strlen($model->paid_flg)) {
                $data["PAID_MONEY"][NUMBER]     = $model->field["PAID_MONEY"];
                $data["PAID_MONEY_DATE"][TEXT]  = str_replace("/", "-", $model->field["PAID_MONEY_DATE"]);
                $data["PAID_MONEY_DIV"][TEXT]   = $model->field["PAID_MONEY_DIV"];
            }
            $data["REPAY_MONEY"][NUMBER]    = $model->field["REPAY_MONEY"];
            $data["REPAY_DATE"][TEXT]       = str_replace("/", "-", $model->field["REPAY_DATE"]);
        }
        $data["REPAY_DEV"][TEXT]        = $model->field["REPAY_DEV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE year           = '".$model->year."'";
        $where .= "   AND schregno       = '".$model->schregno."'";
        $where .= "   AND expense_l_cd   = '".$model->exp_lcd."'";
        $where .= "   AND expense_m_cd   = '".$model->exp_mcd."'";
        $where .= "   AND paid_input_flg = '".$model->paid_input_flg."'";

        $query = Query::updateSQL($data, "money_paid_m_dat", $where);
        # echo $query;
        return $query;
    }

    //削除  データ(入金データ、返金データ)無しの場合、レコード削除
    function getDeleteQuery($model)
    {
        $query  = " DELETE FROM  money_paid_m_dat ";
        $query .= "  WHERE year             = '".$model->year."' ";
        $query .= "    AND schregno         = '".$model->schregno."' ";
        $query .= "    AND expense_l_cd     = '".$model->exp_lcd."'";
        $query .= "    AND expense_m_cd     = '".$model->exp_mcd."'";
        $query .= "    AND paid_input_flg   = '".$model->paid_input_flg."'";
        $query .= "    AND paid_money_date  IS NULL";
        $query .= "    AND paid_money       IS NULL";
        $query .= "    AND paid_money_div   IS NULL";
        $query .= "    AND repay_date       IS NULL";
        $query .= "    AND repay_money      IS NULL";
        $query .= "    AND repay_dev        IS NULL";

        return $query;
    }


    //振替停止の更新
    function &getUpdateQuery2($model)
    {
        $data = array();
        $data["BANK_TRANS_STOP_RESON"][TEXT]    = $model->field["BANK_TRANS_STOP_RESON"];
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][NUMBER]                = "sysdate()";

        $where  = " WHERE year           = '".$model->year."'";
        $where .= "   AND schregno       = '".$model->schregno."'";
        $where .= "   AND expense_m_cd   = '".$model->exp_mcd."'";

        $query = Query::updateSQL($data, "money_due_m_dat", $where);
        return $query;
    }
}
?>
