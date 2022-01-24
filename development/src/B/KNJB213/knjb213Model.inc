<?php

require_once('for_php7.php');

class knjb213Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ

    function knjb213Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->field = array("YEAR"     =>  VARS::post("YEAR"),
                             "JOBCD"    =>  VARS::post("JOBCD"),
                             "DBNAME"   =>  VARS::post("DBNAME")
                            );

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }
}
?>
