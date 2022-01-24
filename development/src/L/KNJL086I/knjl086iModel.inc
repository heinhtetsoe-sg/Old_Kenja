<?php
class knjl086iModel extends model
{
    public $cmd;
    public $ObjYear;
    public $applicantdiv;
    public $testdiv;
    public $move_enter;

    public function knjl086iModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->testdiv);
        }

        $this->cmd = VARS::request("cmd");
        $this->ObjYear      = CTRL_YEAR+1;
        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->move_enter   = VARS::post("MOVE_ENTER"); //移動方向

        $hid_examno = VARS::post("HID_EXAMNO");
        $examnoArray = explode(",", $hid_examno);
        $this->interview = array();
        if (count($examnoArray) > 0) {
            foreach ($examnoArray as $key => $examno) {
                for ($remarkNo = 1; $remarkNo <= 7; $remarkNo++) {
                    $this->interview[$examno][$remarkNo] = VARS::post("OTHER_REMARK{$remarkNo}-{$examno}");
                }
            }
        }
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        knjl086iQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
