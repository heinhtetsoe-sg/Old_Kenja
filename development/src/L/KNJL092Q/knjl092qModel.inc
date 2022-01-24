<?php

require_once('for_php7.php');

class knjl092qModel extends model
{
    public $cmd;
    public $applicantdiv;
    public $testdiv;
    public $ObjYear;
    public $examNoLength;

    public function knjl092qModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR+1;
        $this->examNoLength = 8;

        $this->cmd = VARS::request("cmd");

        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv      = strlen(VARS::post("TESTDIV"))      ? VARS::post("TESTDIV")      : VARS::post("HID_TESTDIV");

        $this->hid_examno           = VARS::post("HID_EXAMNO");

        $this->hid_schregNo = VARS::post("SCHREGNO");

        if ($this->hid_examno) {
            $this->schregNo = array();
            $this->examno = explode(",", $this->hid_examno);
            foreach ($this->examno as $idx => $eNo) {
                $this->schregNo[$eNo] = $this->hid_schregNo[$idx];
            }
        }
    }

    public function &getMainModel()
    {
    }

    public function validation() {
        $retVal = true;
        foreach ($this->schregNo as $eNo => $schNo) {
            if (strlen($schNo) > $this->examNoLength) {
                $this->setWarning("MSG915","学籍番号は{$this->examNoLength}バイトまでです。");
                $retVal = false;
            }
        }
        
        return $retVal;
    }
    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //更新
        if ($this->validation()) {
            knjl092qQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
