<?php

require_once('for_php7.php');

class knjz511Model extends Model {
    var $field = array();
    var $cmd;
    var $namecd;

    function knjz511Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->namecd);
        }

        $this->field = array("NAMECD"            =>  VARS::post("NAMECD"),
                             "TRAINING_CONTENTS" =>  VARS::post("TRAINING_CONTENTS")
                             );

        $this->cmd = VARS::request("cmd");

        if(VARS::get("NAMECD")) {
            $this->namecd = VARS::get("NAMECD");
        }
    }

    //入力項目チェック
    function validateInput() {

        //必須チェック
        if (trim($this->field["NAMECD"]) == "") {
            $this->setWarning("MSG301", "　　　　(　訓練内容コード　)");
            return false;
        }
        if ($this->cmd == "add" || $this->cmd == "update") {
            if (trim($this->field["TRAINING_CONTENTS"]) == "") {
                $this->setWarning("MSG301", "　　　　　　(　訓練内容　)");
                return false;
            }
        }

        //頭ゼロ埋め
        if (strlen($this->field["NAMECD"]) <= 3) {
            $this->field["NAMECD"] = sprintf("%03d", $this->field["NAMECD"]);
        }

        //キーの更新チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && ($this->namecd != $this->field["NAMECD"])) {
            $this->setWarning("MSG308");
            return false;
        }
        if (($this->cmd == "update" || $this->cmd == "delete") && !strlen($this->namecd)) {
            $this->setWarning("MSG308");
            return false;
        }

        //存在チェック
        $db = Query::dbCheckOut();
        $query = knjz511Query::getChallengedTrainingMst($this->field["NAMECD"]);
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
            if (strlen($this->field["NAMECD"]) > 3 ) {
                $this->setWarning("MSG915", "(訓練内容コード) 3バイトまでです。");
                return false;
            }
            if ($this->validate_row_cnt($this->field["TRAINING_CONTENTS"], (10 * 2)) > 1) {
                $this->setWarning("MSG915", "(訓練内容) 全角10文字までです。");
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
            knjz511Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertModel() {
        if ($this->validateInput()){
            knjz511Query::getInsertQuery($this);
            $this->namecd = $this->field["NAMECD"];
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteModel() {
        if ($this->validateInput()){
            knjz511Query::getDeleteQuery($this);
            unset($this->namecd);
            unset($this->field);
            $this->setMessage("MSG201");
        }
    }
}
?>
