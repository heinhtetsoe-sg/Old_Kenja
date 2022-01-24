<?php

require_once('for_php7.php');

class knjl322dModel extends Model {
    var $cmd;
    var $examyear;
    var $applicantdiv;
    var $testdiv;

    function knjl322dModel(){}

    function init() {

        $this->cmd          = VARS::request("cmd");

        $this->examyear     = CTRL_YEAR + 1;
        $this->applicantdiv = "1";  //固定値

        $this->testdiv       = VARS::post("TESTDIV");
        $this->noticeType    = VARS::post("NOTICE_TYPE");
        $this->outputDiv     = VARS::post("OUTPUT_DIV");
        $this->noticeDate    = VARS::post("NOTICE_DATE");
        $this->transferDate1 = VARS::post("TRANSFER_DATE1");
        $this->transferDate2 = VARS::post("TRANSFER_DATE2");
        $this->decisionDate  = VARS::post("DECISION_DATE");
    }
}
?>
