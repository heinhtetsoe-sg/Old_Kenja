<?php

require_once('for_php7.php');

class knjz291_staff_addressModel extends Model {

    var $cmd;
    var $sendSubmit;
    var $sendAuth;
    var $sendPrgid;
    var $field = array();

    function knjz291_staff_addressModel()
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
        $this->field = array("SDATE"        => VARS::post("SDATE"),
                             "EDATE"        => VARS::post("EDATE"),
                             "STAFFZIPCD"   => VARS::post("STAFFZIPCD"),
                             "STAFFADDR1"   => VARS::post("STAFFADDR1"),
                             "STAFFADDR2"   => VARS::post("STAFFADDR2"),
                             "STAFFTELNO"   => VARS::post("STAFFTELNO"),
                             "STAFFFAXNO"   => VARS::post("STAFFFAXNO"),
                             "STAFFE_MAIL"  => VARS::post("STAFFE_MAIL")
                             );

        $this->auth = (VARS::get("SEND_PRGID")) ? $this->sendAuth : AUTHORITY;
        $this->prgid = (VARS::get("SEND_PRGID")) ? $this->sendPrgid : PROGRAMID;
    }

    function validateInput() {
        if ($this->field["SDATE"] == "" ) {
            $this->setWarning("MSG301");
            return false;
        }

        //文字型チェック
        foreach (array("STAFFTELNO"  => array("[^-|0-9]", "( 電話番号 )"),
                      "STAFFFAXNO"   => array("[^-|0-9]", "( FAX番号 )")
                ) as $key => $val
        ) {
            if ($this->field[$key] !="" && preg_match('{' . $val[0] . '}i', trim($this->field[$key]))) {
                $this->setWarning("MSG901", $val[1]);
                return false;
            }
        }

        $sizeCheck = array("STAFFZIPCD"     => array("MSG" => "(郵便番号) 8バイトまでです。", "LEN" => "8", "LINE" => "1"),
                           "STAFFADDR1"     => array("MSG" => "(住所1) 90バイトまでです。", "LEN" => "90", "LINE" => "1"),
                           "STAFFADDR2"     => array("MSG" => "(住所2) 90バイトまでです。", "LEN" => "90", "LINE" => "1"),
                           "STAFFTELNO"     => array("MSG" => "(電話番号) 14バイトまでです。", "LEN" => "14", "LINE" => "1"),
                           "STAFFFAXNO"     => array("MSG" => "(FAX番号) 14バイトまでです。", "LEN" => "14", "LINE" => "1"),
                           "STAFFE_MAIL"    => array("MSG" => "(E-Mail) 25バイトまでです。", "LEN" => "25", "LINE" => "1")
                           );

        foreach ($sizeCheck as $key => $valArray) {
            if ($this->validate_row_cnt($this->field[$key], ($valArray["LEN"] * $valArray["LINE"])) > 1) {
                $this->setWarning("MSG915", $valArray["MSG"]);
                return false;
            }
        }

        $db = ($this->sendStaffDiv == "2") ? Query::dbCheckOut() : Query::dbCheckOut2();

        //重複チェック
        $query = knjz291_staff_addressQuery::getTyoufukuCnt($this);
        $tyoufukuCnt = $db->getOne($query);

        //履歴の件数チェック
        $query = knjz291_staff_addressQuery::getNearSdate($this, 'cnt');
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
            knjz291_staff_addressQuery::insertData($this);
            knjz291_staff_addressQuery::updateStaffMst($this);
            $this->setMessage("MSG201");
            $this->clickSdate = $this->field["SDATE"];
        }
    }

    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjz291_staff_addressQuery::updateData($this);
            knjz291_staff_addressQuery::updateStaffMst($this);
            $this->setMessage("MSG201");
        }
    }

    function &getDeleteModel() {
        if ($this->validateInput()) {
            knjz291_staff_addressQuery::deleteData($this);
            knjz291_staff_addressQuery::updateStaffMst($this);
            $this->setMessage("MSG201");
            $this->clickSdate = "";
            unset($this->field);
        }
    }
}
?>
