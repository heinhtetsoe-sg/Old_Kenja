<?php
class knjl085iModel extends model {
    var $cmd;
    var $ObjYear;
    var $applicantdiv;
    var $testdiv;
    var $exam_type;
    var $examhallcd;

    function knjl085iModel() {}

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->testdiv);
        }

        $this->cmd = VARS::request("cmd");
        $this->ObjYear      = CTRL_YEAR+1;
        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->exam_type    = 1; //固定値
        $this->examhallcd   = strlen(VARS::post("EXAMHALLCD")) ? VARS::post("EXAMHALLCD") : VARS::post("HID_EXAMHALLCD");

        $hid_examno = VARS::post("HID_EXAMNO");
        $examnoArray = explode(",", $hid_examno);
        $this->interview = array();
        if (count($examnoArray) > 0) {
            foreach ($examnoArray as $key => $examno) {
                $this->interview[$examno]["INTERVIEW_A"] = VARS::post("INTERVIEW_A-{$examno}");
                $this->interview[$examno]["ATTEND_FLG"]  = VARS::post("ATTEND_FLG-{$examno}");
            }
        }
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        knjl085iQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }

}
?>
