<?php

require_once('for_php7.php');
class knjl458hModel extends model
{
    public $cmd;
    public $ObjYear;
    public $applicantdiv;
    public $coursecd;
    public $sort;

    public function knjl458hModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->coursecd);
            unset($this->sort);
        }

        $this->cmd = VARS::request("cmd");
        $this->ObjYear      = CTRL_YEAR + 1;
        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->coursecd     = strlen(VARS::post("EXAMCOURSECD")) ? VARS::post("EXAMCOURSECD") : VARS::post("HID_EXAMCOURSECD");
        $this->sort         = VARS::post("SORT");

        $hid_examno = VARS::post("HID_EXAMNO");
        $examnoArray = explode(",", $hid_examno);
        $this->updField = array();
        if (count($examnoArray) > 0) {
            foreach ($examnoArray as $key => $examno) {
                $this->updField[$examno]["ENTDIV"]         = VARS::post("ENTDIV-{$examno}");
                $this->updField[$examno]["UPD_FLG"]        = VARS::post("UPD_FLG_{$examno}");
            }
        }
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        knjl458hQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
