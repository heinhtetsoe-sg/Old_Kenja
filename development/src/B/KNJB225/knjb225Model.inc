<?php

require_once('for_php7.php');

class knjb225Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ

    function knjb225Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->field = array("SEMESTER"     =>  VARS::post("SEMESTER"),
                             "GRADE_COURSE" =>  VARS::post("GRADE_COURSE"),
                             "OUTPUT"       =>  VARS::post("OUTPUT"),
                             "DBNAME"       =>  VARS::post("DBNAME")
                            );

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }
}
?>
