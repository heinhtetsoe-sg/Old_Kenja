<?php

require_once('for_php7.php');

class knjz520Model extends Model {
    var $field = array();
    var $cmd;
    var $membercd;

    function knjz520Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->membercd);
        }

        $this->field = array("MEMBERCD"         =>  VARS::post("MEMBERCD"),
                             "NAME"             =>  VARS::post("NAME"),
                             "ABBV"             =>  VARS::post("ABBV")
                             );

        $this->cmd = VARS::request("cmd");

        if(VARS::get("MEMBERCD")) {
            $this->membercd = VARS::get("MEMBERCD");
        }
    }

    //入力項目チェック
    function validateInput() {

        //必須チェック
        if (trim($this->field["MEMBERCD"]) == "") {
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
        if (strlen($this->field["MEMBERCD"]) <= 7 ) {
            $this->field["MEMBERCD"] = sprintf("%07d", $this->field["MEMBERCD"]);
        }

        //キーの更新チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && $this->membercd != $this->field["MEMBERCD"]) {
            $this->setWarning("MSG308");
            return false;
        }
        if (($this->cmd == "update" || $this->cmd == "delete") && !strlen($this->membercd)) {
            $this->setWarning("MSG308");
            return false;
        }

        //存在チェック
        $db = Query::dbCheckOut();
        $query = knjz520Query::getTeamMemberMst($this->field["MEMBERCD"]);
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
            if (strlen($this->field["MEMBERCD"]) > 7 ) {
                $this->setWarning("MSG915", "(コード) 7バイトまでです。");
                return false;
            }
            if ($this->validate_row_cnt($this->field["NAME"], (40 * 2)) > 1) {
                $this->setWarning("MSG915", "(名称) 全角40文字までです。");
                return false;
            }
            if ($this->validate_row_cnt($this->field["ABBV"], (40 * 2)) > 1) {
                $this->setWarning("MSG915", "(略称) 全角40文字までです。");
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
            knjz520Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertModel() {
        if ($this->validateInput()){
            knjz520Query::getInsertQuery($this);
            $this->membercd = $this->field["MEMBERCD"];
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteModel() {
        if ($this->validateInput()){
            knjz520Query::getDeleteQuery($this);
            unset($this->membercd);
            unset($this->field);
            $this->setMessage("MSG201");
        }
    }
}
?>
