<?php

require_once('for_php7.php');

class knjm440wModel extends Model {
    var $field  = array();
    var $cmd;
    var $control;

    function knjm440wModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd  = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        
        $this->field["SEMESTER"]  = VARS::post("SEMESTER");
        $this->field["TESTCD"]  = VARS::post("TESTCD");
        
        if($this->Properties["useTsushinSemesKonboHyoji"] == '1') {
            $this->field["TESTCD"] = '990009';
        }
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        
        if($this->Properties["useTsushinSemesKonboHyoji"] == '1'){
            if ($this->field["SEMESTER"] == "") {
                $this->setWarning("MSG301");
                return false;
            }
        } else {
            if ($this->field["SEMESTER"] == "" || $this->field["TESTCD"] == "") {
                $this->setWarning("MSG301");
                return false;
            }
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
        knjm440wQuery::getInsertQuery($this);
    }
}
?>
