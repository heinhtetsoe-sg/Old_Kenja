<?php

require_once('for_php7.php');


class knjl017rModel extends model
{
    var $cmd;
    var $year;
    var $applicantdiv;
    var $testdiv;
    var $shdiv;
    var $field = array();
    var $sort;
    var $naidaku;

    function knjl017rModel() {
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
        $this->naidaku      = VARS::post("NAIDAKU");
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
            $result = knjl017rQuery::getUpdateQuery($this);
            if ($result) {
                $this->setMessage("MSG201");
            }
        } else {
            $this->setMessage("MSG310");
        }
    }
}
?>
