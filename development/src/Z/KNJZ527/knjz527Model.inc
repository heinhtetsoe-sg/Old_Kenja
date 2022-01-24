<?php

require_once('for_php7.php');

class knjz527Model extends Model {
    var $field = array();
    var $cmd;
    var $checkcd;

    function knjz527Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->checkcd);
        }

        $this->field = array("CHECK_CD"   =>  VARS::post("CHECK_CD"),
                             "CHECK_NAME"     =>  VARS::post("CHECK_NAME")
                             );

        $this->cmd = VARS::request("cmd");

        if(VARS::get("CHECK_CD")) {
            $this->checkcd = VARS::get("CHECK_CD");
        }
    }

    //入力項目チェック
    function validateInput() {

        //必須チェック
        if (trim($this->field["CHECK_CD"]) == "") {
            $this->setWarning("MSG301", "　　　　　　　(　コード　)");
            return false;
        }
        if ($this->cmd == "add" || $this->cmd == "update") {
            if (trim($this->field["CHECK_NAME"]) == "") {
                $this->setWarning("MSG301", "　　　　　　　(　名 称　)");
                return false;
            }
        }

        //頭ゼロ埋め
        if (strlen($this->field["CHECK_CD"]) <= 3) {
            $this->field["CHECK_CD"] = sprintf("%03d", $this->field["CHECK_CD"]);
        }

        //キーの更新チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && ($this->checkcd != $this->field["CHECK_CD"])) {
            $this->setWarning("MSG308");
            return false;
        }
        if (($this->cmd == "update" || $this->cmd == "delete") && !strlen($this->checkcd)) {
            $this->setWarning("MSG308");
            return false;
        }

        //存在チェック
        $db = Query::dbCheckOut();
        $query = knjz527Query::getCheckNameMst($this->field["CHECK_CD"]);
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
            if (strlen($this->field["CHECK_CD"]) > 3 ) {
                $this->setWarning("MSG915", "(コード) 3バイトまでです。");
                return false;
            }

            if ($this->validate_row_cnt($this->field["CHECK_NAME"], (30 * 2)) > 1) {
                $this->setWarning("MSG915", "(名称) 全角30文字までです。");
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
            knjz527Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertModel() {
        if ($this->validateInput()){
            knjz527Query::getInsertQuery($this);
            $this->checkcd = $this->field["CHECK_CD"];
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteModel() {
        if ($this->validateInput()){
            knjz527Query::getDeleteQuery($this);
            unset($this->checkcd);
            unset($this->field);
            $this->setMessage("MSG201");
        }
    }
}
?>
