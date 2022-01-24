<?php

require_once('for_php7.php');

class knjm442mModel extends Model {
    var $field = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $selectdata = array();

    function knjm442mModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->semester     = VARS::post("SEMESTER");
        $this->cmd          = VARS::request("cmd");
        $this->field = array("SEMESTER"     =>  VARS::post("SEMESTER"),
                             "SUBCLASS"     =>  VARS::post("SUBCLASS"),
                             "TEST_DAY"     =>  VARS::post("TEST_DAY"),
                             "TEST_HOUR"    =>  VARS::post("TEST_HOUR"),
                             "TEST_MINUTE"  =>  VARS::post("TEST_MINUTE")
                            );

        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll();  //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput() {
    }
}
?>
