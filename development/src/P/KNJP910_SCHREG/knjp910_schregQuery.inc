<?php

require_once('for_php7.php');


class knjp910_schregQuery extends Query {

    //本締めデータチェック
    function getCloseFlgData($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     LEVY_CLOSE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND YEAR        = '{$model->year}' ";
        $query .= " AND CLOSE_FLG   = '1' ";

        return $query;
    }

    //決済の状況を取得(比較時に利用)
    function getLevyData($model, $flg) {
        $query  = "";
        $query .= " SELECT ";
        if ($flg == "APPROVAL") {
            $query .= "     T1.OUTGO_APPROVAL ";
        } else if ($flg == "CANCEL") {
            $query .= "     T1.OUTGO_CANCEL ";
        } else if ($flg == "COLLECT") {
            $query .= "     T1.COLLECT_L_CD || T1.COLLECT_M_CD || T1.COLLECT_S_CD || T1.COLLECT_GRP_CD AS COLLECT_L_M_S_GRP_CD ";
        } else if ($flg == "HENKIN_FLG") {
            $query .= "     T1.HENKIN_FLG ";
        } else {
            $query .= "     T1.HENKIN_APPROVAL ";
        }
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= "     AND T1.YEAR        = '{$model->year}' ";
        $query .= "     AND T1.OUTGO_L_CD  = '{$model->getOutgoLcd}' ";
        $query .= "     AND T1.OUTGO_L_CD || T1.OUTGO_M_CD = '{$model->getOutgoLMcd}' ";
        $query .= "     AND T1.REQUEST_NO  = '{$model->getRequestNo}' ";
        return $query;
    }

