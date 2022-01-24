<?php

require_once('for_php7.php');


class knjd643Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;
        
    function knjd643Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }
    function init()
    {               
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),     //年度
                             "SEMESTER"     =>  VARS::post("SEMESTER"), //学期
                             "WISH_RANK"    =>  VARS::post("WISH_RANK"),//推薦希望順位
                             "DBNAME"       =>  VARS::post("DBNAME")    //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");
    }

    function clean()
    {
    }
}
?>
