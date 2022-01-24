<?php

require_once('for_php7.php');

class knjl013aModel extends model {
    var $cmd;
    var $applicantdiv;
    var $testdiv;
    var $examType;
    var $ObjYear;

    function knjl013aModel() {}

    function init() {
        $this->ObjYear  = CTRL_YEAR+1;
        $this->examType = 1;    //固定

        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->testdiv      = VARS::post("TESTDIV");
        $this->hid_ReceptNo = VARS::post("HID_RECEPTNO");

        $this->cmd = VARS::request("cmd");

        //志願者入力画面から戻る時セットされるパラメータ
        $this->send_examno = VARS::post("SEND_EXAMNO");
    }

    function &getMainModel() {}
}
?>
