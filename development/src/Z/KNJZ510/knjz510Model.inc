<?php

require_once('for_php7.php');

class knjz510Model extends Model {
    var $field = array();
    var $cmd;
    var $challengecd;

    function knjz510Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->challengecd);
        }

        $this->field = array("CHALLENGECD"      =>  VARS::post("CHALLENGECD"),
                             "NAME"             =>  VARS::post("NAME"),
                             "ABBV"             =>  VARS::post("ABBV"),
                             "POPULAR_NAME"     =>  VARS::post("POPULAR_NAME"),
                             "REMARK"           =>  VARS::post("REMARK")
                             );

        $this->cmd = VARS::request("cmd");

        if(VARS::get("CHALLENGECD")) {
            $this->challengecd = VARS::get("CHALLENGECD");
        }
    }

    //入力項目チェック
    function validateInput() {

        //必須チェック
        if (trim($this->field["CHALLENGECD"]) == "") {
            $this->setWarning("MSG301", "　　　　　　　(　コード　)");
            return false;
        }
        if ($this->cmd == "add" || $this->cmd == "update") {
            if (trim($this->field["NAME"]) == "") {
                $this->setWarning("MSG301", "　　　　　　　(　名 称　)");
                return false;
            }
        }

        //頭ゼロ埋め
        if (strlen($this->field["CHALLENGECD"]) <= 5) {
            $this->field["CHALLENGECD"] = sprintf("%05d", $this->field["CHALLENGECD"]);
        }

        //キーの更新チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && ($this->challengecd != $this->field["CHALLENGECD"])) {
            $this->setWarning("MSG308");
            return false;
        }
        if (($this->cmd == "update" || $this->cmd == "delete") && !strlen($this->challengecd)) {
            $this->setWarning("MSG308");
            return false;
        }

        //存在チェック
        $db = Query::dbCheckOut();
        $query = knjz510Query::getChallengedNameMst($this->field["CHALLENGECD"]);
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
            if (strlen($this->field["CHALLENGECD"]) > 5 ) {
                $this->setWarning("MSG915", "(コード) 5バイトまでです。");
                return false;
            }
            if ($this->validate_row_cnt($this->field["NAME"], (50 * 2)) > 1) {
                $this->setWarning("MSG915", "(名称) 全角50文字までです。");
                return false;
            }
            if ($this->validate_row_cnt($this->field["ABBV"], (10 * 2)) > 1) {
                $this->setWarning("MSG915", "(略称) 全角10文字までです。");
                return false;
            }
            if ($this->validate_row_cnt($this->field["POPULAR_NAME"], (50 * 2)) > 1) {
                $this->setWarning("MSG915", "(俗名・旧名) 全角50文字までです。");
                return false;
            }
            if ($this->validate_row_cnt($this->field["REMARK"], (30 * 2)) > 9) {
                $this->setWarning("MSG915", "(備考) 9行までです。");
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
            knjz510Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertModel() {
        if ($this->validateInput()){
            knjz510Query::getInsertQuery($this);
            $this->challengecd = $this->field["CHALLENGECD"];
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteModel() {
        if ($this->validateInput()){
            knjz510Query::getDeleteQuery($this);
            unset($this->challengecd);
            unset($this->field);
            $this->setMessage("MSG201");
        }
    }
}
?>
