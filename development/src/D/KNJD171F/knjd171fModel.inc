<?php

require_once('for_php7.php');

class knjd171fModel extends Model {
    var $field = array();
    var $cmd;
    var $control;
    var $selectdata;

    function knjd171fModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),
                             "DATE"             =>  VARS::post("DATE"),
                             "PRINT_REGD"       =>  VARS::post("PRINT_REGD"),
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->selectdata = VARS::post("selectdata");
        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイルの読込み
    }
}
?>
