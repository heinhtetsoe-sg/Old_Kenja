<?php

require_once('for_php7.php');

class knjl303eModel extends Model {
    var $cmd;
    var $examyear;
    var $applicantdiv;
    var $testdiv;
    var $testscoreprint;

    function knjl303eModel(){}

    function init() {

        $this->cmd            = VARS::request("cmd");

        $this->examyear       = CTRL_YEAR + 1;
        $this->applicantdiv   = VARS::post("APPLICANTDIV");
        $this->testdiv        = VARS::post("TESTDIV");
        $this->testscoreprint = VARS::post("TESTSCORE_PRINT");
    }

}
?>
