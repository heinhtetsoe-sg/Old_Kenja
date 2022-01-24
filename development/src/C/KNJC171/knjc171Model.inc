<?php

require_once('for_php7.php');


class knjc171Model extends Model {
    var $field      = array();
    var $cmd;
    var $testTable;

    function knjc171Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),
                             "SEMESTER"     =>  VARS::post("SEMESTER"),
                             "DBNAME"       =>  VARS::post("DBNAME"),
                             "DATE"         =>  VARS::post("DATE"),
                             "PERIOD"       =>  VARS::post("PERIOD")
                     );

        $this->cmd         = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function clean()
    {
    }
}
?>
