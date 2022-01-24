<?php

require_once('for_php7.php');


class knjl072cModel extends model
{
    var $cmd;
    var $year;
    var $applicantdiv;
    var $testdiv;
    var $shdiv;
    var $field = array();
    var $sort;

    var $judgediv;
    var $suc_course;

    function knjl072cModel() {
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->year = CTRL_YEAR+1;
        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->shdiv        = strlen(VARS::post("SHDIV")) ? VARS::post("SHDIV") : VARS::post("HID_SHDIV");
        $this->sort         = VARS::post("SORT");

        $this->judgediv     = VARS::post("JUDGEDIV");
        $this->suc_course   = VARS::post("SUC_COURSE");

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
            $result = knjl072cQuery::getUpdateQuery($this);
            if ($result) {
                $this->setMessage("MSG201");
            }
        } else {
            $this->setMessage("MSG310");
        }
    }
}
?>
