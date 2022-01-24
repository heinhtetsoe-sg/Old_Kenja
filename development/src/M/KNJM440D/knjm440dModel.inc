<?php

require_once('for_php7.php');

class knjm440dModel extends Model {
    var $field  = array();
    var $cmd;
    var $control;

    function knjm440dModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd  = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        $this->field["SEMESTER"]  = VARS::post("SEMESTER");
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->field["SEMESTER"] == "") {
            $this->setWarning("MSG301");
            return false;
        }

        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->setMessage("MSG201");
        }
    }

    //インサート時の内部メソッド
    function &insert() {
        knjm440dQuery::getInsertQuery($this);
    }
}
?>
