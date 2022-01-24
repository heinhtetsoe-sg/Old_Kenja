<?php

require_once('for_php7.php');


class knjh173Model extends Model {
    var $field      = array();
    var $cmd;
    var $go_home_group_no = "";        //primary

    function knjh173Model() {}

    function init() {
        $this->field = array("HOUSE_GROUP_CD"   =>  (VARS::post("HOUSE_GROUP_CD") == "") ? "" : sprintf("%03d",VARS::post("HOUSE_GROUP_CD")),
                             "HOUSE_GROUP_NAME" =>  VARS::post("HOUSE_GROUP_NAME"),
                             "REMARK1"            =>  VARS::post("REMARK1"),
                             "UPDATED"            =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");
        if (VARS::request("cmd") == ""){
            unset($this->go_home_group_no);
        }
        $temp = VARS::get("HOUSE_GROUP_CD");
        if (isset($temp)){
            $this->go_home_group_no = VARS::get("HOUSE_GROUP_CD");
        }
    }

    //入力項目チェック
    function validateInput() {
        //キーのチェック
        if (trim($this->field["HOUSE_GROUP_CD"]) == "" && $this->cmd == "add") {
            $this->setWarning("MSG301", "(ホームコード)");
            return false;
        }
        if ($this->go_home_group_no == "" && $this->cmd == "update") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->cmd == "update" && $this->go_home_group_no != $this->field["HOUSE_GROUP_CD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        if (trim($this->field["HOUSE_GROUP_NAME"]) == "") {
            $this->setWarning("MSG301", "(ホームグループ名)");
            return false;
        }
        //１レコード取得
        $row = knjh173Query::getRow($this->field["HOUSE_GROUP_CD"]);
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918", "(ホームコード)");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
            $this->setWarning("MSG307");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["HOUSE_GROUP_CD"]) > 3 ) {
            $this->setWarning("MSG915", "(ホームコード) 3バイトまで");
            return false;
        } elseif (strlen($this->field["HOUSE_GROUP_NAME"]) > 60 ) {
            $this->setWarning("MSG915", "(ホームグループ名) 60バイトまで");
            return false;
        } elseif (strlen($this->field["REMARK1"]) > 150 ) {
            $this->setWarning("MSG915", "(備考) 150バイトまで");
            return false;
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
            $this->go_home_group_no = $this->field["HOUSE_GROUP_CD"];
            $this->setMessage("MSG201");
        }
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //キーの更新チェック
        if ($this->go_home_group_no == "") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->go_home_group_no != $this->field["HOUSE_GROUP_CD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        $row = knjh173Query::getRow($this->go_home_group_no);
        if (!is_array($row)){
            $this->setWarning("MSG303");
            return;
        }
        knjh173Query::getDeleteQuery($this->field);
        $this->setMessage("MSG201");
    }

    function &insert() {
        knjh173Query::getInsertQuery($this->field);
    }

    //アップデート時の内部メソッド
    function &update() {
        knjh173Query::getUpdateQuery($this->field);
    }
    
    //変数をクリアします
    function clean() {}
}
?>
