<?php
class knjl108iModel extends model
{
    public $cmd;
    public $ObjYear;
    public $applicantdiv;
    public $testdiv;

    public function knjl108iModel()
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

        $hid_examno = VARS::post("HID_EXAMNO");
        $examnoArray = explode(",", $hid_examno);
        $this->updField = array();
        if (count($examnoArray) > 0) {
            foreach ($examnoArray as $key => $examno) {
                $this->updField[$examno]["BD022_REMARK1"]  = VARS::post("BD022_REMARK1-{$examno}");
                $this->updField[$examno]["BD022_REMARK2"]  = VARS::post("BD022_REMARK2-{$examno}");
                $this->updField[$examno]["BD022_REMARK3"]  = VARS::post("BD022_REMARK3-{$examno}");
                $this->updField[$examno]["PROCEDUREDIV"]   = VARS::post("PROCEDUREDIV-{$examno}");
                $this->updField[$examno]["PROCEDUREDATE"]  = VARS::post("PROCEDUREDATE-{$examno}");
                $this->updField[$examno]["ENTDIV"]         = VARS::post("ENTDIV-{$examno}");
            }
        }
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        knjl108iQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
