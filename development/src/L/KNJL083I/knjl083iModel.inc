<?php
class knjl083iModel extends model
{
    public $cmd;
    public $applicantdiv;
    public $testdiv;
    public $examHallCd;
    public $examType;
    public $examYear;
    public $hid_examno;
    public $updField;

    public function knjl083iModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->examHallCd);
        }

        $this->cmd = VARS::request("cmd");
        $this->examType = 1;//1固定
        $this->examYear = CTRL_YEAR + 1;

        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->examHallCd = strlen(VARS::post("EXAMHALLCD")) ? VARS::post("EXAMHALLCD") : VARS::post("HID_EXAMHALLCD");
    }

    public function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        $this->updField = array();

        $hid_examno = VARS::post("HID_EXAMNO");
        $examnoArray = explode(",", $hid_examno);
        foreach ($examnoArray as $key => $examno) {
            $this->updField[$examno]["JUDGEDIV"]  = VARS::post("TEST_ABSENCE-{$examno}");
            $this->updField[$examno]["UPD_FLG"]  = VARS::post("UPD_FLG_{$examno}");
        }

        //更新
        knjl083iQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
