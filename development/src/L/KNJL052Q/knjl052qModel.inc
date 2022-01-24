<?php

require_once('for_php7.php');

class knjl052qModel extends model
{
    public $cmd;
    public $applicantdiv;
    public $testdiv;
    public $ObjYear;

    public function knjl052qModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR+1;

        $this->cmd = VARS::request("cmd");

        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv      = strlen(VARS::post("TESTDIV"))      ? VARS::post("TESTDIV")      : VARS::post("HID_TESTDIV");

        $this->hid_receptno         = VARS::post("HID_RECEPTNO");
        $this->hid_examno           = VARS::post("HID_EXAMNO");

        $this->hid_up_examno          = VARS::post("HID_UP_RECEPTNO");
        $this->hid_up_interview_value = VARS::post("HID_UP_INTERVIEW_VALUE");

        if ($this->hid_receptno) {
            $tmpR = explode(",", $this->hid_receptno);
            $tmpE = explode(",", $this->hid_examno);
            foreach ($tmpR as $key => $receptno) {
                $this->interview_value[$receptno]   = VARS::post("INTERVIEW_VALUE-".$receptno);
                $this->interview_value2[$receptno]  = VARS::post("INTERVIEW_VALUE2-".$receptno);
                $this->examno[$receptno]            = $tmpE[$key];
            }
        }
    }

    public function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //更新
        knjl052qQuery::getUpdateQuery($this);
        if (SCHOOLKIND == "J") {
            $this->setMessage("MSG201");
        } else {
            $this->set_scroll_val = VARS::post("SET_SCROLL_VAL");
        }
    }
}
?>
