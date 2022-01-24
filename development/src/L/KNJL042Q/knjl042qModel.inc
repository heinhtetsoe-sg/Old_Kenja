<?php

require_once('for_php7.php');

class knjl042qModel extends model {
    var $cmd;
    var $year;
    var $applicantdiv;
    var $testdiv;
    var $field = array();
    var $sort;
    var $judgediv;

    function knjl042qModel() {}

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->year = CTRL_YEAR+1;
        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->test_room             = VARS::post("TEST_ROOM");
        $this->interview_settime     = VARS::post("INTERVIEW_SETTIME");
//        $this->interview_waitingroom = VARS::post("INTERVIEW_WAITINGROOM");
        $this->interview_room        = VARS::post("INTERVIEW_ROOM");
        $this->interview_endtime     = VARS::post("INTERVIEW_ENDTIME");
        $this->interview_group       = VARS::post("INTERVIEW_GROUP");

        $this->hid_examno   = VARS::post("HID_EXAMNO");
    }

    function &getMainModel() {}

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        $this->field = array();
        $keyList = array();
        $keyList = explode(",", $this->hid_examno);
        foreach ($keyList as $key => $examno) {
            if (VARS::post("CHK_DATA"."-".$examno)) {
                $this->field["CHK_DATA"][$examno] = VARS::post("CHK_DATA"."-".$examno);
            }
        }
        if (isset($this->field["CHK_DATA"])) {
            $result = knjl042qQuery::getUpdateQuery($this);
            if ($result) {
                $this->setMessage("MSG201");
            }
        } else {
            $this->setMessage("MSG310");
        }
    }
}
?>
