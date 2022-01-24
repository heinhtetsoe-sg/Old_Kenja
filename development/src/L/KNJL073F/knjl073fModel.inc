<?php

require_once('for_php7.php');


class knjl073fModel extends model
{
    var $cmd;
    var $year;
    var $applicantdiv;
    var $testdiv;
    var $testdiv0;
    var $exam_type;
    var $field = array();
    var $examcourse1;
    var $sort;
    var $avg_from;
    var $avg_to;
    var $target;
    var $special_reason_div;

    var $judgediv;
    var $suc_course;

    function knjl073fModel() {
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->year = CTRL_YEAR+1;
        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->testdiv0     = strlen(VARS::post("TESTDIV0")) ? VARS::post("TESTDIV0") : VARS::post("HID_TESTDIV0");
        $this->exam_type    = "1";
        $this->examcourse1  = strlen(VARS::post("EXAMCOURSE1")) ? VARS::post("EXAMCOURSE1") : VARS::post("HID_EXAMCOURSE1");
        $this->sort         = VARS::post("SORT");
        $this->avg_from     = VARS::post("AVG_FROM");
        $this->avg_to       = VARS::post("AVG_TO");
        $this->special_reason_div = VARS::post("SPECIAL_REASON_DIV");

        $this->target         = VARS::post("TARGET");

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
            $result = knjl073fQuery::getUpdateQuery($this);
            if ($result) {
                $this->setMessage("MSG201");
            }
        } else {
            $this->setMessage("MSG310");
        }
    }
}
?>
