<?php

require_once('for_php7.php');

class knjl270gModel extends model {
    var $cmd;
    var $year;
    var $applicantdiv;
    var $testdiv;
    var $shdiv;
    var $examcourse;
    var $field = array();

    var $judgement;

    function knjl270gModel() {
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->year = CTRL_YEAR+1;
        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv      = strlen(VARS::post("TESTDIV"))      ? VARS::post("TESTDIV")      : VARS::post("HID_TESTDIV");
        $this->shdiv        = strlen(VARS::post("SHDIV"))        ? VARS::post("SHDIV")        : VARS::post("HID_SHDIV");
        $this->examcourse   = strlen(VARS::post("EXAMCOURSE"))   ? VARS::post("EXAMCOURSE")   : VARS::post("HID_EXAMCOURSE");

        $this->judgement    = VARS::post("JUDGEMENT");

        $this->hid_examno   = VARS::post("HID_EXAMNO");
    }

    function &getMainModel() {
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        $this->field = array();
        $keyList = array();
        $keyList = explode(",", $this->hid_examno);
        foreach ($keyList as $key => $examno) {
            $this->field["CHK_DATA"][$examno] = VARS::post("CHK_DATA"."-".$examno);
        }
        if (isset($this->field["CHK_DATA"])) {
            $result = knjl270gQuery::getUpdateQuery($this);
            if ($result) {
                $this->setMessage("MSG201");
            }
        } else {
            $this->setMessage("MSG310");
        }
    }
}
?>
