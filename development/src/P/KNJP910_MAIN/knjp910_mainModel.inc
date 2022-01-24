<?php

require_once('for_php7.php');

require_once('../../common/mycalendar.php');

class knjp910_mainModel extends Model {

    var $cmd;
    var $field  = array();
    var $getAuth;
    var $koumoku;
    var $getPrgId;
    var $getSchoolKind;
    var $getYear;
    var $getOutgoLcd;
    var $getOutgoMcd;
    var $getOutgoLMcd;
    var $getRequestNo;
    var $getKounyuRequestNo;
    var $getSekouRequestNo;
    var $getSeisanRequestNo;
    var $auth;
    var $setRow = array();

    function init() {
        $this->cmd         = VARS::request("cmd");
        $this->field = array("OUTGO_L_M_CD"       => VARS::post("OUTGO_L_M_CD"),
                             "INCOME_L_CD"        => VARS::post("INCOME_L_CD"),
                             "TOTAL_ZAN_GK"       => VARS::post("TOTAL_ZAN_GK"),
                             "REQUEST_NO"         => VARS::post("REQUEST_NO"),
                             "REQUEST_STAFF"      => VARS::post("REQUEST_STAFF"),
                             "REQUEST_REASON"     => VARS::post("REQUEST_REASON"),
                             "REQUEST_DATE"       => VARS::post("REQUEST_DATE"),
                             "REQUEST_GK"         => VARS::post("REQUEST_GK"),
                             "REQUEST_TESUURYOU"  => VARS::post("REQUEST_TESUURYOU"),
                             "TRADER_CD"          => VARS::post("TRADER_CD"),
                             "TRADER_NAME"        => VARS::post("TRADER_NAME"),
                             "BANKCD"             => VARS::post("BANKCD"),
                             "BRANCHCD"           => VARS::post("BRANCHCD"),
                             "BANK_DEPOSIT_ITEM"  => VARS::post("BANK_DEPOSIT_ITEM"),
                             "BANK_ACCOUNTNO"     => VARS::post("BANK_ACCOUNTNO"),
                             "ACCOUNTNAME"        => VARS::post("ACCOUNTNAME"),
                             "ACCOUNTNAME_KANA"   => VARS::post("ACCOUNTNAME_KANA"),
                             "PAY_DIV"            => VARS::post("PAY_DIV"),
                             //返金用
                             "HENKIN_FLG"         => VARS::post("HENKIN_FLG"),
                             "COLLECT_L_M_S_GRP_CD"   => VARS::post("COLLECT_L_M_S_GRP_CD")
                              );

        if (VARS::get("SEND_AUTH")) {
            $this->getAuth                 = VARS::get("SEND_AUTH");
            $this->getPrgId                = VARS::get("SEND_PRGID");
            $this->getSchoolKind           = VARS::get("SEND_SCHOOL_KIND");
            $this->getYear                 = VARS::get("SEND_YEAR");
            $this->getOutgoLcd             = VARS::get("SEND_OUTGO_L_CD");
            $this->getOutgoMcd             = VARS::get("SEND_OUTGO_M_CD");
            $this->getOutgoLMcd            = VARS::get("SEND_OUTGO_L_M_CD");
            $this->getRequestNo            = VARS::get("SEND_REQUEST_NO");
            //以下、他画面より取得
            $this->getKounyuRequestNo      = VARS::get("SEND_KOUNYU_REQUEST_NO");
            $this->getSekouRequestNo       = VARS::get("SEND_SEKOU_REQUEST_NO");
            $this->getSeisanRequestNo      = VARS::get("SEND_SEISAN_REQUEST_NO");
        }
        //摘要項目数
        $this->koumoku = "5";
        
        $this->auth = $this->getAuth ? $this->getAuth : AUTHORITY;
        $this->year = $this->getYear ? $this->getYear : CTRL_YEAR;
        
        //サブミット表示用
        for ($i = 1; $i <= $this->koumoku; $i++) {
            $this->setRow["LEVY_S_NAME".$i] = VARS::request("LEVY_S_NAME".$i);
            $this->setRow["COMMODITY_PRICE".$i] = VARS::request("COMMODITY_PRICE".$i);
            $this->setRow["COMMODITY_CNT".$i] = VARS::request("COMMODITY_CNT".$i);
            $this->setRow["TOTAL_PRICE_ZEINUKI".$i] = VARS::request("TOTAL_PRICE_ZEINUKI".$i);
            $this->setRow["REMARK".$i] = VARS::request("REMARK".$i);
            $this->setRow["TOTAL_TAX".$i] = VARS::request("TOTAL_TAX".$i);
            $this->setRow["TOTAL_PRICE".$i] = VARS::request("TOTAL_PRICE".$i);
        }
        
        //取消
        if ($this->cmd == "cancel") {
            $this->field = array();
        }
    }

