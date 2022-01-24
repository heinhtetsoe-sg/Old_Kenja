<?php

require_once('for_php7.php');

class knjz291_staff_workhistModel extends Model {

    var $cmd;
    var $sendSubmit;
    var $sendAuth;
    var $sendPrgid;
    var $field = array();

    function knjz291_staff_workhistModel()
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
            $this->clickFdate = VARS::post("clickFdate");
        }
        $this->field = array("FROM_DATE"        => VARS::post("FROM_DATE"),
                             "FROM_DIV"         => VARS::post("FROM_DIV"),
                             "FROM_SCHOOLCD"    => VARS::post("FROM_SCHOOLCD"),
                             "FROM_COURSECD"    => VARS::post("FROM_COURSECD"),
                             "TO_DATE"          => VARS::post("TO_DATE"),
                             "TO_DIV"           => VARS::post("TO_DIV"),
                             "TO_SCHOOLCD"      => VARS::post("TO_SCHOOLCD"),
                             "TO_COURSECD"      => VARS::post("TO_COURSECD"),
                             "REMARK"           => VARS::post("REMARK")
                             );

        $this->auth = (VARS::get("SEND_PRGID")) ? $this->sendAuth : AUTHORITY;
        $this->prgid = (VARS::get("SEND_PRGID")) ? $this->sendPrgid : PROGRAMID;
    }

    function validateInput() {
        if ($this->field["FROM_DATE"] == "" ||
            $this->field["FROM_DIV"] == ""
        ) {
            $this->setWarning("MSG301");
            return false;
        }
        if ($this->field["TO_DIV"] == "02" &&
            $this->field["TO_DATE"] == ""
        ) {
            $this->setWarning("MSG301");
            return false;
        }
        if (strlen($this->field["TO_DATE"]) > 0 && $this->field["FROM_DATE"] > $this->field["TO_DATE"]) {
            $this->setWarning("日付範囲が不正です。");
            return false;
        }

        $db2 = Query::dbCheckOut2();
        $query = knjz291_staff_workhistQuery::getTyoufukuCnt($this);
        $tyoufukuCnt = $db2->getOne($query);
        Query::dbCheckIn($db2);
        if ($this->cmd == "update" || $this->cmd == "delete") {
            if ($this->clickFdate != $this->field["FROM_DATE"]) {
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
        if ($this->cmd == "update") {
            if ($tyoufukuCnt > 1) {
                $this->setWarning("MSG302");
                return false;
            }
        }
        if ($this->cmd == "delete") {
            if ($tyoufukuCnt == 0) {
                $this->setWarning("MSG308");
                return false;
            }
        }
        return true;
    }

    function &getInsertModel() {
        if ($this->validateInput()) {
            knjz291_staff_workhistQuery::insertData($this);
            knjz291_staff_workhistQuery::deleteStaffYdat($this);
            $this->setMessage("MSG201");
            $this->clickFdate = $this->field["FROM_DATE"];
        }
    }

    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjz291_staff_workhistQuery::updateData($this);
            knjz291_staff_workhistQuery::deleteStaffYdat($this);
            $this->setMessage("MSG201");
        }
    }

    function &getDeleteModel() {
        if ($this->validateInput()) {
            knjz291_staff_workhistQuery::deleteData($this);
            $this->setMessage("MSG201");
            $this->clickFdate = "";
            unset($this->field);
        }
    }
}
?>
