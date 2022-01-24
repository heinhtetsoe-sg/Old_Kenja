<?php

require_once('for_php7.php');

class knjb233Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ

    function knjb233Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->field = array("SEMESTER"     =>  VARS::post("SEMESTER"),
                             "MONTH"        =>  VARS::post("MONTH"),
                             "FORM"         =>  VARS::post("FORM"),
                             "DBNAME"       =>  VARS::post("DBNAME")
                            );

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }
}
?>
