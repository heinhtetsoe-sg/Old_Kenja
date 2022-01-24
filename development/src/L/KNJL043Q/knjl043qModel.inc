<?php

require_once('for_php7.php');

class knjl043qModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;

    function knjl043qModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV")
                            );

        $this->cmd = VARS::request("cmd");
    }
}
?>