<?php

require_once('for_php7.php');

class knja191Model extends Model {
    var $field = array();
    var $cmd;
    var $control;

    function knja191Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),
                             "CHOICE"           =>  VARS::post("CHOICE"),
                             "HR_CLASS_TYPE"    =>  VARS::post("HR_CLASS_TYPE"),
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイルの読込み
    }
}
?>
