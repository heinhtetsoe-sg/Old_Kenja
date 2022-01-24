<?php

require_once('for_php7.php');

class knjb247Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ

    function knjb247Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->field = array("SEMESTER"     =>  VARS::post("SEMESTER"),
                             "GRADE"        =>  VARS::post("GRADE"),
                             "DATE_FROM"    =>  VARS::post("DATE_FROM"),
                             "DATE_TO"      =>  VARS::post("DATE_TO"),
                             "DBNAME"       =>  VARS::post("DBNAME")
                            );

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }
}
?>
