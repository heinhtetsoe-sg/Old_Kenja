<?php

require_once('for_php7.php');


class knjl571fModel extends model
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
    var $special_reason_div;

    var $judgediv;
    var $suc_course;

    function knjl571fModel() {
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->year = CTRL_YEAR+1;
        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->exam_type    = VARS::post("EXAM_TYPE");
        $this->examcourse   = VARS::post("EXAMCOURSE");
        $this->shdiv        = VARS::post("SHDIV");
        $this->sort         = VARS::post("SORT");
        $this->avg_from     = VARS::post("AVG_FROM");
        $this->avg_to       = VARS::post("AVG_TO");
        $this->special_reason_div = VARS::post("SPECIAL_REASON_DIV");

        $this->tmp_judge    = VARS::post("JUDGEDIV");
        $this->tmp_course   = VARS::post("SUC_COURSE");

        $this->hid_examno   = VARS::post("HID_EXAMNO");
    }

    function &getMainModel() {
    }

    //入力項目チェック
    function validateInput() {
        return true;
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
            $this->succourse[$receptno] = VARS::post("SUC_COURSE"."-".$receptno);
            $this->judgediv[$receptno]  = $this->tmp_judge[$key];
        }
        if ($this->validateInput()) {
            $result = knjl571fQuery::getUpdateQuery($this);
            if ($result) {
                $this->setMessage("MSG201");
            }
        }
    }
}
?>
