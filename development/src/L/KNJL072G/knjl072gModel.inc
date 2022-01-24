<?php

require_once('for_php7.php');


class knjl072gModel extends model
{
    var $cmd;
    var $year;
    var $applicantdiv;
    var $testdiv;
    var $shdiv;
    var $examcourse;
    var $field = array();
    var $sort;
    var $special_reason_div;

    var $judgediv;

    function knjl072gModel() {
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->year = CTRL_YEAR+1;
        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->shdiv        = strlen(VARS::post("SHDIV")) ? VARS::post("SHDIV") : VARS::post("HID_SHDIV");
        $this->examcourse   = strlen(VARS::post("EXAMCOURSE")) ? VARS::post("EXAMCOURSE") : VARS::post("HID_EXAMCOURSE");
        $this->sort         = VARS::post("SORT");
        $this->special_reason_div = VARS::post("SPECIAL_REASON_DIV");

        $this->judgediv     = VARS::post("JUDGEDIV");

        $this->hid_examno   = VARS::post("HID_EXAMNO");
    }

    function &getMainModel() {
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        $this->field = array();
        $keyList = array();
        $keyList = explode(",", $this->hid_examno);
        foreach ($keyList as $key => $val) {
            $tmp = explode("-", $val);
            $receptno = $tmp[0];
            $examno = $tmp[1];
            $this->field["CHK_DATA"][$receptno] = VARS::post("CHK_DATA"."-".$receptno);
        }
        if (isset($this->field["CHK_DATA"])) {
            $result = knjl072gQuery::getUpdateQuery($this);
            if ($result) {
                $this->setMessage("MSG201");
            }
        } else {
            $this->setMessage("MSG310");
        }
    }
}
?>
