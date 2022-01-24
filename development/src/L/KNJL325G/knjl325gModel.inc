<?php

require_once('for_php7.php');

class knjl325gModel extends Model {
    var $field = array();
    var $ObjYear;
    var $cmd;

    function knjl325gModel() {
    }

    function init() {
        $this->ObjYear = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),          //入試区分
                             "ZAISOTU"      =>  VARS::post("ZAISOTU"),          //在校生卒業生区分
                             "TWINS"        =>  VARS::post("TWINS"),            //双生児在校生
                             "DBNAME"       =>  VARS::post("DBNAME")            //DB変更に備えて
                            );
        $this->cmd = VARS::request("cmd");
    }
}
?>
