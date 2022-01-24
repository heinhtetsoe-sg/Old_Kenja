<?php
class knjl390iModel extends Model {
    var $cmd;
    var $examyear;
    var $applicantdiv;
    var $testdiv;

    function init() {
        $this->examyear = CTRL_YEAR + 1;

        $this->cmd              = VARS::request("cmd");
        $this->applicantdiv     = "1"; //中学固定
        $this->testdiv          = VARS::post("TESTDIV");

    }

    function &getMainModel() {}
}
?>
