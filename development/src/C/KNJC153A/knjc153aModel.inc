<?php

require_once('for_php7.php');


class knjc153aModel extends Model {
    var $field      = array();
    var $cmd;
    var $testTable;

    function knjc153aModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),
                             "SEMESTER"     =>    VARS::post("SEMESTER"),
                             "SUBCLASSCD"   =>  VARS::post("SUBCLASSCD"),
                             "DBNAME"       =>  VARS::post("DBNAME"),
                             "SDATE"        =>  VARS::post("SDATE"),
                             "DATE"         =>  VARS::post("DATE")
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
