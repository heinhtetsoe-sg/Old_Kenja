<?php

require_once('for_php7.php');

class knjz291_staff_name_histModel extends Model {

    var $cmd;
    var $sendSubmit;
    var $sendAuth;
    var $sendPrgid;
    var $field = array();

    function knjz291_staff_name_histModel()
    {
    }

    function init() {
        //プロパティファイル
        $this->getPropertiesAll();

        if (VARS::get("SEND_PRGID")) {
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->sendPrgid    = VARS::get("SEND_PRGID");
            $this->sendStaffcd  = VARS::get("SEND_STAFFCD");
            $this->sendStaffDiv = VARS::get("SEND_STAFF_DIV");
            $this->field = array();
        }

        $this->cmd = VARS::request("cmd");
        if ($this->cmd == "click") {
            $this->clickSdate = VARS::post("clickSdate");
        }
        $this->field = array("SDATE"                => VARS::post("SDATE"),
                             "EDATE"                => VARS::post("EDATE"),
                             "STAFFNAME"            => VARS::post("STAFFNAME"),
                             "STAFFNAME_SHOW"       => VARS::post("STAFFNAME_SHOW"),
                             "STAFFNAME_KANA"       => VARS::post("STAFFNAME_KANA"),
                             "STAFFNAME_ENG"        => VARS::post("STAFFNAME_ENG"),
                             "STAFFNAME_REAL"       => VARS::post("STAFFNAME_REAL"),
                             "STAFFNAME_KANA_REAL"  => VARS::post("STAFFNAME_KANA_REAL")
                             );

        $this->auth = (VARS::get("SEND_PRGID")) ? $this->sendAuth : AUTHORITY;
        $this->prgid = (VARS::get("SEND_PRGID")) ? $this->sendPrgid : PROGRAMID;
    }

    function validateInput() {
        if ($this->field["SDATE"] == "" ||
            $this->field["STAFFNAME"] == "" ||
            $this->field["STAFFNAME_SHOW"] == ""
        ) {
            $this->setWarning("MSG301");
            return false;
        }

        $sizeCheck = array("STAFFNAME"            => array("MSG" => "(職員氏名) 40バイトまでです。", "LEN" => "40", "LINE" => "1"),
                           "STAFFNAME_SHOW"       => array("MSG" => "(職員氏名表示用) 10バイトまでです。", "LEN" => "10", "LINE" => "1"),
                           "STAFFNAME_KANA"       => array("MSG" => "(職員氏名かな) 80バイトまでです。", "LEN" => "80", "LINE" => "1"),
                           "STAFFNAME_ENG"        => array("MSG" => "(職員氏名(英字)) 60バイトまでです。", "LEN" => "60", "LINE" => "1"),
                           "STAFFNAME_REAL"       => array("MSG" => "(戸籍氏名) 80バイトまでです。", "LEN" => "80", "LINE" => "1"),
                           "STAFFNAME_KANA_REAL"  => array("MSG" => "(戸籍氏名かな) 160バイトまでです。", "LEN" => "160", "LINE" => "1")
                           );

        foreach ($sizeCheck as $key => $valArray) {
            if ($this->validate_row_cnt($this->field[$key], ($valArray["LEN"] * $valArray["LINE"])) > 1) {
                $this->setWarning("MSG915", $valArray["MSG"]);
                return false;
            }
        }

        $db = ($this->sendStaffDiv == "2") ? Query::dbCheckOut() : Query::dbCheckOut2();

        //重複チェック
        $query = knjz291_staff_name_histQuery::getTyoufukuCnt($this);
        $tyoufukuCnt = $db->getOne($query);

        //履歴の件数チェック
        $query = knjz291_staff_name_histQuery::getNearSdate($this, 'cnt');
        $cnt = $db->getOne($query);

        Query::dbCheckIn($db);

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
            if ($cnt <= 1) {
                $this->setWarning("MSG919");
                return false;
            }
        }
        return true;
    }

    function &getInsertModel() {
        if ($this->validateInput()) {
            knjz291_staff_name_histQuery::insertData($this);
            knjz291_staff_name_histQuery::updateStaffMst($this);
            $this->setMessage("MSG201");
            $this->clickSdate = $this->field["SDATE"];
        }
    }

    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjz291_staff_name_histQuery::updateData($this);
            knjz291_staff_name_histQuery::updateStaffMst($this);
            $this->setMessage("MSG201");
        }
    }

    function &getDeleteModel() {
        if ($this->validateInput()) {
            knjz291_staff_name_histQuery::deleteData($this);
            knjz291_staff_name_histQuery::updateStaffMst($this);
            $this->setMessage("MSG201");
            $this->clickSdate = "";
            unset($this->field);
        }
    }
}
?>
