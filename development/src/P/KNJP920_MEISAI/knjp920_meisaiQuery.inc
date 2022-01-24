<?php

require_once('for_php7.php');

class knjp920_meisaiQuery extends Query {

    //本締めデータチェック
    function getCloseFlgData($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     LEVY_CLOSE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND YEAR = '{$model->year}' ";
        $query .= " AND CLOSE_FLG = '1' ";

        return $query;
    }

    //支出伺の決済の状況を取得(比較時に利用)
    function getOutgoData($model, $flg) {
        $query  = "";
        $query .= " SELECT ";
        if ($flg == "APPROVAL") {
            $query .= "     T1.OUTGO_APPROVAL ";
        } else {
            $query .= "     T1.OUTGO_CANCEL ";
        }
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= "     AND T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.KOUNYU_NO = '{$model->getRequestNo}' ";
        return $query;
    }

    //一覧取得
    function getRow($model) {
    
        $db = Query::dbCheckOut();
    
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T1.KOUNYU_L_CD || T1.KOUNYU_M_CD || T1.KOUNYU_S_CD AS KOUNYU_L_M_S_CD ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_KOUNYU_MEISAI_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= "     AND T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.KOUNYU_L_CD = '{$model->getKounyuLcd}' ";
        $query .= "     AND T1.KOUNYU_L_CD || T1.KOUNYU_M_CD = '{$model->getKounyuLMcd}' ";
        $query .= "     AND T1.REQUEST_NO = '{$model->getRequestNo}' ";
        //$query .= "     AND T1.KOUNYU_S_CD = '".sprintf("%02d", $model->getLineNo)."' ";
        $query .= "     AND T1.LINE_NO = ".$model->getLineNo." ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //返金、給付対象、取得
    function getLevySmstInfo($model, $lmsCd) {
        $query  = " SELECT ";
        $query .= "     T1.REPAY_DIV, ";
        $query .= "     T1.BENEFIT ";
        $query .= " FROM ";
        $query .= "     LEVY_S_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= "     AND T1.YEAR        = '{$model->year}' ";
        $query .= "     AND T1.LEVY_L_CD || T1.LEVY_M_CD || T1.LEVY_S_CD = '{$lmsCd}' ";

        return $query;
    }

    //購入科目
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
        $query .= " AND T1.YEAR = '{$model->year}' ";
        $query .= " AND T1.LEVY_L_CD || T1.LEVY_M_CD = '{$model->getKounyuLMcd}' ";

        return $query;
    }

    //購入細目
    function getLevySDiv($model, $maxFlg="") {
        $query  = "";
        $query .= " SELECT ";
        if ($maxFlg != "") {
            $query .= "     max(LEVY_S_CD) ";
        } else {
            $query .= "     T1.LEVY_S_NAME AS NAME, ";
            $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD || T1.LEVY_S_CD || ':' || T1.LEVY_S_NAME AS LABEL, ";
            $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD || T1.LEVY_S_CD AS VALUE ";
        }
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
        $query .= " AND T1.LEVY_L_CD || T1.LEVY_M_CD = '{$model->getKounyuLMcd}' ";

        return $query;
    }

    //明細データの有無確認
    function getMeisaiData($model, $kounyuscd) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_KOUNYU_MEISAI_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= "     AND T1.YEAR        = '{$model->year}' ";
        $query .= "     AND T1.KOUNYU_L_CD || T1.KOUNYU_M_CD = '{$model->getKounyuLMcd}' ";
        $query .= "     AND T1.REQUEST_NO  = '{$model->getRequestNo}' ";
        $query .= "     AND T1.KOUNYU_S_CD = '{$kounyuscd}' ";
        $query .= "     AND T1.LINE_NO     =  {$model->getLineNo} ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($db, $model) {
        $db->autoCommit(false);

        $kounyulcd = substr($model->getKounyuLMcd, 0, 2);
        $kounyumcd = substr($model->getKounyuLMcd, 2, 2);
        $kounyuscd = substr($model->field["KOUNYU_L_M_S_CD"], 4, 2);