    //入力項目チェック
    function validateInput()
    {
        //桁数チェック
        if (strlen($this->field["REQUEST_REASON"]) > 120) {
            $this->setWarning("MSG901", "( 決済理由 ) 120バイトまで");
            return false;
        } else if (strlen($this->field["REQUEST_GK"]) > 7) {
            $this->setWarning("MSG901", "( 収入額 ) 7バイトまで");
            return false;
        } else if (strlen($this->field["REQUEST_TESUURYOU"]) > 7) {
            $this->setWarning("MSG901", "( 振込手数料 ) 7バイトまで");
            return false;
        } else if (strlen($this->field["TRADER_NAME"]) > 120) {
            $this->setWarning("MSG901", "( 業者名 ) 120バイトまで");
            return false;
        } else if (strlen($this->field["BANK_ACCOUNTNO"]) > 7) {
            $this->setWarning("MSG901", "( 口座番号 ) 7バイトまで");
            return false;
        } else if (strlen($this->field["ACCOUNTNAME"]) > 120) {
            $this->setWarning("MSG901", "( 口座名義 ) 7バイトまで");
            return false;
        } else if (strlen($this->field["ACCOUNTNAME_KANA"]) > 120) {
            $this->setWarning("MSG901", "( 口座名義カナ ) 120バイトまで");
            return false;
        }
        
        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        //入力チェック
        if ($this->validateInput()) {
            //新規の場合は伝票番号を発行
            if (!$this->getRequestNo) {
                $getMaxRequestNo = $db->getOne(knjp910_mainQuery::getMaxRequestNo($this));
                //対象年度の伝票番号がない場合
                if (!$getMaxRequestNo) {
                    $this->getRequestNo = $this->getYear.'B00001';
                } else {
                    $setNo = sprintf("%05d", intval(substr($getMaxRequestNo, 5, 5)) + 1);
                    $this->getRequestNo = $this->getYear.'B'.$setNo;
                }
            }
            if ($this->cmd === 'delete_update') {
                knjp910_mainQuery::getDeleteQuery($db, $this);
            }
            knjp910_mainQuery::getUpdateQuery($db, $this);

            //KEYの更新
            $outgolcd = substr($this->field["OUTGO_L_M_CD"], 0, 2);
            $outgomcd = substr($this->field["OUTGO_L_M_CD"], 2, 2);

            $this->getOutgoLcd            = $outgolcd;
            $this->getOutgoMcd            = $outgomcd;
            $this->getOutgoLMcd           = $this->field["OUTGO_L_M_CD"];
            
            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);

        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        $db = Query::dbCheckOut();
        
        knjp910_mainQuery::getDeleteQuery($db, $this, "ALL");
        
        //購入伺、施行伺、精算票がない場合のみ初期化(パラメータで渡せないため)
        if ($this->getKounyuRequestNo == "" && $this->getSekouRequestNo == "" && $this->getSeisanRequestNo == "") {
            $this->getOutgoLcd  = "";
            $this->getOutgoMcd  = "";
            $this->getOutgoLMcd = "";
        }
        $this->getRequestNo  = "";
        
        $this->setMessage("MSG201");
        
        Query::dbCheckIn($db);

        return;
    }
}
?>