    //一覧取得
    function getRow($model) {
    
        $db = Query::dbCheckOut();
    
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T1.OUTGO_L_CD || T1.OUTGO_M_CD || T1.OUTGO_S_CD AS OUTGO_L_M_S_CD ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_MEISAI_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= "     AND T1.YEAR        = '{$model->year}' ";
        $query .= "     AND T1.OUTGO_L_CD  = '{$model->getOutgoLcd}' ";
        $query .= "     AND T1.OUTGO_L_CD || T1.OUTGO_M_CD = '{$model->getOutgoLMcd}' ";
        $query .= "     AND T1.REQUEST_NO  = '{$model->getRequestNo}' ";
        //$query .= "     AND T1.OUTGO_S_CD = '".sprintf("%02d", $model->getLineNo)."' ";
        $query .= "     AND T1.LINE_NO     = ".$model->getLineNo." ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.OUTGO_L_CD, ";
        $query .= "     T1.OUTGO_M_CD, ";
        $query .= "     T1.REQUEST_NO ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //支出科目
    function getLevyMDiv($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD || ':' || T1.LEVY_M_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_M_MST T1 ";
        $query .= "     INNER JOIN LEVY_L_MST L1 ON L1.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                             AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                             AND L1.YEAR        = T1.YEAR ";
        $query .= "                             AND L1.LEVY_L_CD   = T1.LEVY_L_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND T1.YEAR        = '{$model->year}' ";
        $query .= " AND T1.LEVY_L_CD || T1.LEVY_M_CD = '{$model->getOutgoLMcd}' ";

        return $query;
    }

    //支出細目
    function getLevySDiv($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD || T1.LEVY_S_CD || ':' || T1.LEVY_S_NAME AS LABEL, ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD || T1.LEVY_S_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     LEVY_S_MST T1 ";
        $query .= "     INNER JOIN LEVY_M_MST L1 ON L1.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                             AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                             AND L1.YEAR        = T1.YEAR ";
        $query .= "                             AND L1.LEVY_L_CD   = T1.LEVY_L_CD ";
        $query .= "                             AND L1.LEVY_M_CD   = T1.LEVY_M_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND T1.YEAR        = '{$model->year}' ";
        $query .= " AND T1.LEVY_L_CD || T1.LEVY_M_CD = '{$model->getOutgoLMcd}' ";
        //返金時のみ
        if ($model->getHenkinFlg) {
            $query .= " AND T1.REPAY_DIV = '1' ";
        }
        return $query;
    }

    //請求月を取得
    function getSeikyuMonth() {
    
        $query  = " SELECT ";
        for ($month = 1; $month < 13; $month++) {
            if ($month != 1) {
                $query .= " UNION ";
                $query .= " SELECT ";
            }
            $query .= "     $month AS ORDER_MONTH, ";
            $query .= "     '{$month}' AS VALUE, ";
            $query .= "     '{$month}' || '月分' AS LABEL ";
            $query .= " FROM ";
            $query .= "     SYSIBM.SYSDUMMY1 ";
        }
        $query .= " ORDER BY ";
        $query .= "     ORDER_MONTH ";

        return $query;
    }

    //生徒取得
    function getSchno($model, $flg="") {
        //収入額 (生徒ごと)
        $query  = " WITH SCHREG_SELECT_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_SCHREG_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND T1.YEAR        = '{$model->year}' ";
        $query .= " AND T1.OUTGO_L_CD  = '{$model->getOutgoLcd}' ";
        $query .= " AND T1.OUTGO_L_CD || T1.OUTGO_M_CD = '{$model->getOutgoLMcd}' ";
        $query .= " AND T1.REQUEST_NO  = '{$model->getRequestNo}' ";
        //$query .= " AND T1.OUTGO_S_CD = '".sprintf("%02d", $model->getLineNo)."' ";
        $query .= " AND T1.LINE_NO     = ".$model->getLineNo." ";
        $query .= " ) ";

        $query .= " SELECT  ";
        $query .= "     T1.SCHREGNO AS VALUE,  ";
        //年組番表示
        if ($model->field["HR_CLASS_HYOUJI_FLG"] === '1') {
            $query .= "     SCH_RH.HR_NAME || T1.ATTENDNO || '番' || '　' || T2.NAME AS LABEL  ";
        } else {
            $query .= "     T1.SCHREGNO || '　' || T2.NAME AS LABEL  ";
        }
        $query .= " FROM  ";
        $query .= "     SCHREG_REGD_DAT T1  ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT SCH_RH ON T1.YEAR = SCH_RH.YEAR ";
        $query .= "                                      AND T1.SEMESTER = SCH_RH.SEMESTER ";
        $query .= "                                      AND T1.GRADE = SCH_RH.GRADE ";
        $query .= "                                      AND T1.HR_CLASS = SCH_RH.HR_CLASS ";
        $query .= "    ,SCHREG_BASE_MST T2 ";
        //選択した伝票番号のLINE_NOの生徒データのみを取得
        if ($flg) {
            $query .= "     INNER JOIN SCHREG_SELECT_DATA L2 ON L2.SCHREGNO = T2.SCHREGNO ";
        }
        $query .= " WHERE  ";
        $query .= "     T1.YEAR = '{$model->year}'  ";
        $query .= " AND T1.SEMESTER = '".CTRL_SEMESTER."'  ";
        $query .= " AND T2.SCHREGNO   = T1.SCHREGNO  ";
        //選択した伝票番号のLINE_NOの生徒データを除く(右側)
        if ($flg == "") {
            $query .= " AND T2.SCHREGNO NOT IN (SELECT ";
            $query .= "                             SCHREGNO ";
            $query .= "                         FROM ";
            $query .= "                             SCHREG_SELECT_DATA ";
            $query .= "                         ) ";
        }
        $query .= " ORDER BY  ";
        //年組番表示
        if ($model->field["HR_CLASS_HYOUJI_FLG"] === '1') {
            $query .= "   T1.GRADE || T1.HR_CLASS || T1.ATTENDNO ";
        } else {
            $query .= "     SUBSTR(T1.SCHREGNO, 1, 4) DESC, ";
            $query .= "     SUBSTR(T1.SCHREGNO, 5)  ";
        }

        return $query;
    }

    //返金用生徒取得
    function getHenkinSchno($model, $flg="") {
        //入金項目または細目
        $collectlcd = substr($model->getCollectLMSGrpcd, 0, 2);
        $collectmcd = substr($model->getCollectLMSGrpcd, 2, 2);
        $collectscd = substr($model->getCollectLMSGrpcd, 4, 2);
        $collectgrpcd = substr($model->getCollectLMSGrpcd, 6, 4);
        //支出細目をセット
        $outgoscd = substr($model->field["OUTGO_L_M_S_CD"], 4, 2);
        //対象テーブルセット
        if ($collectscd == '00') {
            $dueTablename = "COLLECT_MONEY_DUE_M_DAT";
            $paidTablename = "COLLECT_MONEY_PAID_M_DAT";
            $repayTablename = "COLLECT_MONEY_REPAY_M_DAT";
        } else {
            $dueTablename = "COLLECT_MONEY_DUE_S_DAT";
            $paidTablename = "COLLECT_MONEY_PAID_S_DAT";
            $repayTablename = "COLLECT_MONEY_REPAY_S_DAT";
        }
    
        //返金予定額 (生徒ごと)
        $query  = " WITH SCHREG_SELECT_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_SCHREG_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND T1.YEAR        = '{$model->year}' ";
        $query .= " AND T1.OUTGO_L_CD  = '{$model->getOutgoLcd}' ";
        $query .= " AND T1.OUTGO_L_CD || T1.OUTGO_M_CD = '{$model->getOutgoLMcd}' ";
        $query .= " AND T1.REQUEST_NO  = '{$model->getRequestNo}' ";
        //$query .= " AND T1.OUTGO_S_CD = '".sprintf("%02d", $model->getLineNo)."' ";
        $query .= " AND T1.LINE_NO     = ".$model->getLineNo." ";
        
        //対象データ取得
        //収入額がある生徒
        $query .= " ), INCOME_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     L1.SCHREGNO, ";
        $query .= "     SUM(INCOME_MONEY) AS INCOME_MONEY ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT T1 ";
        $query .= "     INNER JOIN LEVY_REQUEST_INCOME_SCHREG_DAT L1 ON L1.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                                                 AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                                                 AND L1.YEAR        = T1.YEAR ";
        $query .= "                                                 AND L1.INCOME_L_CD = T1.INCOME_L_CD ";
        $query .= "                                                 AND L1.INCOME_M_CD = T1.INCOME_M_CD ";
        $query .= "                                                 AND L1.REQUEST_NO  = T1.REQUEST_NO ";
        $query .= "     INNER JOIN LEVY_REQUEST_INCOME_MEISAI_DAT L2 ON L2.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                                                 AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                                                 AND L2.YEAR        = T1.YEAR ";
        $query .= "                                                 AND L2.INCOME_L_CD = T1.INCOME_L_CD ";
        $query .= "                                                 AND L2.INCOME_M_CD = T1.INCOME_M_CD ";
        $query .= "                                                 AND L2.REQUEST_NO  = T1.REQUEST_NO ";
        $query .= "                                                 AND L2.INCOME_S_CD = L1.INCOME_S_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND T1.YEAR        = '{$model->year}' ";
        $query .= " AND T1.INCOME_APPROVAL = '1' ";
        $query .= " AND T1.INCOME_CANCEL IS NULL ";
        $query .= " GROUP BY ";
        $query .= "     L1.SCHREGNO ";
        //支出額がある生徒
        $query .= " ), OUTGO_DATA AS (     ";
        $query .= " SELECT ";
        $query .= "     L1.SCHREGNO, ";
        $query .= "     SUM(OUTGO_MONEY) AS OUTGO_MONEY ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT T1 ";
        $query .= "     INNER JOIN LEVY_REQUEST_OUTGO_SCHREG_DAT L1 ON L1.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                                                AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                                                AND L1.YEAR        = T1.YEAR ";
        $query .= "                                                AND L1.OUTGO_L_CD  = T1.OUTGO_L_CD ";
        $query .= "                                                AND L1.OUTGO_M_CD  = T1.OUTGO_M_CD ";
        $query .= "                                                AND L1.REQUEST_NO  = T1.REQUEST_NO ";
        $query .= "     INNER JOIN LEVY_REQUEST_OUTGO_MEISAI_DAT L2 ON L2.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                                                AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                                                AND L2.YEAR        = T1.YEAR ";
        $query .= "                                                AND L2.OUTGO_L_CD  = T1.OUTGO_L_CD ";
        $query .= "                                                AND L2.OUTGO_M_CD  = T1.OUTGO_M_CD ";
        $query .= "                                                AND L2.REQUEST_NO  = T1.REQUEST_NO ";
        $query .= "                                                AND L2.OUTGO_S_CD  = L1.OUTGO_S_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND T1.YEAR        = '{$model->year}' ";
        //$query .= " AND T1.OUTGO_APPROVAL = '1' ";
        $query .= " AND T1.OUTGO_CANCEL IS NULL ";
        $query .= " GROUP BY ";
        $query .= "     L1.SCHREGNO ";
        //収入額 - 支出額 >= 0が対象
        $query .= " ), TAISHOU_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     VALUE(T1.INCOME_MONEY, 0) - VALUE(L1.OUTGO_MONEY, 0) AS ZAN_GK ";
        $query .= " FROM ";
        $query .= "     INCOME_DATA T1 ";
        $query .= "     LEFT JOIN OUTGO_DATA L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     VALUE(T1.INCOME_MONEY, 0) - VALUE(L1.OUTGO_MONEY, 0) >= 0 ";

        //入金データがある生徒を取得
        $query  .= " ), PAID_TOTAL AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(L1.PAID_MONEY) AS PAID_MONEY_TOTAL ";
        $query .= " FROM ";
        $query .= "     $dueTablename T1 ";
        $query .= "     INNER JOIN $paidTablename L1 ON L1.SCHOOLCD       = T1.SCHOOLCD ";
        $query .= "                                 AND L1.SCHOOL_KIND    = T1.SCHOOL_KIND ";        
        $query .= "                                 AND L1.YEAR           = T1.YEAR ";        
        $query .= "                                 AND L1.SCHREGNO       = T1.SCHREGNO ";        
        $query .= "                                 AND L1.COLLECT_GRP_CD = T1.COLLECT_GRP_CD  ";
        $query .= "                                 AND L1.COLLECT_L_CD   = T1.COLLECT_L_CD  ";
        $query .= "                                 AND L1.COLLECT_M_CD   = T1.COLLECT_M_CD  ";
        if ($dueTablename == "COLLECT_MONEY_DUE_S_DAT") {
            $query .= "                                AND L1.COLLECT_S_CD = T1.COLLECT_S_CD  ";
        }
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD       = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND    = '".$model->getSchoolKind."'";
        $query .= "     AND T1.YEAR           = '".$model->year."' ";
        $query .= "     AND T1.COLLECT_GRP_CD = '".$collectgrpcd."' ";
        $query .= "     AND T1.COLLECT_L_CD   = '".$collectlcd."' ";
        $query .= "     AND T1.COLLECT_M_CD   = '".$collectmcd."' ";
        if ($dueTablename == "COLLECT_MONEY_DUE_S_DAT") {
            $query .= "     AND T1.COLLECT_S_CD     = '".$collectscd."' ";
        }
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";
        //返金データがある生徒を取得
        $query .= " ), REPAY_TOTAL AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(L1.REPAY_MONEY) AS REPAY_MONEY_TOTAL ";
        $query .= " FROM ";
        $query .= "     $dueTablename T1 ";
        $query .= "     INNER JOIN $repayTablename L1 ON L1.SCHOOLCD       = T1.SCHOOLCD ";
        $query .= "                                  AND L1.SCHOOL_KIND    = T1.SCHOOL_KIND ";        
        $query .= "                                  AND L1.YEAR           = T1.YEAR ";        
        $query .= "                                  AND L1.SCHREGNO       = T1.SCHREGNO ";        
        $query .= "                                  AND L1.COLLECT_GRP_CD = T1.COLLECT_GRP_CD  ";
        $query .= "                                  AND L1.COLLECT_L_CD   = T1.COLLECT_L_CD  ";
        $query .= "                                  AND L1.COLLECT_M_CD   = T1.COLLECT_M_CD  ";
        if ($dueTablename == "COLLECT_MONEY_DUE_S_DAT") {
            $query .= "                                AND L1.COLLECT_S_CD = T1.COLLECT_S_CD  ";
        }
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD       = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND    = '".$model->getSchoolKind."'";
        $query .= "     AND T1.YEAR           = '".$model->year."' ";
        $query .= "     AND T1.COLLECT_GRP_CD = '".$collectgrpcd."' ";
        $query .= "     AND T1.COLLECT_L_CD   = '".$collectlcd."' ";
        $query .= "     AND T1.COLLECT_M_CD   = '".$collectmcd."' ";
        if ($dueTablename == "COLLECT_MONEY_DUE_S_DAT") {
            $query .= "     AND T1.COLLECT_S_CD     = '".$collectscd."' ";
        }
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";
        //入金額 - 返金額 >= 0が対象
        $query .= " ), REPAY_ABLE_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     PAID_TOTAL T1";
        $query .= "     LEFT JOIN REPAY_TOTAL L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     VALUE(T1.PAID_MONEY_TOTAL, 0) - VALUE(L1.REPAY_MONEY_TOTAL, 0) >= 0 ";
        
        //対象の生徒データ表示
        $query .= " )";
        $query .= " SELECT  ";
        $query .= "     T1.SCHREGNO AS VALUE,  ";
        //年組番表示
        if ($model->field["HR_CLASS_HYOUJI_FLG"] === '1') {
            $query .= "     SCH_RH.HR_NAME || T1.ATTENDNO || '番' || '　' || T2.NAME || '( 収入残額:' || CHAR(L1.ZAN_GK) || ')' AS LABEL  ";
        } else {
            $query .= "     T1.SCHREGNO || '　' || T2.NAME || '( 収入残額:' || CHAR(L1.ZAN_GK) || ')' AS LABEL  ";
        }
        $query .= " FROM  ";
        $query .= "     SCHREG_REGD_DAT T1  ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT SCH_RH ON T1.YEAR = SCH_RH.YEAR ";
        $query .= "                                      AND T1.SEMESTER = SCH_RH.SEMESTER ";
        $query .= "                                      AND T1.GRADE = SCH_RH.GRADE ";
        $query .= "                                      AND T1.HR_CLASS = SCH_RH.HR_CLASS ";
        $query .= "    ,SCHREG_BASE_MST T2 ";
        $query .= "     INNER JOIN TAISHOU_DATA L1 ON L1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     INNER JOIN REPAY_ABLE_DATA D1 ON D1.SCHREGNO = T2.SCHREGNO ";
        //選択した伝票番号の生徒データのみを取得
        if ($flg) {
            $query .= "     INNER JOIN SCHREG_SELECT_DATA L2 ON L2.SCHREGNO = T2.SCHREGNO ";
        }
        $query .= " WHERE  ";
        $query .= "     T1.YEAR = '{$model->year}'  ";
        $query .= " AND T1.SEMESTER = '".CTRL_SEMESTER."'  ";
        $query .= " AND T2.SCHREGNO   = T1.SCHREGNO  ";
        
        //選択した伝票番号の生徒データを除く(右側)
        if ($flg == "") {
            $query .= " AND T2.SCHREGNO NOT IN (SELECT ";
            $query .= "                             SCHREGNO ";
            $query .= "                         FROM ";
            $query .= "                             SCHREG_SELECT_DATA ";
            $query .= "                         ) ";
        }
        $query .= " ORDER BY  ";
        //年組番表示
        if ($model->field["HR_CLASS_HYOUJI_FLG"] === '1') {
            $query .= "   T1.GRADE || T1.HR_CLASS || T1.ATTENDNO ";
        } else {
            $query .= "     SUBSTR(T1.SCHREGNO, 1, 4) DESC, ";
            $query .= "     SUBSTR(T1.SCHREGNO, 5)  ";
        }
        return $query;
    }

    //データチェック用1
    //返金用収入額の残額を取得 (ただし、入力中の伝票+NOは除く)
    function getSchnoMoneyData($model) {
        //対象生徒
        $schregMoneyData = array();
        $schregMoneyData = explode(",", $model->selectStudent);
        //支出細目をセット
        $outgoscd = substr($model->field["OUTGO_L_M_S_CD"], 4, 2);
        
        //対象データ取得
        //収入額がある生徒
        $query  = " WITH INCOME_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     L1.SCHREGNO, ";
        $query .= "     SUM(INCOME_MONEY) AS INCOME_MONEY ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT T1 ";
        $query .= "     INNER JOIN LEVY_REQUEST_INCOME_SCHREG_DAT L1 ON L1.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                                                 AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                                                 AND L1.YEAR        = T1.YEAR ";
        $query .= "                                                 AND L1.INCOME_L_CD = T1.INCOME_L_CD ";
        $query .= "                                                 AND L1.INCOME_M_CD = T1.INCOME_M_CD ";
        $query .= "                                                 AND L1.REQUEST_NO  = T1.REQUEST_NO ";
        $query .= "     INNER JOIN LEVY_REQUEST_INCOME_MEISAI_DAT L2 ON L2.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                                                 AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                                                 AND L2.YEAR        = T1.YEAR ";
        $query .= "                                                 AND L2.INCOME_L_CD = T1.INCOME_L_CD ";
        $query .= "                                                 AND L2.INCOME_M_CD = T1.INCOME_M_CD ";
        $query .= "                                                 AND L2.REQUEST_NO  = T1.REQUEST_NO ";
        $query .= "                                                 AND L2.INCOME_S_CD = L1.INCOME_S_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND T1.SCHOOL_KIND     = '".$model->getSchoolKind."'";
        $query .= " AND T1.YEAR            = '{$model->year}' ";
        $query .= " AND T1.INCOME_APPROVAL = '1' ";
        $query .= " AND T1.INCOME_CANCEL IS NULL ";
        //選択した生徒のみをチェック
        $query .= " AND L1.SCHREGNO IN ( ";
        $count = 0;
        $conma = "";
        foreach ($schregMoneyData as $schregMoneyvalue) {
            list($schregno, $money) = explode("-", $schregMoneyvalue);
            $query .= " $conma'".$schregno."' ";
            $conma = ",";
            $count++;
        }
        $query .= " ) ";
        $query .= " GROUP BY ";
        $query .= "     L1.SCHREGNO ";
        //支出額がある生徒
        $query .= " ), OUTGO_DATA AS (     ";
        $query .= " SELECT ";
        $query .= "     L1.SCHREGNO, ";
        $query .= "     SUM(OUTGO_MONEY) AS OUTGO_MONEY ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT T1 ";
        $query .= "     INNER JOIN LEVY_REQUEST_OUTGO_SCHREG_DAT L1 ON L1.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                                                AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                                                AND L1.YEAR        = T1.YEAR ";
        $query .= "                                                AND L1.OUTGO_L_CD  = T1.OUTGO_L_CD ";
        $query .= "                                                AND L1.OUTGO_M_CD  = T1.OUTGO_M_CD ";
        $query .= "                                                AND L1.REQUEST_NO  = T1.REQUEST_NO ";
        $query .= "     INNER JOIN LEVY_REQUEST_OUTGO_MEISAI_DAT L2 ON L2.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                                                AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                                                AND L2.YEAR        = T1.YEAR ";
        $query .= "                                                AND L2.OUTGO_L_CD  = T1.OUTGO_L_CD ";
        $query .= "                                                AND L2.OUTGO_M_CD  = T1.OUTGO_M_CD ";
        $query .= "                                                AND L2.REQUEST_NO  = T1.REQUEST_NO ";
        $query .= "                                                AND L2.OUTGO_S_CD  = L1.OUTGO_S_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND T1.YEAR        = '{$model->year}' ";
        //$query .= " AND T1.OUTGO_APPROVAL = '1' ";
        $query .= " AND T1.OUTGO_CANCEL IS NULL ";
        //選択した生徒のみをチェック
        $query .= " AND L1.SCHREGNO IN ( ";
        $count = 0;
        $conma = "";
        foreach ($schregMoneyData as $schregMoneyvalue) {
            list($schregno, $money) = explode("-", $schregMoneyvalue);
            $query .= " $conma'".$schregno."' ";
            $conma = ",";
            $count++;
        }
        $query .= " ) ";
        $query .= " GROUP BY ";
        $query .= "     L1.SCHREGNO ";
        //更新前の収入額 - 支出額 >= 0 の金額
        $query .= " ), TAISHOU_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     VALUE(T1.INCOME_MONEY, 0) - VALUE(L1.OUTGO_MONEY, 0) AS ZAN_GK ";
        $query .= " FROM ";
        $query .= "     INCOME_DATA T1 ";
        $query .= "     LEFT JOIN OUTGO_DATA L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     VALUE(T1.INCOME_MONEY, 0) - VALUE(L1.OUTGO_MONEY, 0) >= 0 ";
        //入力中の伝票の支出伺の金額を取得
        $query .= " ), HENKIN_USE_DATA_NOZOKU AS ( ";
        $query .= " SELECT ";
        $query .= "     L1.SCHREGNO, ";
        $query .= "     SUM(L1.OUTGO_MONEY) AS OUTGO_MONEY ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT T1 ";
        $query .= "     INNER JOIN LEVY_REQUEST_OUTGO_SCHREG_DAT L1 ON L1.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                                                AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                                                AND L1.YEAR        = T1.YEAR ";
        $query .= "                                                AND L1.OUTGO_L_CD  = T1.OUTGO_L_CD ";
        $query .= "                                                AND L1.OUTGO_M_CD  = T1.OUTGO_M_CD ";
        $query .= "                                                AND L1.REQUEST_NO  = T1.REQUEST_NO ";
        $query .= "     INNER JOIN LEVY_REQUEST_OUTGO_MEISAI_DAT L2 ON L2.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                                                AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                                                AND L2.YEAR        = T1.YEAR ";
        $query .= "                                                AND L2.OUTGO_L_CD  = T1.OUTGO_L_CD ";
        $query .= "                                                AND L2.OUTGO_M_CD  = T1.OUTGO_M_CD ";
        $query .= "                                                AND L2.REQUEST_NO  = T1.REQUEST_NO ";
        $query .= "                                                AND L2.OUTGO_S_CD  = L1.OUTGO_S_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND T1.YEAR        = '{$model->year}' ";
        $query .= " AND T1.OUTGO_CANCEL IS NULL ";
        $query .= " AND (T1.OUTGO_L_CD || T1.OUTGO_M_CD = '{$model->getOutgoLMcd}' ";
        $query .= "      AND L2.REQUEST_NO  = '{$model->getRequestNo}' ";
        $query .= "      AND L2.OUTGO_S_CD = '".$outgoscd."'  ";
        $query .= "      )  ";
        $query .= " GROUP BY ";
        $query .= "     L1.SCHREGNO ";
        //収入残額 + 入力中の金額 = 残額 (生徒ごと)
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ZAN_GK + VALUE(L2.OUTGO_MONEY, 0) AS ZAN_GK ";
        $query .= " FROM ";
        $query .= "     TAISHOU_DATA T1  ";
        $query .= "     LEFT JOIN HENKIN_USE_DATA_NOZOKU L2 ON L2.SCHREGNO = T1.SCHREGNO ";

        return $query;
    }

    //データチェック用2
    //返金用COLLECT_MONEY_REPAY_*_DATの返金可能な金額を取得
    function getSchnoCollectMoneyData($model) {
        //対象生徒
        $schregMoneyData = array();
        $schregMoneyData = explode(",", $model->selectStudent);
        //入金項目または細目
        $collectlcd = substr($model->getCollectLMSGrpcd, 0, 2);
        $collectmcd = substr($model->getCollectLMSGrpcd, 2, 2);
        $collectscd = substr($model->getCollectLMSGrpcd, 4, 2);
        $collectgrpcd = substr($model->getCollectLMSGrpcd, 6, 4);
        //支出細目をセット
        $outgoscd = substr($model->field["OUTGO_L_M_S_CD"], 4, 2);
        //対象テーブルセット
        if ($collectscd == '00') {
            $dueTablename = "COLLECT_MONEY_DUE_M_DAT";
            $paidTablename = "COLLECT_MONEY_PAID_M_DAT";
            $repayTablename = "COLLECT_MONEY_REPAY_M_DAT";
        } else {
            $dueTablename = "COLLECT_MONEY_DUE_S_DAT";
            $paidTablename = "COLLECT_MONEY_PAID_S_DAT";
            $repayTablename = "COLLECT_MONEY_REPAY_S_DAT";
        }
        //入金している金額を取得
        $query  = " WITH PAID_TOTAL AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(L1.PAID_MONEY) AS PAID_MONEY_TOTAL ";
        $query .= " FROM ";
        $query .= "     $dueTablename T1 ";
        $query .= "     INNER JOIN $paidTablename L1 ON L1.SCHOOLCD       = T1.SCHOOLCD ";
        $query .= "                                 AND L1.SCHOOL_KIND    = T1.SCHOOL_KIND ";        
        $query .= "                                 AND L1.YEAR           = T1.YEAR ";        
        $query .= "                                 AND L1.SCHREGNO       = T1.SCHREGNO ";        
        $query .= "                                 AND L1.COLLECT_GRP_CD = T1.COLLECT_GRP_CD  ";
        $query .= "                                 AND L1.COLLECT_L_CD   = T1.COLLECT_L_CD  ";
        $query .= "                                 AND L1.COLLECT_M_CD   = T1.COLLECT_M_CD  ";
        if ($dueTablename == "COLLECT_MONEY_DUE_S_DAT") {
            $query .= "                                AND L1.COLLECT_S_CD = T1.COLLECT_S_CD  ";
        }
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= "     AND T1.YEAR        = '".$model->year."' ";
        $query .= "     AND T1.COLLECT_GRP_CD   = '".$collectgrpcd."' ";
        $query .= "     AND T1.COLLECT_L_CD     = '".$collectlcd."' ";
        $query .= "     AND T1.COLLECT_M_CD     = '".$collectmcd."' ";
        if ($dueTablename == "COLLECT_MONEY_DUE_S_DAT") {
            $query .= "     AND T1.COLLECT_S_CD     = '".$collectscd."' ";
        }
        //選択した生徒のみをチェック
        $query .= " AND T1.SCHREGNO IN ( ";
        $count = 0;
        $conma = "";
        foreach ($schregMoneyData as $schregMoneyvalue) {
            list($schregno, $money) = explode("-", $schregMoneyvalue);
            $query .= " $conma'".$schregno."' ";
            $conma = ",";
            $count++;
        }
        $query .= " ) ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";
        //返金している金額を取得
        $query .= " ), REPAY_TOTAL AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(L1.REPAY_MONEY) AS REPAY_MONEY_TOTAL ";
        $query .= " FROM ";
        $query .= "     $dueTablename T1 ";
        $query .= "     INNER JOIN $repayTablename L1 ON L1.SCHOOLCD       = T1.SCHOOLCD ";
        $query .= "                                  AND L1.SCHOOL_KIND    = T1.SCHOOL_KIND ";        
        $query .= "                                  AND L1.YEAR           = T1.YEAR ";        
        $query .= "                                  AND L1.SCHREGNO       = T1.SCHREGNO ";        
        $query .= "                                  AND L1.COLLECT_GRP_CD = T1.COLLECT_GRP_CD  ";
        $query .= "                                  AND L1.COLLECT_L_CD   = T1.COLLECT_L_CD  ";
        $query .= "                                  AND L1.COLLECT_M_CD   = T1.COLLECT_M_CD  ";
        if ($dueTablename == "COLLECT_MONEY_DUE_S_DAT") {
            $query .= "                                AND L1.COLLECT_S_CD = T1.COLLECT_S_CD  ";
        }
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= "     AND T1.YEAR        = '".$model->year."' ";
        $query .= "     AND T1.COLLECT_GRP_CD   = '".$collectgrpcd."' ";
        $query .= "     AND T1.COLLECT_L_CD     = '".$collectlcd."' ";
        $query .= "     AND T1.COLLECT_M_CD     = '".$collectmcd."' ";
        if ($dueTablename == "COLLECT_MONEY_DUE_S_DAT") {
            $query .= "     AND T1.COLLECT_S_CD     = '".$collectscd."' ";
        }
        //選択した生徒のみをチェック
        $query .= " AND T1.SCHREGNO IN ( ";
        $count = 0;
        $conma = "";
        foreach ($schregMoneyData as $schregMoneyvalue) {
            list($schregno, $money) = explode("-", $schregMoneyvalue);
            $query .= " $conma'".$schregno."' ";
            $conma = ",";
            $count++;
        }
        $query .= " ) ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";
        //作成した返金用の支出伺の金額を取得
        $query .= " ), ALL_LEVY_HENKIN_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     L1.SCHREGNO, ";
        $query .= "     SUM(L1.OUTGO_MONEY) AS OUTGO_MONEY ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT T1 ";
        $query .= "     INNER JOIN LEVY_REQUEST_OUTGO_SCHREG_DAT L1 ON L1.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                                                AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";        
        $query .= "                                                AND L1.YEAR        = T1.YEAR ";        
        $query .= "                                                AND L1.OUTGO_L_CD  = T1.OUTGO_L_CD ";
        $query .= "                                                AND L1.OUTGO_M_CD  = T1.OUTGO_M_CD ";
        $query .= "                                                AND L1.REQUEST_NO  = T1.REQUEST_NO ";
        $query .= "     INNER JOIN LEVY_REQUEST_OUTGO_MEISAI_DAT L2 ON L2.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                                                AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ";        
        $query .= "                                                AND L2.YEAR        = T1.YEAR ";        
        $query .= "                                                AND L2.OUTGO_L_CD  = T1.OUTGO_L_CD ";
        $query .= "                                                AND L2.OUTGO_M_CD  = T1.OUTGO_M_CD ";
        $query .= "                                                AND L2.REQUEST_NO  = T1.REQUEST_NO ";
        $query .= "                                                AND L2.OUTGO_S_CD  = L1.OUTGO_S_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND T1.YEAR        = '{$model->year}' ";
        $query .= " AND T1.OUTGO_CANCEL IS NULL ";
        $query .= " AND T1.COLLECT_GRP_CD = '".$collectgrpcd."' ";
        $query .= " AND T1.COLLECT_L_CD = '".$collectlcd."' ";
        $query .= " AND T1.COLLECT_M_CD = '".$collectmcd."' ";
        $query .= " AND T1.COLLECT_S_CD = '".$collectscd."' ";
        //選択した生徒のみをチェック
        $query .= " AND L1.SCHREGNO IN ( ";
        $count = 0;
        $conma = "";
        foreach ($schregMoneyData as $schregMoneyvalue) {
            list($schregno, $money) = explode("-", $schregMoneyvalue);
            $query .= " $conma'".$schregno."' ";
            $conma = ",";
            $count++;
        }
        $query .= " ) ";
        $query .= " GROUP BY ";
        $query .= "     L1.SCHREGNO ";
        //入力中の伝票の支出伺の金額を取得
        $query .= " ), SAKUSEI_LEVY_HENKIN_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     L1.SCHREGNO, ";
        $query .= "     SUM(L1.OUTGO_MONEY) AS OUTGO_MONEY ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT T1 ";
        $query .= "     INNER JOIN LEVY_REQUEST_OUTGO_SCHREG_DAT L1 ON L1.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                                                AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";        
        $query .= "                                                AND L1.YEAR        = T1.YEAR ";        
        $query .= "                                                AND L1.OUTGO_L_CD  = T1.OUTGO_L_CD ";
        $query .= "                                                AND L1.OUTGO_M_CD  = T1.OUTGO_M_CD ";
        $query .= "                                                AND L1.REQUEST_NO  = T1.REQUEST_NO ";
        $query .= "     INNER JOIN LEVY_REQUEST_OUTGO_MEISAI_DAT L2 ON L2.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                                                AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ";        
        $query .= "                                                AND L2.YEAR        = T1.YEAR ";        
        $query .= "                                                AND L2.OUTGO_L_CD  = T1.OUTGO_L_CD ";
        $query .= "                                                AND L2.OUTGO_M_CD  = T1.OUTGO_M_CD ";
        $query .= "                                                AND L2.REQUEST_NO  = T1.REQUEST_NO ";
        $query .= "                                                AND L2.OUTGO_S_CD  = L1.OUTGO_S_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND T1.YEAR = '{$model->year}' ";
        $query .= " AND T1.OUTGO_CANCEL IS NULL ";
        $query .= " AND (T1.OUTGO_L_CD || T1.OUTGO_M_CD = '{$model->getOutgoLMcd}' ";
        $query .= "      AND L2.REQUEST_NO = '{$model->getRequestNo}' ";
        $query .= "      AND L2.OUTGO_S_CD = '".$outgoscd."'  ";
        $query .= "      AND T1.COLLECT_GRP_CD = '".$collectgrpcd."' ";
        $query .= "      AND T1.COLLECT_L_CD = '".$collectlcd."' ";
        $query .= "      AND T1.COLLECT_M_CD = '".$collectmcd."' ";
        $query .= "      AND T1.COLLECT_S_CD = '".$collectscd."' ";
        $query .= "      )  ";
        $query .= " GROUP BY ";
        $query .= "     L1.SCHREGNO ";
        $query .= " ) ";
        //返金可能額
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     VALUE(T1.PAID_MONEY_TOTAL, 0) - VALUE(L1.REPAY_MONEY_TOTAL, 0) - VALUE(L2.OUTGO_MONEY, 0) + VALUE(L3.OUTGO_MONEY, 0) AS HENKIN_KANOU_GK ";
        $query .= " FROM ";
        $query .= "     PAID_TOTAL T1";
        $query .= "     LEFT JOIN REPAY_TOTAL L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN ALL_LEVY_HENKIN_DATA L2 ON L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SAKUSEI_LEVY_HENKIN_DATA L3 ON L3.SCHREGNO = T1.SCHREGNO ";

        return $query;
    }

    //明細データの有無確認
    function getMeisaiData($model, $outgoscd, $flg="") {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_MEISAI_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= "     AND T1.YEAR        = '{$model->year}' ";
        $query .= "     AND T1.OUTGO_L_CD || T1.OUTGO_M_CD = '{$model->getOutgoLMcd}' ";
        $query .= "     AND T1.REQUEST_NO  = '{$model->getRequestNo}' ";
        $query .= "     AND T1.OUTGO_S_CD  = '".$outgoscd."' ";
        if ($flg) {
            $query .= "     AND T1.LINE_NO <> ".$model->getLineNo." ";
        }
        return $query;
    }
    
    //UPDATE
    function &getUpdateQuery($db, $model) {
        $db->autoCommit(false);
        
        //対象生徒
        $schregSelectData = array();
        $schregSelectData = explode(",", $model->selectStudent);

        $outgolcd = substr($model->getOutgoLMcd, 0, 2);
        $outgomcd = substr($model->getOutgoLMcd, 2, 2);
        $outgoscd = substr($model->field["OUTGO_L_M_S_CD"], 4, 2);
        
        $countMeisai = $db->getOne(knjp910_schregQuery::getMeisaiData($model, $outgoscd, ""));
        
        //明細データをセット(Delete Insert)
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_MEISAI_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND YEAR        = '{$model->year}' ";
        $query .= " AND OUTGO_L_CD  = '".$outgolcd."' ";
        $query .= " AND OUTGO_M_CD  = '".$outgomcd."' ";
        $query .= " AND REQUEST_NO  = '{$model->getRequestNo}' ";
        //$query .= " AND OUTGO_S_CD = '".$outgoscd."' ";
        $query .= " AND LINE_NO     = ".$model->getLineNo." ";
        $db->query($query);
        
        $data = array();
        $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]      = $model->getSchoolKind;
        $data["YEAR"][TEXT]             = $model->year;
        $data["OUTGO_L_CD"][TEXT]       = $outgolcd;
        $data["OUTGO_M_CD"][TEXT]       = $outgomcd;
        $data["REQUEST_NO"][TEXT]       = $model->getRequestNo;
        $data["OUTGO_S_CD"][TEXT]       = $outgoscd;
        $data["LINE_NO"][NUMBER]                    = $model->getLineNo;
        $data["COMMODITY_PRICE"][NUMBER]            = $model->field["COMMODITY_PRICE"];
        if ($model->field["WARIHURI_DIV"] == "1") {
            if ($model->selectStudent) {
                $data["COMMODITY_CNT"][NUMBER]          = get_count($schregSelectData);
                $data["TOTAL_PRICE_ZEINUKI"][NUMBER]    = get_count($schregSelectData) * $model->field["COMMODITY_PRICE"];
                if (!$model->getHenkinFlg) {
                    $data["TOTAL_TAX"][NUMBER]              = floor((get_count($schregSelectData) * $model->field["COMMODITY_PRICE"]) * 0.08);
                    $data["TOTAL_PRICE"][NUMBER]            = floor((get_count($schregSelectData) * $model->field["COMMODITY_PRICE"]) * 1.08);
                } else {
                    $data["TOTAL_TAX"][NUMBER]              = 0;
                    $data["TOTAL_PRICE"][NUMBER]            = get_count($schregSelectData) * $model->field["COMMODITY_PRICE"];
                }
            } else {
                $data["COMMODITY_CNT"][NUMBER]          = 0;
                $data["TOTAL_PRICE_ZEINUKI"][NUMBER]    = 0;
                $data["TOTAL_TAX"][NUMBER]              = 0;
                $data["TOTAL_PRICE"][NUMBER]            = 0;
            }
        } else {
            $data["COMMODITY_CNT"][NUMBER]          = $model->field["COMMODITY_CNT"];
            $data["TOTAL_PRICE_ZEINUKI"][NUMBER]    = $model->field["COMMODITY_CNT"] * $model->field["COMMODITY_PRICE"];
            $data["TOTAL_TAX"][NUMBER]              = floor(($model->field["COMMODITY_CNT"] * $model->field["COMMODITY_PRICE"]) * 0.08);
            $data["TOTAL_PRICE"][NUMBER]            = floor(($model->field["COMMODITY_CNT"] * $model->field["COMMODITY_PRICE"]) * 1.08);
            $data["TRADER_SEIKYU_NO"][TEXT]         = $model->field["TRADER_SEIKYU_NO"];
            $data["SEIKYU_MONTH"][TEXT]             = $model->field["SEIKYU_MONTH"];
        }
        $data["WARIHURI_DIV"][TEXT]         = $model->field["WARIHURI_DIV"];
        $data["REMARK"][TEXT]               = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";
        $query = Query::insertSQL($data, "LEVY_REQUEST_OUTGO_MEISAI_DAT");
        $db->query($query);
        
        //支出生徒割り振りデータをセット(Delete Insert)
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_SCHREG_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= "     AND T1.YEAR        = '{$model->year}' ";
        $query .= "     AND T1.OUTGO_L_CD || T1.OUTGO_M_CD = '{$model->getOutgoLMcd}' ";
        $query .= "     AND T1.REQUEST_NO  = '{$model->getRequestNo}' ";
        //$query .= "     AND T1.OUTGO_S_CD = '".$outgoscd."' ";
        $query .= "     AND T1.LINE_NO     = ".$model->getLineNo." ";
        $db->query($query);
        //生徒データをInsert
        if ($model->selectStudent) {
            foreach ($schregSelectData as $schregNo) {
                //金額上限チェック
                $data = array();
                $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
                $data["SCHOOL_KIND"][TEXT]      = $model->getSchoolKind;
                $data["LINE_NO"][NUMBER]        = $model->getLineNo;
                $data["YEAR"][TEXT]             = $model->year;
                $data["OUTGO_L_CD"][TEXT]       = $outgolcd;
                $data["OUTGO_M_CD"][TEXT]       = $outgomcd;
                $data["REQUEST_NO"][TEXT]       = $model->getRequestNo;
                $data["OUTGO_S_CD"][TEXT]       = $outgoscd;
                $data["LINE_NO"][NUMBER]        = $model->getLineNo;
                $data["SCHREGNO"][TEXT]         = $schregNo;
                $data["OUTGO_MONEY"][NUMBER]    = $model->field["COMMODITY_PRICE"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";
                $query = Query::insertSQL($data, "LEVY_REQUEST_OUTGO_SCHREG_DAT");
                $db->query($query);
            }
        }
        
        $db->commit();
        return;
    }
    
    //DELETE
    function &getDeleteQuery($db, $model) {
        $db->autoCommit(false);

        $outgoscd = substr($model->field["OUTGO_L_M_S_CD"], 4, 2);

        //MEISAI
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_MEISAI_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND YEAR        = '{$model->year}' ";
        $query .= " AND OUTGO_L_CD  = '{$model->getOutgoLcd}' ";
        $query .= " AND OUTGO_M_CD  = '{$model->getOutgoMcd}' ";
        $query .= " AND REQUEST_NO  = '{$model->getRequestNo}' ";
        //$query .= " AND OUTGO_S_CD = '".$outgoscd."' ";
        $query .= " AND LINE_NO     = ".$model->getLineNo." ";
        $db->query($query);

        //SCHREG
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_SCHREG_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND YEAR        = '{$model->year}' ";
        $query .= " AND OUTGO_L_CD  = '{$model->getOutgoLcd}' ";
        $query .= " AND OUTGO_M_CD  = '{$model->getOutgoMcd}' ";
        $query .= " AND REQUEST_NO  = '{$model->getRequestNo}' ";
        //$query .= " AND OUTGO_S_CD = '".$outgoscd."' ";
        $query .= " AND LINE_NO     = ".$model->getLineNo." ";
        $db->query($query);

        $db->commit();
        return;
    }
}
?>

