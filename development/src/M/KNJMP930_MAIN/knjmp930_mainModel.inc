<?php

require_once('for_php7.php');

require_once('../../common/mycalendar.php');

class knjmp930_mainModel extends Model {

    var $cmd;
    var $field  = array();
    var $getAuth;
    var $getPrgId;
    var $getYear;
    var $getSekouLcd;
    var $getSekouMcd;
    var $getSekouLMcd;
    var $getRequestNo;
    var $auth;

    function init() {
        $this->cmd         = VARS::request("cmd");
        $this->field = array("SEKOU_L_M_CD"      => VARS::post("SEKOU_L_M_CD"),
                             "REQUEST_NO"         => VARS::post("REQUEST_NO"),
                             "REQUEST_STAFF"      => VARS::post("REQUEST_STAFF"),
                             "REQUEST_REASON"     => VARS::post("REQUEST_REASON"),
                             "REQUEST_DATE"       => VARS::post("REQUEST_DATE"),
                             "REQUEST_GK"         => VARS::post("REQUEST_GK"),
                                                          
                             "SEKOU_JIGYOU_NAME"  => VARS::post("SEKOU_JIGYOU_NAME"),
                             "SEKOU_NAIYOU"       => VARS::post("SEKOU_NAIYOU"),
                             "SEKOU_DATE_FROM"    => VARS::post("SEKOU_DATE_FROM"),
                             "SEKOU_DATE_TO"      => VARS::post("SEKOU_DATE_TO"),
                             "SEKOU_PLACE"        => VARS::post("SEKOU_PLACE"),
                             "KEIYAKU_HOUHOU"     => VARS::post("KEIYAKU_HOUHOU"),
                             "REMARK"             => VARS::post("REMARK")

                              );

        if (VARS::get("SEND_AUTH")) {
            $this->getAuth                 = VARS::get("SEND_AUTH");
            $this->getPrgId                = VARS::get("SEND_PRGID");
            $this->getYear                 = VARS::get("SEND_YEAR");
            $this->getSekouLcd             = VARS::get("SEND_SEKOU_L_CD");
            $this->getSekouMcd             = VARS::get("SEND_SEKOU_M_CD");
            $this->getSekouLMcd            = VARS::get("SEND_SEKOU_L_M_CD");
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
        if (strlen($this->field["SEKOU_JIGYOU_NAME"]) > 120) {
            $this->setWarning("MSG901", "( 事業名 ) 120バイトまで");
            return false;
        } else if (strlen($this->field["SEKOU_NAIYOU"]) > 120) {
            $this->setWarning("MSG901", "( 施行内容 ) 120バイトまで");
            return false;
        } else if (strlen($this->field["SEKOU_PLACE"]) > 120) {
            $this->setWarning("MSG901", "( 施行場所 ) 120バイトまで");
            return false;
        } else if (strlen($this->field["KEIYAKU_HOUHOU"]) > 120) {
            $this->setWarning("MSG901", "( 契約方法 ) 120バイトまで");
            return false;
        } else if (strlen($this->field["REMARK"]) > 500) {
            $this->setWarning("MSG901", "( 付記事項 ) 500バイトまで");
            return false;
        } else if (strlen($this->field["REQUEST_GK"]) > 7) {
            $this->setWarning("MSG901", "( 予算額 ) 7バイトまで");
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
                $getMaxRequestNo = $db->getOne(knjmp930_mainQuery::getMaxRequestNo($this));
                //対象年度の伝票番号がない場合
                if (!$getMaxRequestNo) {
                    $this->getRequestNo = $this->getYear.'D00001';
                } else {
                    $setNo = sprintf("%05d", intval(substr($getMaxRequestNo, 5, 5)) + 1);
                    $this->getRequestNo = $this->getYear.'D'.$setNo;
                }
            }
            knjmp930_mainQuery::getUpdateQuery($db, $this);

            //KEYの更新
            $sekoulcd = substr($this->field["SEKOU_L_M_CD"], 0, 2);
            $sekoumcd = substr($this->field["SEKOU_L_M_CD"], 2, 2);

            $this->getSekouLcd            = $sekoulcd;
            $this->getSekouMcd            = $sekoumcd;
            $this->getSekouLMcd           = $this->field["SEKOU_L_M_CD"];

            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);

        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        $db = Query::dbCheckOut();
        
        knjmp930_mainQuery::getDeleteQuery($db, $this);
        
        $this->getSekouLcd  = "";
        $this->getSekouMcd  = "";
        $this->getSekouLMcd = "";
        $this->getRequestNo  = "";
        $this->getOutgoRequestNo = "";

        $this->setMessage("MSG201");
        
        Query::dbCheckIn($db);

        return;
    }
}
?>
