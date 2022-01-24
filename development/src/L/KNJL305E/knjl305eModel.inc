<?php

require_once('for_php7.php');

class knjl305eModel extends Model {
    var $cmd;
    var $examyear;
    var $applicantdiv;
    var $testdiv;
    var $outputorder;

    function knjl305eModel(){}

    function init() {

        $this->cmd          = VARS::request("cmd");

        $this->examyear     = CTRL_YEAR + 1;
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->testdiv      = VARS::post("TESTDIV");
        $this->outputorder  = VARS::post("OUTPUT_ORDER");
    }
}
?>
