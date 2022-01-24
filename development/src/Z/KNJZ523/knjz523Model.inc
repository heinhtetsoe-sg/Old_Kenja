<?php

require_once('for_php7.php');

class knjz523Model extends Model {
    var $field = array();
    var $cmd;
    var $namecd;

    function knjz523Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->namecd);
        }

        $this->field = array("CARDNAME_CD"            =>  VARS::post("CARDNAME_CD"),
                             "CARDNAME" =>  VARS::post("CARDNAME")
                             );

        $this->cmd = VARS::request("cmd");

        if(VARS::get("CARDNAME_CD")) {
            $this->namecd = VARS::get("CARDNAME_CD");
        }
    }

    //入力項目チェック
    function validateInput() {

        //必須チェック
        if (trim($this->field["CARDNAME_CD"]) == "") {
            $this->setWarning("MSG301", "　　　　(　手帳の障害コード　)");
            return false;
        }
        if ($this->cmd == "add" || $this->cmd == "update") {
            if (trim($this->field["CARDNAME"]) == "") {
                $this->setWarning("MSG301", "　　　　　　(　手帳の障害名称　)");
                return false;
            }
        }

        //頭ゼロ埋め
        if (strlen($this->field["CARDNAME_CD"]) <= 2) {
            $this->field["CARDNAME_CD"] = sprintf("%02d", $this->field["CARDNAME_CD"]);
        }

        //キーの更新チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && ($this->namecd != $this->field["CARDNAME_CD"])) {
            $this->setWarning("MSG308");
            return false;
        }
        if (($this->cmd == "update" || $this->cmd == "delete") && !strlen($this->namecd)) {
            $this->setWarning("MSG308");
            return false;
        }

        //存在チェック
        $db = Query::dbCheckOut();
        $query = knjz523Query::getChallengedCardNameMst($this->field["CARDNAME_CD"]);
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
            if (strlen($this->field["CARDNAME_CD"]) > 2 ) {
                $this->setWarning("MSG915", "(手帳の障害コード) 2バイトまでです。");
                return false;
            }
            if ($this->validate_row_cnt($this->field["CARDNAME"], (30 * 2)) > 1) {
                $this->setWarning("MSG915", "(手帳の障害名称) 全角30文字までです。");
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
            knjz523Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertModel() {
        if ($this->validateInput()){
            knjz523Query::getInsertQuery($this);
            $this->namecd = $this->field["CARDNAME_CD"];
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteModel() {
        if ($this->validateInput()){
            knjz523Query::getDeleteQuery($this);
            unset($this->namecd);
            unset($this->field);
            $this->setMessage("MSG201");
        }
    }
}
?>
