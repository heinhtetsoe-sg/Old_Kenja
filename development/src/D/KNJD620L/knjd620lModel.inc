<?php

require_once('for_php7.php');


class knjd620lModel extends Model {
    var $field      = array();
    var $cmd;
    var $testTable;

    function knjd620lModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),
                             "SEMESTER"     =>    VARS::post("SEMESTER"),
                             "SUBCLASSCD"   =>  VARS::post("SUBCLASSCD"),
                             "PRINT_DIV"    =>  VARS::post("PRINT_DIV"),
                             "PRINT_KEKKA0" =>  VARS::post("PRINT_KEKKA0"),
                             "PRINT_TANNIN" =>  VARS::post("PRINT_TANNIN"),
                             "DBNAME"       =>  VARS::post("DBNAME"),
                             "DATE"         =>  VARS::post("DATE")
                     );

        $this->cmd         = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
    }

    function clean()
    {
    }
}
?>
