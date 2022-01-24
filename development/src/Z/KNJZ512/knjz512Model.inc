<?php

require_once('for_php7.php');

class knjz512Model extends Model {
    var $field = array();
    var $cmd;
    var $centercd;

    function knjz512Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->centercd);
        }

        $this->field = array("CENTERCD"                 =>  VARS::post("CENTERCD"),
                             "NAME"                     =>  VARS::post("NAME"),
                             "ABBV"                     =>  VARS::post("ABBV"),
                             "ZIPCD"                    =>  VARS::post("ZIPCD"),
                             "ADDR1"                    =>  VARS::post("ADDR1"),
                             "ADDR2"                    =>  VARS::post("ADDR2"),
                             "TELNO"                    =>  VARS::post("TELNO"),
                             "FAXNO"                    =>  VARS::post("FAXNO"),
                             "MEDICAL_DIRECTOR_NAME"    =>  VARS::post("MEDICAL_DIRECTOR_NAME")
                             );

        $this->cmd = VARS::request("cmd");

        if(VARS::get("CENTERCD")) {
            $this->centercd = VARS::get("CENTERCD");
        }
    }

    //入力項目チェック
    function validateInput() {

        //必須チェック
        if (trim($this->field["CENTERCD"]) == "") {
            $this->setWarning("MSG301", "　　　　　　(　コード　)");
            return false;
        }
        if ($this->cmd == "add" || $this->cmd == "update") {
            if (trim($this->field["NAME"]) == "") {
                $this->setWarning("MSG301", "　　　　　　(　名 称　)");
                return false;
            }
        }

        //頭ゼロ埋め
        if (strlen($this->field["CENTERCD"]) <= 5 ) {
            $this->field["CENTERCD"] = sprintf("%05d", $this->field["CENTERCD"]);
        }

        //キーの更新チェック
        if (($this->cmd == "update" || $this->cmd == "delete") &&
            ($this->centercd != $this->field["CENTERCD"])) {
            $this->setWarning("MSG308");
            return false;
        }
        if (($this->cmd == "update" || $this->cmd == "delete") && (!strlen($this->centercd))) {
            $this->setWarning("MSG308");
            return false;
        }

        //存在チェック
        $db = Query::dbCheckOut();
        $query = knjz512Query::getMedicalCenterMst($this->field["CENTERCD"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        //更新対象がなければエラー
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //追加時 既にレコードがあればエラー
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918");
            return false;
        }

        //文字数チェック
        if ($this->cmd == "add" || $this->cmd == "update") {
            if (strlen($this->field["CENTERCD"]) > 5 ) {
                $this->setWarning("MSG915", "(コード) 5バイトまでです。");
                return false;
            }
            if ($this->validate_row_cnt($this->field["NAME"], (30 * 2)) > 1) {
                $this->setWarning("MSG915", "(名称) 全角30文字までです。");
                return false;
            }
            if ($this->validate_row_cnt($this->field["ABBV"], (10 * 2)) > 1) {
                $this->setWarning("MSG915", "(略称) 全角10文字までです。");
                return false;
            }
            if (strlen($this->field["ZIPCD"]) > 8 ) {
                $this->setWarning("MSG915", "(郵便番号) 8バイトまでです。");
                return false;
            }
            if ($this->validate_row_cnt($this->field["ADDR1"], (50 * 2)) > 1) {
                $this->setWarning("MSG915", "(住所１) 全角50文字までです。");
                return false;
            }
            if ($this->validate_row_cnt($this->field["ADDR2"], (50 * 2)) > 1) {
                $this->setWarning("MSG915", "(住所２) 全角50文字までです。");
                return false;
            }
            if (strlen($this->field["TELNO"]) > 14 ) {
                $this->setWarning("MSG915", "(電話番号) 14バイトまでです。");
                return false;
            }
            if (strlen($this->field["FAXNO"]) > 14 ) {
                $this->setWarning("MSG915", "(ＦＡＸ) 14バイトまでです。");
                return false;
            }
            if ($this->validate_row_cnt($this->field["MEDICAL_DIRECTOR_NAME"], (30 * 2)) > 1) {
                $this->setWarning("MSG915", "(代表者・院長) 全角30文字までです。");
                return false;
            }
        }

        return true;
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateModel() {
        if ($this->validateInput()){
            knjz512Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertModel() {
        if ($this->validateInput()){
            knjz512Query::getInsertQuery($this);
            $this->centercd = $this->field["CENTERCD"];
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteModel() {
        if ($this->validateInput()){
            knjz512Query::getDeleteQuery($this);
            unset($this->centercd);
            unset($this->field);
            $this->setMessage("MSG201");
        }
    }
}
?>