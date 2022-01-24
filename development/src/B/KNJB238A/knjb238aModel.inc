<?php

require_once('for_php7.php');

class knjb238aModel extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ

    function knjb238aModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->field = array("SEMESTER"     =>  VARS::post("SEMESTER"),
                             "SUBCLASSCD"   =>  VARS::post("SUBCLASSCD"),
                             "DATE_FROM"    =>  VARS::post("DATE_FROM"),
                             "DATE_TO"      =>  VARS::post("DATE_TO"),
                             "DBNAME"       =>  VARS::post("DBNAME")
                            );

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }
}
?>
