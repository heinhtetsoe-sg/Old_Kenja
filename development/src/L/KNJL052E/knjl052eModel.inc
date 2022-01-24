<?php

require_once('for_php7.php');

class knjl052eModel extends model {
    var $cmd;
    var $applicantdiv;
    var $testdiv;
    var $examhallcd;
    var $ObjYear;

    function knjl052eModel() {}

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
                $this->interview[$examno]["1"] = VARS::post("OTHER_REMARK1_".$examno);
                $this->interview[$examno]["2"] = VARS::post("OTHER_REMARK2_".$examno);
                $this->interview[$examno]["4"] = VARS::post("OTHER_REMARK4_".$examno);
                $this->interview[$examno]["5"] = VARS::post("OTHER_REMARK5_".$examno);
            }
        }
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        knjl052eQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }

}
?>
