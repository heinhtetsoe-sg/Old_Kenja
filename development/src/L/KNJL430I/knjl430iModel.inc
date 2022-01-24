<?php
class knjl430iModel extends Model {
    var $cmd;
    var $examyear;
    var $applicantdiv;
    var $testdiv;

    function init() {
        $this->examyear = CTRL_YEAR + 1;

        $this->cmd              = VARS::request("cmd");
        $this->applicantdiv     = "2"; //高校固定
        $this->testdiv          = VARS::post("TESTDIV");

    }

    function &getMainModel() {}
}
?>
