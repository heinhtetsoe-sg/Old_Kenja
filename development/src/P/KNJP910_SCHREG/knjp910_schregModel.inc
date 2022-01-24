<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp910_schregModel extends Model {
    var $field      = array();
    var $cmd;
    var $getAuth;
    var $getPrgId;
    var $getSchoolKind;
    var $getYear;
    var $getOutgoLcd;
    var $getOutgoMcd;
    var $getOutgoLMcd;
    var $getRequestNo;
    var $getIncomeLcd;
    var $getLineNo;
    var $auth;
    var $year;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード
    var $selectStudent;
    var $selectStudentLabel;

    /**
     * メンバ変数の初期化
     */
    function knjp910_schregModel()
    {
    }

    function init()
    {
        if (VARS::get("SEND_AUTH")) {
            $this->getAuth         = VARS::get("SEND_AUTH");
            $this->getSchoolKind   = VARS::get("SEND_SCHOOL_KIND");
            $this->getYear         = VARS::get("SEND_YEAR");
            $this->getPrgId        = VARS::get("SEND_PRGID");
            $this->getOutgoLcd     = VARS::get("SEND_OUTGO_L_CD");
            $this->getOutgoMcd     = VARS::get("SEND_OUTGO_M_CD");
            $this->getOutgoLMcd    = VARS::get("SEND_OUTGO_L_M_CD");
            $this->getRequestNo    = VARS::get("SEND_REQUEST_NO");
            $this->getIncomeLcd    = VARS::get("SEND_INCOME_L_CD");
            $this->getLineNo       = VARS::get("SEND_LINE_NO");
        }

        $this->auth = $this->getAuth ? $this->getAuth : AUTHORITY;
        $this->year = $this->getYear ? $this->getYear : CTRL_YEAR;

        $this->field = array("WARIHURI_DIV"         =>  VARS::post("WARIHURI_DIV"),         //1:返金可、2:返金不可切替
                             "OUTGO_L_M_S_CD"       =>  VARS::post("OUTGO_L_M_S_CD"),       //支出細目
                             "COMMODITY_PRICE"      =>  VARS::post("COMMODITY_PRICE"),      //単価
                             "COMMODITY_CNT"        =>  VARS::post("COMMODITY_CNT"),        //数量
                             "TOTAL_TAX"            =>  VARS::post("TOTAL_TAX"),            //消費税
                             "TOTAL_PRICE_ZEINUKI"  =>  VARS::post("TOTAL_PRICE_ZEINUKI"),  //合計金額(税抜き)
                             "TOTAL_PRICE"          =>  VARS::post("TOTAL_PRICE"),          //合計金額(税込)
                             "TRADER_SEIKYU_NO"     =>  VARS::post("TRADER_SEIKYU_NO"),     //請求番号
                             "SEIKYU_MONTH"         =>  VARS::post("SEIKYU_MONTH"),         //請求月
                             "REMARK"               =>  VARS::post("REMARK"),               //摘要
                             "HR_CLASS_HYOUJI_FLG"  =>  VARS::post("HR_CLASS_HYOUJI_FLG"),  //年組番表示
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                     );
        //年組表示を初期値とする
        if (VARS::get("SEND_AUTH")) {
            $this->field["HR_CLASS_HYOUJI_FLG"] = "1";
        }
        $this->selectStudent = VARS::post("selectStudent");
        $this->selectStudentLabel = VARS::post("selectStudentLabel");

        $this->cmd         = VARS::request("cmd");
    }
    
    //入力項目チェック
    function validateInput($db)
    {
        //必須チェック
        if ($this->field["OUTGO_L_M_S_CD"] == "") {
            $this->setWarning("MSG301", "( 品名等 )");
            return false;
        }
        //重複チェック
        $outgoscd = substr($this->field["OUTGO_L_M_S_CD"], 4, 2);
        $getCountData = $db->getOne(knjp910_schregQuery::getMeisaiData($this, $outgoscd, "ETC_LINE_NO"));
        if ($getCountData > 0) {
            $this->setWarning("MSG302", "指定した品名は既に他のNOにて登録済みです。");
            return false;
        }
        //桁数チェック
        if (strlen($this->field["COMMODITY_PRICE"]) > 6) {
            $this->setWarning("MSG901", "( 単価 ) 6バイトまで");
            return false;
        } else if (strlen($this->field["TRADER_SEIKYU_NO"]) > 10) {
            $this->setWarning("TRADER_SEIKYU_NO", "( 請求番号 ) 10バイトまで");
            return false;
        } else if (strlen($this->field["REMARK"]) > 120) {
            $this->setWarning("MSG901", "( 摘要 ) 120バイトまで");
            return false;
        }
        //金額チェック(返金処理)
        if ($this->getHenkinFlg === '1') {
            $errorFlg = false;
            $setSchregNo = "";
            $conma = "";
            $count = 0;
            //収入残高の上限値チェック
            $query = knjp910_schregQuery::getSchnoMoneyData($this);
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["ZAN_GK"] < $this->field["COMMODITY_PRICE"]) {
                    $setSchregNo .= $conma.$row["SCHREGNO"];
                    $conma = ",";
                    $errorFlg = true;
                }
                $count++;
            }
            $result->free();
            if ($errorFlg) {
                $this->setWarning("MSG203", "単価が生徒の収入残高を超えています。(".$setSchregNo.")");
                return false;
            }
            //COLLECTの返金可能額チェック
            $query = knjp910_schregQuery::getSchnoCollectMoneyData($this);
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["HENKIN_KANOU_GK"] < $this->field["COMMODITY_PRICE"]) {
                    $setSchregNo .= $conma.$row["SCHREGNO"];
                    $conma = ",";
                    $errorFlg = true;
                }
                $count++;
            }
            $result->free();
            if ($errorFlg) {
                $this->setWarning("MSG203", "単価が生徒の返金可能な金額を超えています。(".$setSchregNo.")");
                return false;
            }
        }
        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        //入力チェック
        if ($this->validateInput($db)) {
            knjp910_schregQuery::getUpdateQuery($db, $this);
            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);

        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        $db = Query::dbCheckOut();
        
        knjp910_schregQuery::getDeleteQuery($db, $this);
        $this->setMessage("MSG201");
        
        Query::dbCheckIn($db);

        return;
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>

