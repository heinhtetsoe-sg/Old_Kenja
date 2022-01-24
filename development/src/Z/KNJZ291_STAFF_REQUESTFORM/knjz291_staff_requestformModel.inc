<?php

require_once('for_php7.php');

class knjz291_staff_requestformModel extends Model {

    var $cmd;
    var $sendSubmit;
    var $sendAuth;
    var $sendPrgid;
    var $field = array();

    function knjz291_staff_requestformModel()
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
            $this->clickSdate = VARS::post("clickSdate");
        }
        $this->field = array("SDATE"            => VARS::post("SDATE"),
                             "EDATE"            => VARS::post("EDATE"),
                             "WORK_DIV"         => VARS::post("WORK_DIV"),
                             "REASON"           => VARS::post("REASON")
                             );

        $this->auth = (VARS::get("SEND_PRGID")) ? $this->sendAuth : AUTHORITY;
        $this->prgid = (VARS::get("SEND_PRGID")) ? $this->sendPrgid : PROGRAMID;
    }

    function validateInput() {
        if ($this->field["SDATE"] == "" ||
            $this->field["EDATE"] == "" ||
            $this->field["WORK_DIV"] == ""
        ) {
            $this->setWarning("MSG301");
            return false;
        }
        if ($this->field["SDATE"] > $this->field["EDATE"]) {
            $this->setWarning("日付範囲が不正です。");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REASON"], (100 * 1)) > 1) {
            $this->setWarning("MSG915","100バイトまでです。(大文字50)");
            return false;
        }

        $db2 = Query::dbCheckOut2();
        $query = knjz291_staff_requestformQuery::getTyoufukuCnt($this);
        $tyoufukuCnt = $db2->getOne($query);
        Query::dbCheckIn($db2);
        if ($this->cmd == "update" || $this->cmd == "delete") {
            if ($this->clickSdate != $this->field["SDATE"]) {
                $this->setWarning("MSG303");
                return false;
            }
        }
        if ($this->cmd == "insert") {
            if ($tyoufukuCnt > 0) {
                $this->setWarning("MSG302");
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
            knjz291_staff_requestformQuery::insertData($this);
            $this->setMessage("MSG201");
            $this->clickSdate = $this->field["SDATE"];
        }
    }

    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjz291_staff_requestformQuery::updateData($this);
            $this->setMessage("MSG201");
        }
    }

    function &getDeleteModel() {
        if ($this->validateInput()) {
            knjz291_staff_requestformQuery::deleteData($this);
            $this->setMessage("MSG201");
            $this->clickSdate = "";
            unset($this->field);
        }
    }
}
?>
