<?php

require_once('for_php7.php');


class knjl071rModel extends model
{
    var $cmd;
    var $year;
    var $applicantdiv;
    var $testdiv;
    var $shdiv;
    var $field = array();
    var $sort;
    var $judgement;

    function knjl071rModel() {
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->fixApplicantDiv = '1';
        if (SCHOOLKIND == 'J') {
            $this->fixApplicantDiv = '2';
        }

        $this->year         = CTRL_YEAR + 1;
        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->shdiv        = strlen(VARS::post("SHDIV")) ? VARS::post("SHDIV") : VARS::post("HID_SHDIV");
        $this->sort         = VARS::post("SORT");
        $this->judgement    = VARS::post("JUDGEMENT");
        $this->hid_receptno = VARS::post("HID_RECEPTNO");
    }

    function &getMainModel() {
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        $this->field = array();
        $keyList = array();
        $keyList = explode(",", $this->hid_receptno);
        foreach ($keyList as $key => $receptno) {
            $this->field["CHK_DATA"][$receptno] = VARS::post("CHK_DATA"."-".$receptno);
        }
        if (isset($this->field["CHK_DATA"])) {
            $result = knjl071rQuery::getUpdateReceptQuery($this);
            $result = knjl071rQuery::getUpdateBaseQuery($this);
            if ($result) {
                $this->setMessage("MSG201");
            }
        } else {
            $this->setMessage("MSG310");
        }
    }
}
?>
