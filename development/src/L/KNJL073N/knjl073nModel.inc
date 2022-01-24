<?php

require_once('for_php7.php');


class knjl073nModel extends model
{
    public $cmd;
    public $applicantdiv;
    public $appHold;
    public $testdiv;
    public $examcourse;
    public $shdiv;
    public $exam_type;
    public $examno;
    public $ObjYear;
    public $sort;
    public $score;
    public $field = array();

    public function knjl073nModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR+1;

        $this->cmd = VARS::request("cmd");
        
        
        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->appHold      = VARS::post("APP_HOLD");
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->exam_type    = "1";
        $this->examcourse   = strlen(VARS::post("EXAMCOURSE")) ? VARS::post("EXAMCOURSE") : VARS::post("HID_EXAMCOURSE");
        $this->shdiv        = strlen(VARS::post("SHDIV")) ? VARS::post("SHDIV") : VARS::post("HID_SHDIV");

        $this->sort         = VARS::post("SORT");
        $this->hid_examno   = VARS::post("HID_EXAMNO");

        $this->suc_course = VARS::post("SUC_COURSE");
        $this->field["CHK_DATA"] = VARS::post("CHK_DATA");
    }

    public function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        if (isset($this->field["CHK_DATA"])) {
            $result = knjl073nQuery::getUpdateQuery($this);
            if ($result) {
                $this->setMessage("MSG201");
            }
        } else {
            $this->setMessage("MSG310");
        }
    }
}
