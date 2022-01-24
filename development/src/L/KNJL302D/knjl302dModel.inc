<?php

require_once('for_php7.php');

class knjl302dModel extends Model {
    var $cmd;
    var $examyear;
    var $applicantdiv;
    var $testdiv;

    function knjl302dModel(){}

    function init() {

        $this->cmd          = VARS::request("cmd");

        $this->examyear     = CTRL_YEAR + 1;
        $this->applicantdiv = "1";  //固定値

        $this->testdiv      = VARS::post("TESTDIV");
    }
}
?>
