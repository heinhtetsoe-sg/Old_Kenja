<?php
class knjl438iModel extends Model {
    var $cmd;
    var $examyear;
    var $applicantdiv;
    var $testdiv;

    function init() {
        $this->examyear = CTRL_YEAR + 1;

        $this->cmd              = VARS::request("cmd");
        $this->applicantdiv     = "2"; //高校固定
        $this->testdiv          = VARS::post("TESTDIV");
        $this->outputdiv        = VARS::post("OUTPUT_DIV");
    }

    function &getMainModel() {}
}
?>
