<?php

require_once('for_php7.php');

class knjl051eModel extends model {
    var $cmd;
    var $applicantdiv;
    var $testdiv;
    var $examhallcd;
    var $s_examno;
    var $e_examno;
    var $ObjYear;

    function knjl051eModel() {}

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->examhallcd);
        }

        $this->ObjYear      = CTRL_YEAR+1;
        $this->applicantdiv = "1";  //固定値
        $this->exam_type    = "1";  //固定値

        $this->cmd = VARS::request("cmd");

        $this->applicantdiv     = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv          = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->examhallcd       = strlen(VARS::post("EXAMHALLCD")) ? VARS::post("EXAMHALLCD") : VARS::post("HID_EXAMHALLCD");

        $this->interview = array();
        if (isset($this->examnoArray)) {
            foreach ($this->examnoArray as $key => $examno) {
                $this->interview[$examno]["A"] = VARS::post("INTERVIEW_A_".$examno);
                $this->interview[$examno]["B"] = VARS::post("INTERVIEW_B_".$examno);
            }
        }
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        knjl051eQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }

}
?>
