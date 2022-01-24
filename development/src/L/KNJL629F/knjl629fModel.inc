<?php

require_once('for_php7.php');

class knjl629fModel extends Model {
    var $cmd;
    var $examyear;
    var $applicantdiv;
    var $testdiv;
    var $print_date;

    function init() {
        $this->examyear = CTRL_YEAR + 1;

        $this->cmd          = VARS::request("cmd");
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->testdiv      = VARS::post("TESTDIV");
        $this->print_date   = VARS::post("PRINT_DATE");
    }

    function &getMainModel()
    {
    }
}
?>
