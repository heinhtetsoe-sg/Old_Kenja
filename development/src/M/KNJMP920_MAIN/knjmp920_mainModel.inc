<?php

require_once('for_php7.php');

require_once('../../common/mycalendar.php');

class knjmp920_mainModel extends Model {

    var $cmd;
    var $field  = array();
    var $koumoku;
    var $getAuth;
    var $getPrgId;
    var $getYear;
    var $getKounyuLcd;
    var $getKounyuMcd;
    var $getKounyuLMcd;
    var $getRequestNo;
    var $auth;
    var $setRow = array();

    function init() {
        $this->cmd         = VARS::request("cmd");
        $this->field = array("KOUNYU_L_M_CD"      => VARS::post("KOUNYU_L_M_CD"),
                             "REQUEST_NO"         => VARS::post("REQUEST_NO"),
                             "REQUEST_STAFF"      => VARS::post("REQUEST_STAFF"),
                             "REQUEST_REASON"     => VARS::post("REQUEST_REASON"),
                             "REQUEST_DATE"       => VARS::post("REQUEST_DATE"),
                             "REQUEST_GK"         => VARS::post("REQUEST_GK"),
                             "REQUEST_TESUURYOU"  => VARS::post("REQUEST_TESUURYOU"),
                             
                             "TRADER_CD1"         => VARS::post("TRADER_CD1"),
                             "TRADER_NAME1"       => VARS::post("TRADER_NAME1"),
                             "TRADER_KAKUTEI1"    => VARS::post("TRADER_KAKUTEI1"),
                             "TRADER_CD2"         => VARS::post("TRADER_CD2"),
                             "TRADER_NAME2"       => VARS::post("TRADER_NAME2"),
                             "TRADER_KAKUTEI2"    => VARS::post("TRADER_KAKUTEI2"),
                             "TRADER_CD3"         => VARS::post("TRADER_CD3"),
                             "TRADER_NAME3"       => VARS::post("TRADER_NAME3"),
                             "TRADER_KAKUTEI3"    => VARS::post("TRADER_KAKUTEI3"),
                             "TRADER_CD4"         => VARS::post("TRADER_CD4"),
                             "TRADER_NAME4"       => VARS::post("TRADER_NAME4"),
                             "TRADER_KAKUTEI4"    => VARS::post("TRADER_KAKUTEI4"),
                             
                             "KOUNYU_MITUMORI_DATE" => VARS::post("KOUNYU_MITUMORI_DATE"),
                             "KEIYAKU_HOUHOU"       => VARS::post("KEIYAKU_HOUHOU"),
                             "NOUNYU_LIMIT_DATE"    => VARS::post("NOUNYU_LIMIT_DATE"),
                             "NOUNYU_PLACE"         => VARS::post("NOUNYU_PLACE"),
                             "REMARK"               => VARS::post("REMARK")

                              );

        if (VARS::get("SEND_AUTH")) {
            $this->getAuth                 = VARS::get("SEND_AUTH");
            $this->getPrgId                = VARS::get("SEND_PRGID");
            $this->getYear                 = VARS::get("SEND_YEAR");
            $this->getKounyuLcd            = VARS::get("SEND_KOUNYU_L_CD");
            $this->getKounyuMcd            = VARS::get("SEND_KOUNYU_M_CD");
            $this->getKounyuLMcd           = VARS::get("SEND_KOUNYU_L_M_CD");
            $this->getRequestNo            = VARS::get("SEND_REQUEST_NO");
            $this->getOutgoRequestNo       = VARS::get("SEND_OUTGO_REQUEST_NO");
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
        if (strlen($this->field["TRADER_NAME1"]) > 120 || strlen($this->field["TRADER_NAME2"]) > 120 ||
            strlen($this->field["TRADER_NAME3"]) > 120 || strlen($this->field["TRADER_NAME4"]) > 120) {
            $this->setWarning("MSG901", "( 業者名 ) 120バイトまで");
            return false;
        } else if (strlen($this->field["KEIYAKU_HOUHOU"]) > 120) {
            $this->setWarning("MSG901", "( 契約方法 ) 120バイトまで");
            return false;
        } else if (strlen($this->field["NOUNYU_PLACE"]) > 120) {
            $this->setWarning("MSG901", "( 納入場所 ) 120バイトまで");
            return false;
        } else if (strlen($this->field["REMARK"]) > 120) {
            $this->setWarning("MSG901", "( その他 ) 120バイトまで");
            return false;
        } else if (strlen($this->field["REQUEST_TESUURYOU"]) > 7) {
            $this->setWarning("MSG901", "( 振込手数料 ) 7バイトまで");
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
                $getMaxRequestNo = $db->getOne(knjmp920_mainQuery::getMaxRequestNo($this));
                //対象年度の伝票番号がない場合
                if (!$getMaxRequestNo) {
                    $this->getRequestNo = $this->getYear.'C00001';
                } else {
                    $setNo = sprintf("%05d", intval(substr($getMaxRequestNo, 5, 5)) + 1);
                    $this->getRequestNo = $this->getYear.'C'.$setNo;
                }
            }
            if ($this->cmd === 'delete_update') {
                knjmp920_mainQuery::getDeleteQuery($db, $this);
            }
            knjmp920_mainQuery::getUpdateQuery($db, $this);

            //KEYの更新
            $kounyulcd = substr($this->field["KOUNYU_L_M_CD"], 0, 2);
            $kounyumcd = substr($this->field["KOUNYU_L_M_CD"], 2, 2);

            $this->getKounyuLcd            = $kounyulcd;
            $this->getKounyuMcd            = $kounyumcd;
            $this->getKounyuLMcd           = $this->field["KOUNYU_L_M_CD"];

            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);

        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        $db = Query::dbCheckOut();
        
        knjmp920_mainQuery::getDeleteQuery($db, $this, "ALL");
        
        $this->getKounyuLcd  = "";
        $this->getKounyuMcd  = "";
        $this->getKounyuLMcd = "";
        $this->getRequestNo  = "";
        $this->getOutgoRequestNo = "";
        
        $this->setMessage("MSG201");
        
        Query::dbCheckIn($db);

        return;
    }
}
?>
