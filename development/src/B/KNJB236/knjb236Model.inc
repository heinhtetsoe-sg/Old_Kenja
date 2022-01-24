<?php

require_once('for_php7.php');

class knjb236Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ

    function knjb236Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->field = array("SEMESTER"     =>  VARS::post("SEMESTER"),
                             "PTRN_TITLE"   =>  VARS::post("PTRN_TITLE"),
                             "DATE"         =>  VARS::post("DATE"),
                             "DBNAME"       =>  VARS::post("DBNAME")
                            );

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }
}
?>
