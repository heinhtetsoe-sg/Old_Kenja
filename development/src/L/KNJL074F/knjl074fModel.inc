<?php

require_once('for_php7.php');


class knjl074fModel extends model
{
    var $cmd;
    var $year;
    var $applicantdiv;
    var $testdiv;
    var $exam_type;
    var $field = array();
    var $sort;
    var $avg_from;
    var $avg_to;
    var $target;
    var $special_reason_div;

    function knjl074fModel() {
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->year = CTRL_YEAR+1;
        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->exam_type    = VARS::post("EXAM_TYPE");
        $this->sort         = VARS::post("SORT");
        $this->avg_from     = VARS::post("AVG_FROM");
        $this->avg_to       = VARS::post("AVG_TO");
        $this->special_reason_div = VARS::post("SPECIAL_REASON_DIV");

        $this->target         = VARS::post("TARGET");

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
            $this->field["JUDGE_KIND"][$receptno] = VARS::post("JUDGE_KIND"."-".$receptno);
        }
        if (isset($this->field["JUDGE_KIND"])) {
            $result = knjl074fQuery::getUpdateQuery($this);
            if ($result) {
                $this->setMessage("MSG201");
            }
        } else {
            $this->setMessage("MSG310");
        }
    }
}
?>
