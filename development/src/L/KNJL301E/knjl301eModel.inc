<?php

require_once('for_php7.php');

class knjl301eModel extends Model {
    var $cmd;
    var $examyear;
    var $applicantdiv;
    var $testdiv;

    function knjl301eModel(){}

    function init() {

        $this->cmd          = VARS::request("cmd");

        $this->examyear     = CTRL_YEAR + 1;
        $this->applicantdiv = VARS::post("APPLICANTDIV");

        $this->testdiv      = VARS::post("TESTDIV");
    }
}
?>
