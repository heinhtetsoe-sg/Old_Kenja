<?php

require_once('for_php7.php');

class knjl431hModel extends model
{
    public $cmd;
    public $year;
    public $applicantdiv;
    public $testdiv;
    public $examcoursecd;
    public $exam_type;
    public $field = array();

    public function knjl431hModel()
    {
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");

        $this->year = CTRL_YEAR + 1;
        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->examcoursecd = strlen(VARS::post("EXAMCOURSECD")) ? VARS::post("EXAMCOURSECD") : VARS::post("HID_EXAMCOURSECD");
        $this->exam_type    = strlen(VARS::post("EXAM_TYPE")) ? VARS::post("EXAM_TYPE") : VARS::post("HID_EXAM_TYPE");

        $this->hid_examno   = VARS::post("HID_EXAMNO");
    }

    public function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        $this->field = array();
        $keyList = array();
        $keyList = explode(",", $this->hid_examno);
        foreach ($keyList as $key => $val) {
            $tmp = explode("-", $val);
            $receptno = $tmp[0];
            $examno = $tmp[1];
            $this->field["CHK_DATA"][$receptno] = VARS::post("CHK_DATA"."-".$receptno);
            $this->field["UPD_FLG"][$receptno] = VARS::post("UPD_FLG"."-".$receptno);
        }
        $result = knjl431hQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
