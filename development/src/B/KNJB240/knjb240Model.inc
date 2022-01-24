<?php

require_once('for_php7.php');

class knjb240Model extends Model {
    var $field = array();
    var $cmd;
    var $selectdata;
    var $control;   //コントロールマスタ

    function knjb240Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->field = array("SEMESTER"     =>  VARS::post("SEMESTER"),
                             "SUBCLASSCD"   =>  VARS::post("SUBCLASSCD"),
                             "S_MONTH"      =>  VARS::post("S_MONTH"),
                             "E_MONTH"      =>  VARS::post("E_MONTH"),
                             "DBNAME"       =>  VARS::post("DBNAME")
                            );

        $this->selectdata = VARS::post("selectdata");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }
}
?>
