<?php

require_once('for_php7.php');

require_once('../../common/mycalendar.php');

class knjmp940_mainModel extends Model {

    var $cmd;
    var $field  = array();
    var $getAuth;
    var $getPrgId;
    var $getYear;
    var $getSeisanLcd;
    var $getSeisanMcd;
    var $getSeisanLMcd;
    var $getRequestNo;
    var $getOutgoRequestNo;
    var $auth;

    function init() {
        $this->cmd         = VARS::request("cmd");
        $this->field = array("SEISAN_L_M_CD"      => VARS::post("SEISAN_L_M_CD"),
                             "REQUEST_NO"         => VARS::post("REQUEST_NO"),
                             "REQUEST_STAFF"      => VARS::post("REQUEST_STAFF"),
                             "REQUEST_REASON"     => VARS::post("REQUEST_REASON"),
                             "REQUEST_DATE"       => VARS::post("REQUEST_DATE"),
                             "REQUEST_GK"         => VARS::post("REQUEST_GK"),
                                                          
                             "SEISAN_TITLE"         => VARS::post("SEISAN_TITLE"),
                             "GENKIN_JURYOU_STAFF"  => VARS::post("GENKIN_JURYOU_STAFF"),
                             "SEISAN_NAIYOU"        => VARS::post("SEISAN_NAIYOU"),
                             "JURYOU_DATE"          => VARS::post("JURYOU_DATE"),
                             "JURYOU_GK"            => VARS::post("JURYOU_GK"),
                             "SIHARAI_DATE"         => VARS::post("SIHARAI_DATE"),
                             "SIHARAI_GK"           => VARS::post("SIHARAI_GK"),
                             "ZAN_GK"               => VARS::post("ZAN_GK"),
                             "REMARK"               => VARS::post("REMARK"),
                             "SUITOU_STAFF"         => VARS::post("SUITOU_STAFF"),
                             "INCOME_DATE"          => VARS::post("INCOME_DATE")

                              );

        if (VARS::get("SEND_AUTH")) {
            $this->getAuth                 = VARS::get("SEND_AUTH");
            $this->getPrgId                = VARS::get("SEND_PRGID");
            $this->getYear                 = VARS::get("SEND_YEAR");
            $this->getSeisanLcd            = VARS::get("SEND_SEISAN_L_CD");
            $this->getSeisanMcd            = VARS::get("SEND_SEISAN_M_CD");
            $this->getSeisanLMcd           = VARS::get("SEND_SEISAN_L_M_CD");
            $this->getRequestNo            = VARS::get("SEND_REQUEST_NO");
            $this->getOutgoRequestNo       = VARS::get("SEND_OUTGO_REQUEST_NO");
        }
        
        $this->auth = $this->getAuth ? $this->getAuth : AUTHORITY;
        $this->year = $this->getYear ? $this->getYear : CTRL_YEAR;
        
        //取消
        if ($this->cmd == "cancel") {
            $this->field = array();
        }
    }

    //入力項目チェック
    function validateInput()
    {
        //桁数チェック
        if (strlen($this->field["SEISAN_TITLE"]) > 120) {
            $this->setWarning("MSG901", "( 件名 ) 120バイトまで");
            return false;
        } else if (strlen($this->field["SEISAN_NAIYOU"]) > 250) {
            $this->setWarning("MSG901", "( 精算内容 ) 250バイトまで");
            return false;
        } else if (strlen($this->field["JURYOU_GK"]) > 7) {
            $this->setWarning("MSG901", "( 受領額 ) 7バイトまで");
            return false;
        } else if (strlen($this->field["SIHARAI_GK"]) > 7) {
            $this->setWarning("MSG901", "( 支払額 ) 7バイトまで");
            return false;
        } else if (strlen($this->field["ZAN_GK"]) > 7) {
            $this->setWarning("MSG901", "( 残額 ) 7バイトまで");
            return false;
        } else if (strlen($this->field["REMARK"]) > 120) {
            $this->setWarning("MSG901", "( 備考 ) 120バイトまで");
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
                $getMaxRequestNo = $db->getOne(knjmp940_mainQuery::getMaxRequestNo($this));
                //対象年度の伝票番号がない場合
                if (!$getMaxRequestNo) {
                    $this->getRequestNo = $this->getYear.'E00001';
                } else {
                    $setNo = sprintf("%05d", intval(substr($getMaxRequestNo, 5, 5)) + 1);
                    $this->getRequestNo = $this->getYear.'E'.$setNo;
                }
            }
            knjmp940_mainQuery::getUpdateQuery($db, $this);

            //KEYの更新
            $seisanlcd = substr($this->field["SEISAN_L_M_CD"], 0, 2);
            $seisanmcd = substr($this->field["SEISAN_L_M_CD"], 2, 2);

            $this->getSeisanLcd            = $seisanlcd;
            $this->getSeisanMcd            = $seisanmcd;
            $this->getSeisanLMcd           = $this->field["SEISAN_L_M_CD"];

            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);

        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        $db = Query::dbCheckOut();
        
        knjmp940_mainQuery::getDeleteQuery($db, $this);
        
        $this->getSeisanLcd  = "";
        $this->getSeisanMcd  = "";
        $this->getSeisanLMcd = "";
        $this->getRequestNo  = "";
        $this->getOutgoRequestNo  = "";
        
        $this->setMessage("MSG201");
        
        Query::dbCheckIn($db);

        return;
    }
}
?>
