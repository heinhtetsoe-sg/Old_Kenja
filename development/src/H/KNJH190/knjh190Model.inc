<?php

require_once('for_php7.php');


class knjh190Model extends Model {
    var $field   = array();
    var $cmd;
    var $area_cd = "";        //primary

    function knjh190Model() {}

    function init() {
        $this->field = array("AREA_CD"   =>  VARS::post("AREA_CD"),
                             "AREA_NAME" =>  VARS::post("AREA_NAME"),
                             "UPDATED"   =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");
        if (VARS::request("cmd") == ""){
            unset($this->area_cd);
        }
        $temp = VARS::get("AREA_CD");
        if (isset($temp)){
            $this->area_cd = VARS::get("AREA_CD");
        }
    }

    //入力項目チェック
    function validateInput() {
        //キーのチェック
        if (trim($this->field["AREA_CD"]) == "" && $this->cmd == "add") {
            $this->setWarning("MSG301", "(グループ番号)");
            return false;
        }
        if ($this->area_cd == "" && $this->cmd == "update") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->cmd == "update" && $this->area_cd != $this->field["AREA_CD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        //１レコード取得
        $row = knjh190Query::getRow($this->field["AREA_CD"]);
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
        if (strlen($this->field["AREA_CD"]) > 1 ) {
            $this->setWarning("MSG915", "(グループ名) 1バイトまで");
            return false;
        } elseif (strlen($this->field["AREA_NAME"]) > 45 ) {
            $this->setWarning("MSG915", "(備考) 45バイトまで");
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
            $this->area_cd = $this->field["AREA_CD"];
            $this->setMessage("MSG201");
        }
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //キーの更新チェック
        if ($this->area_cd == "") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->area_cd != $this->field["AREA_CD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        $row = knjh190Query::getRow($this->area_cd);
        if (!is_array($row)){
            $this->setWarning("MSG303");
            return;
        }
        knjh190Query::getDeleteQuery($this->field);
        $this->setMessage("MSG201");
    }

    function &insert() {
        knjh190Query::getInsertQuery($this->field);
    }

    //アップデート時の内部メソッド
    function &update() {
        knjh190Query::getUpdateQuery($this->field);
    }
    
    //変数をクリアします
    function clean() {}
}
?>
