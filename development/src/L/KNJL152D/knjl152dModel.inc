<?php

require_once('for_php7.php');

class knjl152dModel extends Model {
    var $cmd;
    var $examyear;
    var $applicantdiv;
    var $testdiv;

    function knjl152dModel(){}

    function init() {

        $this->cmd          = VARS::request("cmd");

        $this->examyear     = VARS::post("YEAR");
        $this->applicantdiv = "1";  //固定値

        $this->testdiv       = VARS::post("TESTDIV");
    }
}
?>
