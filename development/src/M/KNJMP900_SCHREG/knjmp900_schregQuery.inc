<?php

require_once('for_php7.php');


class knjmp900_schregQuery extends Query {

    //本締めデータチェック
    function getCloseFlgData($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     LEVY_CLOSE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " AND CLOSE_FLG = '1' ";

        return $query;
    }

    //決済の状況を取得(比較時に利用)
    function getLevyData($model, $flg) {
        $query  = "";
        $query .= " SELECT ";
        if ($flg == "APPROVAL") {
            $query .= "     T1.INCOME_APPROVAL ";
        } else {
            $query .= "     T1.INCOME_CANCEL ";
        }
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.INCOME_L_CD = '{$model->getIncomeLcd}' ";
        $query .= "     AND T1.INCOME_L_CD || T1.INCOME_M_CD = '{$model->getIncomeLMcd}' ";
        $query .= "     AND T1.REQUEST_NO = '{$model->getRequestNo}' ";
        return $query;
    }

    //一覧取得
    function getRow($model) {
    
        $db = Query::dbCheckOut();
    
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_MEISAI_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.INCOME_L_CD = '{$model->getIncomeLcd}' ";
        $query .= "     AND T1.INCOME_L_CD || T1.INCOME_M_CD = '{$model->getIncomeLMcd}' ";
        $query .= "     AND T1.REQUEST_NO = '{$model->getRequestNo}' ";
        $query .= "     AND T1.INCOME_S_CD = '".sprintf("%02d", $model->getLineNo)."' ";
        //$query .= "     AND T1.LINE_NO = ".$model->getLineNo." ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.INCOME_L_CD, ";
        $query .= "     T1.INCOME_M_CD, ";
        $query .= "     T1.REQUEST_NO ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //収入科目
    function getLevyMDiv($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD || ':' || T1.LEVY_M_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_M_MST T1 ";
        $query .= "     INNER JOIN LEVY_L_MST L1 ON L1.LEVY_L_CD = T1.LEVY_L_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= " AND T1.LEVY_L_CD || T1.LEVY_M_CD = '{$model->getIncomeLMcd}' ";

        return $query;
    }

    //生徒取得
    function getSchno($model, $flg="") {
        $collectlcd = substr($model->getCollectLMScd, 0, 2);
        $collectmcd = substr($model->getCollectLMScd, 2, 2);
        $collectscd = '00';
        
        //入金項目M(生徒ごと)
        $query  = " WITH MONEY_PAID_M_SCHREG_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(VALUE(L1.PAID_MONEY, 0)) AS PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_MONEY_DUE_M_DAT T1 ";
        $query .= "     LEFT JOIN COLLECT_MONEY_PAID_M_DAT L1 ON L1.YEAR = T1.YEAR  ";
        $query .= "                                          AND L1.COLLECT_L_CD = T1.COLLECT_L_CD  ";
        $query .= "                                          AND L1.COLLECT_M_CD = T1.COLLECT_M_CD  ";
        $query .= "                                          AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE  ";
        $query .= "     T1.YEAR = '{$model->year}'  ";
        $query .= " AND T1.COLLECT_L_CD = '{$collectlcd}' ";
        $query .= " AND T1.COLLECT_M_CD = '{$collectmcd}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";
        //入金明細S(生徒ごと)
        $query .= " ), MONEY_PAID_S_SCHREG_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(VALUE(L2.PAID_MONEY, 0)) AS PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_MONEY_DUE_S_DAT T1 ";
        $query .= "     LEFT JOIN COLLECT_MONEY_PAID_S_DAT L2 ON L2.YEAR = T1.YEAR  ";
        $query .= "                                          AND L2.COLLECT_L_CD = T1.COLLECT_L_CD  ";
        $query .= "                                          AND L2.COLLECT_M_CD = T1.COLLECT_M_CD  ";
        $query .= "                                          AND L2.COLLECT_S_CD = T1.COLLECT_S_CD  ";
        $query .= "                                          AND L2.SCHREGNO = T1.SCHREGNO ";        
        $query .= " WHERE  ";
        $query .= "     T1.YEAR = '{$model->year}'  ";
        $query .= " AND T1.COLLECT_L_CD = '{$collectlcd}' ";
        $query .= " AND T1.COLLECT_M_CD = '{$collectmcd}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";
        //入金額合計(生徒ごと)
        $query .= " ), MONEY_SCHREG_DATA AS ( ";
        //項目Mデータのみ
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     VALUE(T1.PAID_MONEY, 0) AS PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     MONEY_PAID_M_SCHREG_DATA T1 ";
        $query .= "     LEFT JOIN MONEY_PAID_S_SCHREG_DATA L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE  ";
        $query .= "     L1.SCHREGNO IS NULL  ";
        //明細Sデータのみ
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     VALUE(T1.PAID_MONEY, 0) AS PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     MONEY_PAID_S_SCHREG_DATA T1 ";
        $query .= "     LEFT JOIN MONEY_PAID_M_SCHREG_DATA L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE  ";
        $query .= "     L1.SCHREGNO IS NULL  ";
        //両方に持つ場合
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     VALUE(T1.PAID_MONEY, 0) + VALUE(L1.PAID_MONEY, 0) AS PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     MONEY_PAID_M_SCHREG_DATA T1 ";
        $query .= "     LEFT JOIN MONEY_PAID_S_SCHREG_DATA L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE  ";
        $query .= "     L1.SCHREGNO IS NOT NULL  ";
        //使用した収入額チェック
        $query .= " ), INCOME_USE_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     L1.SCHREGNO, ";
        $query .= "     SUM(L1.INCOME_MONEY) AS INCOME_MONEY ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT T1 ";
        $query .= "     INNER JOIN LEVY_REQUEST_INCOME_SCHREG_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                                 AND L1.INCOME_L_CD = T1.INCOME_L_CD ";
        $query .= "                                                 AND L1.INCOME_M_CD = T1.INCOME_M_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= " AND T1.COLLECT_L_CD = '{$collectlcd}' ";
        $query .= " AND T1.COLLECT_M_CD = '{$collectmcd}' ";
        $query .= " AND T1.COLLECT_S_CD = '{$collectscd}' ";
        $query .= " AND T1.INCOME_CANCEL IS NULL ";
        $query .= " GROUP BY ";
        $query .= "     L1.SCHREGNO ";
        //入金額 - 使用する収入額 = 残額 (生徒ごと)
        $query .= " ), INCOME_ZAN_GK_SCHREG_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.PAID_MONEY - VALUE(L1.INCOME_MONEY, 0) AS ZAN_GK ";
        $query .= " FROM ";
        $query .= "     MONEY_SCHREG_DATA T1  ";
        $query .= "     LEFT JOIN INCOME_USE_DATA L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        //選択した伝票番号のLINE_NOの生徒データのみを取得(左側)
        $query .= " ), SCHREG_SELECT_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_SCHREG_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= " AND T1.INCOME_L_CD = '{$model->getIncomeLcd}' ";
        $query .= " AND T1.INCOME_L_CD || T1.INCOME_M_CD = '{$model->getIncomeLMcd}' ";
        $query .= " AND T1.REQUEST_NO = '{$model->getRequestNo}' ";
        $query .= " AND T1.INCOME_S_CD = '".sprintf("%02d", $model->getLineNo)."' ";
        $query .= " ) ";

        $query .= " SELECT  ";
        $query .= "     T1.SCHREGNO || '-' || CHAR(L1.ZAN_GK) AS VALUE,  ";
        //年組番表示
        if ($model->field["HR_CLASS_HYOUJI_FLG"] === '1') {
            $query .= "     SCH_RH.HR_NAME || T1.ATTENDNO || '番' || '　' || T2.NAME || '( 残額:' || CHAR(L1.ZAN_GK) || ')' AS LABEL  ";
        } else {
            $query .= "     T1.SCHREGNO || '　' || T2.NAME || '( 残額:' || CHAR(L1.ZAN_GK) || ')' AS LABEL  ";
        }
        $query .= " FROM  ";
        $query .= "     SCHREG_REGD_DAT T1  ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT SCH_RH ON T1.YEAR = SCH_RH.YEAR ";
        $query .= "                                      AND T1.SEMESTER = SCH_RH.SEMESTER ";
        $query .= "                                      AND T1.GRADE = SCH_RH.GRADE ";
        $query .= "                                      AND T1.HR_CLASS = SCH_RH.HR_CLASS ";
        $query .= "    ,SCHREG_BASE_MST T2 ";
        $query .= "     INNER JOIN INCOME_ZAN_GK_SCHREG_DATA L1 ON L1.SCHREGNO = T2.SCHREGNO ";
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

    //明細データの有無確認
    function getMeisaiData($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_MEISAI_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.INCOME_L_CD || T1.INCOME_M_CD = '{$model->getIncomeLMcd}' ";
        $query .= "     AND T1.REQUEST_NO  = '{$model->getRequestNo}' ";
        $query .= "     AND T1.INCOME_S_CD = '".sprintf("%02d", $model->getLineNo)."' ";
        //$query .= "     AND T1.LINE_NO     = ".$model->getLineNo." ";

        return $query;
    }
    
    //入金額の残額を取得 (ただし、入力中の伝票+NOは除く)
    function getSchnoMoneyData($model) {
        //対象生徒
        $schregMoneyData = array();
        $schregMoneyData = explode(",", $model->selectStudent);
        //入金科目をセット
        $collectlcd = substr($model->getCollectLMScd, 0, 2);
        $collectmcd = substr($model->getCollectLMScd, 2, 2);
        $collectscd = '00';
        
        //入金項目M(生徒ごと)
        $query  = " WITH MONEY_PAID_M_SCHREG_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(VALUE(L1.PAID_MONEY, 0)) AS PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_MONEY_DUE_M_DAT T1 ";
        $query .= "     LEFT JOIN COLLECT_MONEY_PAID_M_DAT L1 ON L1.YEAR = T1.YEAR  ";
        $query .= "                                          AND L1.COLLECT_L_CD = T1.COLLECT_L_CD  ";
        $query .= "                                          AND L1.COLLECT_M_CD = T1.COLLECT_M_CD  ";
        $query .= "                                          AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE  ";
        $query .= "     T1.YEAR = '{$model->year}'  ";
        $query .= " AND T1.COLLECT_L_CD = '{$collectlcd}' ";
        $query .= " AND T1.COLLECT_M_CD = '{$collectmcd}' ";
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
        //入金明細S(生徒ごと)
        $query .= " ), MONEY_PAID_S_SCHREG_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(VALUE(L2.PAID_MONEY, 0)) AS PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_MONEY_DUE_S_DAT T1 ";
        $query .= "     LEFT JOIN COLLECT_MONEY_PAID_S_DAT L2 ON L2.YEAR = T1.YEAR  ";
        $query .= "                                          AND L2.COLLECT_L_CD = T1.COLLECT_L_CD  ";
        $query .= "                                          AND L2.COLLECT_M_CD = T1.COLLECT_M_CD  ";
        $query .= "                                          AND L2.COLLECT_S_CD = T1.COLLECT_S_CD  ";
        $query .= "                                          AND L2.SCHREGNO = T1.SCHREGNO ";        
        $query .= " WHERE  ";
        $query .= "     T1.YEAR = '{$model->year}'  ";
        $query .= " AND T1.COLLECT_L_CD = '{$collectlcd}' ";
        $query .= " AND T1.COLLECT_M_CD = '{$collectmcd}' ";
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
        //入金額合計(生徒ごと)
        $query .= " ), MONEY_SCHREG_DATA AS ( ";
        //項目Mデータのみ
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     VALUE(T1.PAID_MONEY, 0) AS PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     MONEY_PAID_M_SCHREG_DATA T1 ";
        $query .= "     LEFT JOIN MONEY_PAID_S_SCHREG_DATA L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE  ";
        $query .= "     L1.SCHREGNO IS NULL  ";
        //明細Sデータのみ
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     VALUE(T1.PAID_MONEY, 0) AS PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     MONEY_PAID_S_SCHREG_DATA T1 ";
        $query .= "     LEFT JOIN MONEY_PAID_M_SCHREG_DATA L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE  ";
        $query .= "     L1.SCHREGNO IS NULL  ";
        //両方に持つ場合
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     VALUE(T1.PAID_MONEY, 0) + VALUE(L1.PAID_MONEY, 0) AS PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     MONEY_PAID_M_SCHREG_DATA T1 ";
        $query .= "     LEFT JOIN MONEY_PAID_S_SCHREG_DATA L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE  ";
        $query .= "     L1.SCHREGNO IS NOT NULL  ";
        
        //使用した収入額チェック
        $query .= " ), INCOME_USE_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     L1.SCHREGNO, ";
        $query .= "     SUM(L1.INCOME_MONEY) AS INCOME_MONEY ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT T1 ";
        $query .= "     INNER JOIN LEVY_REQUEST_INCOME_SCHREG_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                                 AND L1.INCOME_L_CD = T1.INCOME_L_CD ";
        $query .= "                                                 AND L1.INCOME_M_CD = T1.INCOME_M_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= " AND T1.COLLECT_L_CD = '{$collectlcd}' ";
        $query .= " AND T1.COLLECT_M_CD = '{$collectmcd}' ";
        $query .= " AND T1.COLLECT_S_CD = '{$collectscd}' ";
        $query .= " AND T1.INCOME_CANCEL IS NULL ";
        $query .= " GROUP BY ";
        $query .= "     L1.SCHREGNO ";
        //入力中の伝票の金額
        $query .= " ), INCOME_USE_DATA_NOZOKU AS ( ";
        $query .= " SELECT ";
        $query .= "     L1.SCHREGNO, ";
        $query .= "     SUM(L1.INCOME_MONEY) AS INCOME_MONEY ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT T1 ";
        $query .= "     INNER JOIN LEVY_REQUEST_INCOME_SCHREG_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                                 AND L1.INCOME_L_CD = T1.INCOME_L_CD ";
        $query .= "                                                 AND L1.INCOME_M_CD = T1.INCOME_M_CD ";
        $query .= "     INNER JOIN LEVY_REQUEST_INCOME_MEISAI_DAT L2 ON L2.YEAR = T1.YEAR ";
        $query .= "                                                 AND L2.INCOME_L_CD = T1.INCOME_L_CD ";
        $query .= "                                                 AND L2.INCOME_M_CD = T1.INCOME_M_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= " AND T1.COLLECT_L_CD = '{$collectlcd}' ";
        $query .= " AND T1.COLLECT_M_CD = '{$collectmcd}' ";
        $query .= " AND T1.COLLECT_S_CD = '{$collectscd}' ";
        $query .= " AND T1.INCOME_CANCEL IS NULL ";
        //入力中の伝票を取得
        $query .= " AND (T1.INCOME_L_CD || T1.INCOME_M_CD = '{$model->getIncomeLMcd}' ";
        $query .= "      AND L2.REQUEST_NO  = '{$model->getRequestNo}' ";
        $query .= "      AND L2.INCOME_S_CD = '".sprintf("%02d", $model->getLineNo)."'  ";
        $query .= "      )  ";
        $query .= " GROUP BY ";
        $query .= "     L1.SCHREGNO ";
        
        //入金額 - 使用している収入額 + 入力中の金額 = 残額 (生徒ごと)
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.PAID_MONEY - VALUE(L1.INCOME_MONEY, 0) + VALUE(L2.INCOME_MONEY, 0) AS ZAN_GK ";
        $query .= " FROM ";
        $query .= "     MONEY_SCHREG_DATA T1  ";
        $query .= "     LEFT JOIN INCOME_USE_DATA L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN INCOME_USE_DATA_NOZOKU L2 ON L2.SCHREGNO = T1.SCHREGNO ";

        return $query;
    }


    //UPDATE
    function &getUpdateQuery($db, $model) {
        $db->autoCommit(false);
        
        //対象生徒
        $schregMoneyData = array();
        $schregMoneyData = explode(",", $model->selectStudent);

        $incomelcd = substr($model->getIncomeLMcd, 0, 2);
        $incomemcd = substr($model->getIncomeLMcd, 2, 2);
        
        $countMeisai = $db->getOne(knjmp900_schregQuery::getMeisaiData($model));
        //明細データをセット
        $data = array();
        $data["COMMODITY_NAME"][TEXT]       = $model->field["COMMODITY_NAME"];
        $data["LINE_NO"][NUMBER]            = $model->getLineNo;
        $data["COMMODITY_PRICE"][NUMBER]    = $model->field["COMMODITY_PRICE"];
        if ($model->field["WARIHURI_DIV"] == "1") {
            if ($model->selectStudent) {
                $data["COMMODITY_CNT"][NUMBER]      = get_count($schregMoneyData);
                $data["TOTAL_PRICE"][NUMBER]        = get_count($schregMoneyData) * $model->field["COMMODITY_PRICE"];
            } else {
                $data["COMMODITY_CNT"][NUMBER]      = 0;
                $data["TOTAL_PRICE"][NUMBER]        = 0 * $model->field["COMMODITY_PRICE"];
            }
        } else {
            $data["COMMODITY_CNT"][NUMBER]          = $model->field["COMMODITY_CNT"];
            $data["TOTAL_PRICE"][NUMBER]            = $model->field["COMMODITY_CNT"] * $model->field["COMMODITY_PRICE"];
        }
        $data["WARIHURI_DIV"][TEXT]         = $model->field["WARIHURI_DIV"];
        $data["REMARK"][TEXT]               = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";
        if ($countMeisai == 0) {
            $data["YEAR"][TEXT]                 = $model->year;
            $data["INCOME_L_CD"][TEXT]          = $incomelcd;
            $data["INCOME_M_CD"][TEXT]          = $incomemcd;
            $data["REQUEST_NO"][TEXT]           = $model->getRequestNo;
            $data["INCOME_S_CD"][TEXT]          = sprintf("%02d", $model->getLineNo);//LINE_NOと同一のものをいれておく
            $query = Query::insertSQL($data, "LEVY_REQUEST_INCOME_MEISAI_DAT");
            $db->query($query);
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR = '{$model->year}' ";
            $where .= " AND INCOME_L_CD || INCOME_M_CD = '{$model->getIncomeLMcd}' ";
            $where .= " AND REQUEST_NO  = '{$model->getRequestNo}' ";
            $where .= " AND INCOME_S_CD = '".sprintf("%02d", $model->getLineNo)."' ";
            $query = Query::updateSQL($data, "LEVY_REQUEST_INCOME_MEISAI_DAT", $where);
            $db->query($query);
        }
        
        //収入生徒割り振りデータをセット(Delete Insert)
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_INCOME_SCHREG_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.INCOME_L_CD || T1.INCOME_M_CD = '{$model->getIncomeLMcd}' ";
        $query .= "     AND T1.REQUEST_NO  = '{$model->getRequestNo}' ";
        $query .= "     AND T1.INCOME_S_CD = '".sprintf("%02d", $model->getLineNo)."' ";
        $db->query($query);
        //生徒データをInsert
        if ($model->selectStudent) {
            foreach ($schregMoneyData as $schregMoneyvalue) {
                list($schregno, $money) = explode("-", $schregMoneyvalue);

                $data = array();
                $data["LINE_NO"][NUMBER]        = $model->getLineNo;
                $data["YEAR"][TEXT]             = $model->year;
                $data["INCOME_L_CD"][TEXT]      = $incomelcd;
                $data["INCOME_M_CD"][TEXT]      = $incomemcd;
                $data["REQUEST_NO"][TEXT]       = $model->getRequestNo;
                $data["INCOME_S_CD"][TEXT]      = sprintf("%02d", $model->getLineNo);//LINE_NOと同一のものをいれておく
                $data["LINE_NO"][NUMBER]        = $model->getLineNo;
                $data["SCHREGNO"][TEXT]         = $schregno;
                $data["INCOME_MONEY"][NUMBER]   = $model->field["COMMODITY_PRICE"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";
                $query = Query::insertSQL($data, "LEVY_REQUEST_INCOME_SCHREG_DAT");
                $db->query($query);
            }
        }
        
        $db->commit();
        return;
    }
    
    //UPDATE
    function &getDeleteQuery($db, $model) {
        $db->autoCommit(false);

        //MEISAI
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_INCOME_MEISAI_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " AND INCOME_L_CD = '{$model->getIncomeLcd}' ";
        $query .= " AND INCOME_M_CD = '{$model->getIncomeMcd}' ";
        $query .= " AND REQUEST_NO = '{$model->getRequestNo}' ";
        $query .= " AND INCOME_S_CD = '".sprintf("%02d", $model->getLineNo)."' ";
        $db->query($query);

        //SCHREG
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_INCOME_SCHREG_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " AND INCOME_L_CD = '{$model->getIncomeLcd}' ";
        $query .= " AND INCOME_M_CD = '{$model->getIncomeMcd}' ";
        $query .= " AND REQUEST_NO = '{$model->getRequestNo}' ";
        $query .= " AND INCOME_S_CD = '".sprintf("%02d", $model->getLineNo)."' ";
        $db->query($query);

        $db->commit();
        return;
    }
}
?>

