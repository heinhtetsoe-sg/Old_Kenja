<?php

require_once('for_php7.php');

class knjm441wModel extends Model {
    var $field = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $selectdata = array();

    function knjm441wModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->semester     = VARS::post("SEMESTER");
        $this->hrClass      = VARS::post("GRADE_HR_CLASS");
        $this->cmd          = VARS::request("cmd");
    
        $this->field = array("SEMESTER"     =>  VARS::post("SEMESTER"),
                             "TESTCD"       =>  VARS::post("TESTCD"),
                             "SUBCLASS"     =>  VARS::post("SUBCLASS")
                            );

        $this->cmd = VARS::request("cmd");

        $this->selectdata = explode(",", VARS::post("selectdata"));

        $this->getPropertiesAll();  //プロパティファイルの読込み
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->field["SEMESTER"] == "") {
            $this->setWarning("MSG301");
            return false;
        }
        if ($this->field["SUBCLASS"] == "") {
            $this->setWarning("MSG301");
            return false;
        }
        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            if ($this->cmd != "updateprint") {
                $this->setMessage("MSG201");
            }
        }
    }

    //アップデート時の内部メソッド
    function &update() {
        knjm441wQuery::getUpdateQuery($this);
    }
}
?>
