<?php

require_once('for_php7.php');

class knjl303dModel extends Model {
    var $cmd;
    var $examyear;
    var $applicantdiv;
    var $testdiv;

    function knjl303dModel(){}

    function init() {

        $this->cmd          = VARS::request("cmd");

        $this->examyear     = CTRL_YEAR + 1;
        $this->applicantdiv = "1";  //固定値

        $this->testdiv      = VARS::post("TESTDIV");
        $this->form         = VARS::post("FORM");
        $this->sort         = VARS::post("SORT");
    }
}
?>
