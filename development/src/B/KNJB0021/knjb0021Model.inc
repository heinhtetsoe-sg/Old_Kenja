<?php

require_once('for_php7.php');

class knjb0021Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ

    function knjb0021Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->field = array("YEAR"         =>  VARS::post("YEAR"),
                             "SECTIONCD"    =>  VARS::post("SECTIONCD"),
                             "CHK_FUKA"     =>  VARS::post("CHK_FUKA"),
                             "DBNAME"       =>  VARS::post("DBNAME")
                            );

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }
}
?>