        $tax   = ($model->field["IN_TAX"] == "1") ? 0: 0.08;
        $inTax = ($model->field["IN_TAX"] == "1") ? 1: 1.08;
        $totalPriceNoTax = $model->field["COMMODITY_CNT"] * $model->field["COMMODITY_PRICE"];
        $totalPriceInTax = floor($totalPriceNoTax * $inTax);

        $countMeisai = $db->getOne(knjp920_meisaiQuery::getMeisaiData($model, $kounyuscd));
        //明細データをセット
        $data = array();
        $data["COMMODITY_PRICE"][NUMBER]        = $model->field["COMMODITY_PRICE"];
        $data["COMMODITY_CNT"][NUMBER]          = $model->field["COMMODITY_CNT"];
        $data["TOTAL_PRICE_ZEINUKI"][NUMBER]    = $totalPriceNoTax;
        $data["TOTAL_TAX"][NUMBER]              = floor($totalPriceNoTax * $tax);
        $data["TOTAL_PRICE"][NUMBER]            = $totalPriceInTax;
        $data["REMARK"][TEXT]                   = $model->field["REMARK"];
        $data["SCH_CNT"][NUMBER]                = $model->field["SCH_CNT"];
        $data["SCH_PRICE"][NUMBER]              = ceil($totalPriceInTax / $model->field["SCH_CNT"]);
        $data["HASUU"][NUMBER]                  = ceil($totalPriceInTax / $model->field["SCH_CNT"]) * $model->field["SCH_CNT"] - $totalPriceInTax;
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][NUMBER]                = "sysdate()";
        if ($countMeisai == 0) {
            $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]      = $model->getSchoolKind;
            $data["YEAR"][TEXT]             = $model->year;
            $data["REQUEST_NO"][TEXT]       = $model->getRequestNo;
            $data["LINE_NO"][NUMBER]        = $model->getLineNo;
            $data["KOUNYU_L_CD"][TEXT]      = $kounyulcd;
            $data["KOUNYU_M_CD"][TEXT]      = $kounyumcd;
            $data["KOUNYU_S_CD"][TEXT]      = $kounyuscd;
            $query = Query::insertSQL($data, "LEVY_REQUEST_KOUNYU_MEISAI_DAT");
            $db->query($query);
        } else {
            $where  = " WHERE ";
            $where .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
            $where .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
            $where .= " AND YEAR        = '{$model->year}' ";
            $where .= " AND KOUNYU_L_CD || KOUNYU_M_CD = '{$model->getKounyuLMcd}' ";
            $where .= " AND REQUEST_NO  = '{$model->getRequestNo}' ";
            $where .= " AND KOUNYU_S_CD = '{$kounyuscd}' ";
            $where .= " AND LINE_NO     =  {$model->getLineNo} ";
            $query = Query::updateSQL($data, "LEVY_REQUEST_KOUNYU_MEISAI_DAT", $where);
            $db->query($query);
        }

        //LEVY_S_MST
        $data = array();
        $data["REPAY_DIV"][TEXT]         = $model->field["REPAY_DIV"];
        if ($model->Properties["useBenefit"] == "1") {
            $data["BENEFIT"][TEXT]           = $model->field["BENEFIT"];
        }
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][FUNC]           = "sysdate()";

        if ($model->maxScd == $kounyuscd) {
            list($lmsCd, $sName) = explode(":", $model->field["KOUNYU_L_M_S_CD"]);
            $data["SCHOOLCD"][TEXT]          = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]       = $model->getSchoolKind;
            $data["YEAR"][TEXT]              = $model->year;
            $data["LEVY_L_CD"][TEXT]         = $kounyulcd;
            $data["LEVY_M_CD"][TEXT]         = $kounyumcd;
            $data["LEVY_S_CD"][TEXT]         = $kounyuscd;
            $data["LEVY_S_NAME"][TEXT]       = $sName;

            $query = Query::insertSQL($data, "LEVY_S_MST");
            $db->query($query);
        } else {
            $where  = " WHERE ";
            $where .= "       SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $where .= "   AND SCHOOL_KIND = '{$model->getSchoolKind}' ";
            $where .= "   AND YEAR        = '{$model->year}' ";
            $where .= "   AND LEVY_L_CD   = '{$kounyulcd}' ";
            $where .= "   AND LEVY_M_CD   = '{$kounyumcd}' ";
            $where .= "   AND LEVY_S_CD   = '{$kounyuscd}' ";

            $query = Query::updateSQL($data, "LEVY_S_MST", $where);
            $db->query($query);

        }

        //LEVY_REQUEST_KOUNYU_DAT
        $kounyuCnt = $db->getOne(knjp920_meisaiQuery::getCntKounyuDat($model, $kounyulcd, $kounyumcd));

        //購入額総計を取得
        $reqesutGk = $db->getOne(knjp920_meisaiQuery::getReqesutGk($model, $kounyulcd, $kounyumcd));

        $data = array();
        $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]      = $model->getSchoolKind;
        $data["YEAR"][TEXT]             = $model->year;
        $data["KOUNYU_L_CD"][TEXT]      = $kounyulcd;
        $data["KOUNYU_M_CD"][TEXT]      = $kounyumcd;
        $data["REQUEST_NO"][TEXT]       = $model->getRequestNo;
        $data["REQUEST_GK"][NUMBER]     = $reqesutGk;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        if ($kounyuCnt == 0) {
            $query = Query::insertSQL($data, "LEVY_REQUEST_KOUNYU_DAT");
            $db->query($query);
        } else {
            $where  = " WHERE ";
            $where .= "       SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $where .= "   AND SCHOOL_KIND = '{$model->getSchoolKind}' ";
            $where .= "   AND YEAR        = '{$model->year}' ";
            $where .= "   AND KOUNYU_L_CD = '{$kounyulcd}' ";
            $where .= "   AND KOUNYU_M_CD = '{$kounyumcd}' ";
            $where .= "   AND REQUEST_NO  = '{$model->getRequestNo}' ";

            $query = Query::updateSQL($data, "LEVY_REQUEST_KOUNYU_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        return;
    }

    //LEVY_REQUEST_KOUNYU_DATカウント
    function getCntKounyuDat($model, $lCd, $mCd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_KOUNYU_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->getSchoolKind}' ";
        $query .= "     AND YEAR        = '{$model->year}' ";
        $query .= "     AND KOUNYU_L_CD = '{$lCd}' ";
        $query .= "     AND KOUNYU_M_CD = '{$mCd}' ";
        $query .= "     AND REQUEST_NO  = '{$model->getRequestNo}' ";

        return $query;
    }

    //支出額総計を取得
    function getReqesutGk($model, $lCd, $mCd) {
        $query  = " SELECT ";
        $query .= "     sum(TOTAL_PRICE) ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_KOUNYU_MEISAI_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->getSchoolKind}' ";
        $query .= "     AND YEAR        = '{$model->year}' ";
        $query .= "     AND REQUEST_NO  = '{$model->getRequestNo}' ";
        $query .= "     AND KOUNYU_L_CD = '{$lCd}' ";
        $query .= "     AND KOUNYU_M_CD = '{$mCd}' ";

        return $query;
    }

    //DELETE
    function &getDeleteQuery($db, $model) {
        $db->autoCommit(false);

        $kounyuscd = substr($model->field["KOUNYU_L_M_S_CD"], 4, 2);

        //MEISAI
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_KOUNYU_MEISAI_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= " AND SCHOOL_KIND = '".$model->getSchoolKind."'";
        $query .= " AND YEAR        = '{$model->year}' ";
        $query .= " AND KOUNYU_L_CD = '{$model->getKounyuLcd}' ";
        $query .= " AND KOUNYU_M_CD = '{$model->getKounyuMcd}' ";
        $query .= " AND REQUEST_NO  = '{$model->getRequestNo}' ";
        $query .= " AND KOUNYU_S_CD = '{$kounyuscd}' ";
        $query .= " AND LINE_NO     =  {$model->getLineNo} ";
        $db->query($query);

        $db->commit();
        return;
    }
}
?>

