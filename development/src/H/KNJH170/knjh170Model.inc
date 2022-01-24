<?php

require_once('for_php7.php');


class knjh170Model extends Model {
    var $field      = array();
    var $cmd;
    var $go_home_group_no = "";        //primary

    function knjh170Model() {}

    function init() {
        $this->field = array("GO_HOME_GROUP_NO"   =>  (VARS::post("GO_HOME_GROUP_NO") == "") ? "" : sprintf("%02d",VARS::post("GO_HOME_GROUP_NO")),
                             "GO_HOME_GROUP_NAME" =>  VARS::post("GO_HOME_GROUP_NAME"),
                             "REMARK1"            =>  VARS::post("REMARK1"),
                             "MEETING_PLACE"      =>  VARS::post("MEETING_PLACE"),
                             "UPDATED"            =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");
        if (VARS::request("cmd") == ""){
            unset($this->go_home_group_no);
        }
        $temp = VARS::get("GO_HOME_GROUP_NO");
        if (isset($temp)){
            $this->go_home_group_no = VARS::get("GO_HOME_GROUP_NO");
        }
    }

    //入力項目チェック
    function validateInput() {
        //キーのチェック
        if (trim($this->field["GO_HOME_GROUP_NO"]) == "" && $this->cmd == "add") {
            $this->setWarning("MSG301", "(グループ番号)");
            return false;
        }
        if ($this->go_home_group_no == "" && $this->cmd == "update") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->cmd == "update" && $this->go_home_group_no != $this->field["GO_HOME_GROUP_NO"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        if (trim($this->field["GO_HOME_GROUP_NAME"]) == "") {
            $this->setWarning("MSG301", "(グループ名)");
            return false;
        }
        //１レコード取得
        $row = knjh170Query::getRow($this->field["GO_HOME_GROUP_NO"]);
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918", "(グループ番号)");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
            $this->setWarning("MSG307");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["GO_HOME_GROUP_NAME"]) > 60 ) {
            $this->setWarning("MSG915", "(グループ名) 60バイトまで");
            return false;
        } elseif (strlen($this->field["REMARK1"]) > 150 ) {
            $this->setWarning("MSG915", "(備考) 150バイトまで");
            return false;
        } elseif (strlen($this->field["MEETING_PLACE"]) > 45 ) {
            $this->setWarning("MSG915", "(集合場所) 45バイトまで");
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
            $this->go_home_group_no = $this->field["GO_HOME_GROUP_NO"];
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
        if ($this->go_home_group_no != $this->field["GO_HOME_GROUP_NO"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        $row = knjh170Query::getRow($this->go_home_group_no);
        if (!is_array($row)){
            $this->setWarning("MSG303");
            return;
        }
        knjh170Query::getDeleteQuery($this->field);
        $this->setMessage("MSG201");
    }

    function &insert() {
        knjh170Query::getInsertQuery($this->field);
    }

    //アップデート時の内部メソッド
    function &update() {
        knjh170Query::getUpdateQuery($this->field);
    }
    
    //変数をクリアします
    function clean() {}
}
?>
