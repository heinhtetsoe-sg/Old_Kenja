<?php

require_once('for_php7.php');

require_once('../../common/mycalendar.php');

class knjp900_mainModel extends Model {

    var $cmd;
    var $field  = array();
    var $getAuth;
    var $getPrgId;
    var $getSchoolKind;
    var $getYear;
    var $getIncomeLcd;
    var $getIncomeMcd;
    var $getIncomeLMcd;
    var $getRequestNo;
    var $auth;

    function init() {
        $this->cmd         = VARS::request("cmd");
        $this->field = array("INCOME_L_M_CD"       => VARS::post("INCOME_L_M_CD"),
                             "COLLECT_L_M_S_CD"    => VARS::post("COLLECT_L_M_S_CD"),
                             "REQUEST_NO"          => VARS::post("REQUEST_NO"),
                             "REQUEST_STAFF"       => VARS::post("REQUEST_STAFF"),
                             "REQUEST_REASON"      => VARS::post("REQUEST_REASON"),
                             "REQUEST_DATE"        => VARS::post("REQUEST_DATE"),
                             "REQUEST_GK"          => VARS::post("REQUEST_GK")
                              );

        if (VARS::get("SEND_AUTH")) {
            $this->getAuth                 = VARS::get("SEND_AUTH");
            $this->getPrgId                = VARS::get("SEND_PRGID");
            $this->getSchoolKind           = VARS::get("SEND_SCHOOL_KIND");
            $this->getYear                 = VARS::get("SEND_YEAR");
            $this->getIncomeLcd            = VARS::get("SEND_INCOME_L_CD");
            $this->getIncomeMcd            = VARS::get("SEND_INCOME_M_CD");
            $this->getIncomeLMcd           = VARS::get("SEND_INCOME_L_M_CD");
            $this->getRequestNo            = VARS::get("SEND_REQUEST_NO");
        }
        //摘要項目数
        $this->koumoku = "6";
        
        $this->auth = $this->getAuth ? $this->getAuth : AUTHORITY;
        $this->year = $this->getYear ? $this->getYear : CTRL_YEAR;

        //サブミット表示用
        for ($i = 1; $i <= $this->koumoku; $i++) {
            $this->setRow["COMMODITY_NAME".$i] = VARS::request("COMMODITY_NAME".$i);
            $this->setRow["COMMODITY_PRICE".$i] = VARS::request("COMMODITY_PRICE".$i);
            $this->setRow["COMMODITY_CNT".$i] = VARS::request("COMMODITY_CNT".$i);
            $this->setRow["TOTAL_PRICE".$i] = VARS::request("TOTAL_PRICE".$i);
            $this->setRow["REMARK".$i] = VARS::request("REMARK".$i);
        }

        //取消
        if ($this->cmd == "cancel") {
            $this->field = array();
        }
    }

    //入力項目チェック
    function validateInput()
    {
        if (strlen($this->field["REQUEST_REASON"]) > 120) {
            $this->setWarning("MSG901", "( 決済理由 ) 120バイトまで");
            return false;
        } else if (strlen($this->field["REQUEST_GK"]) > 9) {
            $this->setWarning("MSG901", "( 収入額 ) 9バイトまで");
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
                $getMaxRequestNo = $db->getOne(knjp900_mainQuery::getMaxRequestNo($this));
                //対象年度の伝票番号がない場合
                if (!$getMaxRequestNo) {
                    $this->getRequestNo = $this->getYear.'A00001';
                } else {
                    $setNo = sprintf("%05d", intval(substr($getMaxRequestNo, 5, 5)) + 1);
                    $this->getRequestNo = $this->getYear.'A'.$setNo;
                }
            }
            if ($this->cmd === 'delete_update') {
                knjp900_mainQuery::getDeleteQuery($db, $this);
            }
            knjp900_mainQuery::getUpdateQuery($db, $this);

            //KEYの更新
            $incomelcd = substr($this->field["INCOME_L_M_CD"], 0, 2);
            $incomemcd = substr($this->field["INCOME_L_M_CD"], 2, 2);

            $this->getIncomeLcd            = $incomelcd;
            $this->getIncomeMcd            = $incomemcd;
            $this->getIncomeLMcd           = $this->field["INCOME_L_M_CD"];

            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);

        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        $db = Query::dbCheckOut();
        
        knjp900_mainQuery::getDeleteQuery($db, $this, "ALL");
        
        $this->getIncomeLcd  = "";
        $this->getIncomeMcd  = "";
        $this->getIncomeLMcd = "";
        $this->getRequestNo  = "";
        
        $this->setMessage("MSG201");
        
        Query::dbCheckIn($db);

        return;
    }
}
?>
