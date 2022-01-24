<?php

require_once('for_php7.php');

class knjz291_staff_qualifiedModel extends Model {

    var $cmd;
    var $sendSubmit;
    var $sendAuth;
    var $sendPrgid;
    var $field = array();

    function knjz291_staff_qualifiedModel()
    {
    }

    function init() {
        //プロパティファイル
        $this->getPropertiesAll();

        if (VARS::get("SEND_PRGID")) {
            $this->sendAuth = VARS::get("SEND_AUTH");
            $this->sendPrgid = VARS::get("SEND_PRGID");
            $this->sendStaffcd = VARS::get("SEND_STAFFCD");
            $this->field = array();
        }

        $this->cmd = VARS::request("cmd");
        if ($this->cmd == "click") {
            $this->clickSeq = VARS::post("clickSeq");
        }
        $this->field = array("SEQ"                  => VARS::post("SEQ"),
                             "QUALIFIED_CD"         => VARS::post("QUALIFIED_CD"),
                             "QUALIFIED_NAME"       => VARS::post("QUALIFIED_NAME"),
                             "GET_DATE"             => VARS::post("GET_DATE")
                             );

        $this->auth = (VARS::get("SEND_PRGID")) ? $this->sendAuth : AUTHORITY;
        $this->prgid = (VARS::get("SEND_PRGID")) ? $this->sendPrgid : PROGRAMID;
    }

    function validateInput() {
        if ($this->field["QUALIFIED_CD"] == "" &&
            $this->field["QUALIFIED_NAME"] == ""
        ) {
            $this->setWarning("MSG301","免許・資格を選択または入力して下さい。");
            return false;
        }
        if ($this->validate_row_cnt($this->field["QUALIFIED_NAME"], (100 * 1)) > 1) {
            $this->setWarning("MSG915","100バイトまでです。(大文字50)");
            return false;
        }

        $db2 = Query::dbCheckOut2();
        $query = knjz291_staff_qualifiedQuery::getTyoufukuCnt($this);
        $tyoufukuCnt = $db2->getOne($query);
        Query::dbCheckIn($db2);
        if ($this->cmd == "update" || $this->cmd == "delete") {
            if ($this->clickSeq != $this->field["SEQ"]) {
                $this->setWarning("MSG303");
                return false;
            }
        }
        if ($this->cmd == "update" || $this->cmd == "delete") {
            if ($tyoufukuCnt == 0) {
                $this->setWarning("MSG308");
                return false;
            }
        }
        return true;
    }

    function &getInsertModel() {
        if ($this->validateInput()) {
            $seq = knjz291_staff_qualifiedQuery::insertData($this);
            $this->setMessage("MSG201");
            $this->clickSeq = $this->field["SEQ"] = $seq;
        }
    }

    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjz291_staff_qualifiedQuery::updateData($this);
            $this->setMessage("MSG201");
        }
    }

    function &getDeleteModel() {
        if ($this->validateInput()) {
            knjz291_staff_qualifiedQuery::deleteData($this);
            $this->setMessage("MSG201");
            $this->clickSeq = "";
            unset($this->field);
        }
    }
}
?>
