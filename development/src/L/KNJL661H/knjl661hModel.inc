<?php
class knjl661hModel extends model
{
    public $cmd;
    public $ObjYear;
    public $applicantdiv;
    public $testdiv;
    public $exam_type;
    public $s_receptno;
    public $e_receptno;
    public $updField;

    public function knjl661hModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->testdiv);
        }

        $this->cmd = VARS::request("cmd");
        $this->ObjYear        = CTRL_YEAR + 1;
        $this->exam_type      = 1; //固定値
        $this->applicantdiv   = 2; //固定値
        $this->testdiv        = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->s_receptno     = VARS::post("S_RECEPTNO");
        $this->e_receptno     = VARS::post("E_RECEPTNO");
    }

    public function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        $this->updField = array();

        $hid_receptno = VARS::post("HID_RECEPTNO");
        $receptnoArray = explode(",", $hid_receptno);

        foreach ($receptnoArray as $receptno) {
            if (VARS::post("JUDGEMENT-{$receptno}") == "3") {
                continue;
            }
            $this->updField[$receptno]["PLUS_POINT"]     = VARS::post("PLUS_POINT-{$receptno}");
        }

        //更新
        knjl661hQuery::getUpdateQuery($this);
        $this->setMessage("MSG201", "\\n加点を変更した場合は\\n再度、成績確定処理を実行して下さい。");
    }
}
?>
