<?php

require_once('for_php7.php');

class knjb252Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ

    function knjb252Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),
                             "DATE_FROM"        =>  VARS::post("DATE_FROM"),
                             "DATE_TO"          =>  VARS::post("DATE_TO"),
                             "DBNAME"           =>  VARS::post("DBNAME")
                            );

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }
}
?>
