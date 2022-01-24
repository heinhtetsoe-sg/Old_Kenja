<?php
class knjl750hModel extends model
{
    public $cmd;
    public $ObjYear;
    public $applicantdiv;
    public $testdiv;
    public $exam_type;
    public $testsubclasscd;
    public $examhallcd;
    public $s_receptno;
    public $e_receptno;

    public function knjl750hModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->testsubclasscd);
            unset($this->examhallcd);
        }

        $this->cmd = VARS::request("cmd");
        $this->ObjYear      = CTRL_YEAR+1;
        $this->exam_type    = 1; //固定値
        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->testsubclasscd   = strlen(VARS::post("TESTSUBCLASSCD")) ? VARS::post("TESTSUBCLASSCD") : VARS::post("HID_TESTSUBCLASSCD");
        $this->examhallcd   = strlen(VARS::post("EXAMHALLCD")) ? VARS::post("EXAMHALLCD") : VARS::post("HID_EXAMHALLCD");
        $this->s_receptno   = VARS::post("S_RECEPTNO");
        $this->e_receptno   = VARS::post("E_RECEPTNO");
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        $this->updField = array();

        $hid_receptno = VARS::post("HID_RECEPTNO");
        $receptnoArray = explode(",", $hid_receptno);
        foreach ($receptnoArray as $key => $receptno) {
            $this->updField[$receptno]["SCORE"]     = VARS::post("SCORE-{$receptno}");
            $this->updField[$receptno]["JUDGEDIV"]  = VARS::post("JUDGEDIV-{$receptno}");
        }

        //更新
        knjl750hQuery::getUpdateQuery($this);
        $this->setMessage("MSG201", "\\n全ての得点入力が完了したら\\n成績確定処理を実行して下さい。");
    }
}
