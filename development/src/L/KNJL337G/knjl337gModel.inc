<?php

require_once('for_php7.php');

class knjl337gModel extends Model {
    var $field = array();
    var $ObjYear;
    var $cmd;

    function knjl337gModel() {
    }

    function init() {
        $this->ObjYear = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"), //入試制度
                             "DBNAME"       =>  VARS::post("DBNAME"),       //DB変更に備えて
                            );
        $this->cmd = VARS::request("cmd");
    }
}
?>
