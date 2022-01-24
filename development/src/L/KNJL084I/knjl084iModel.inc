<?php
class knjl084iModel extends model
{
    public $cmd;
    public $ObjYear;
    public $applicantdiv;
    public $testdiv;
    public $exam_type;
    public $examhallcd;
    public $testsubclasscd;
    public $testsubclasscdSaki;

    public function knjl084iModel()
    {
    }

    public function init()
    {

        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->examhallcd);
            unset($this->testsubclasscd);
        }

        $this->cmd = VARS::request("cmd");
        $this->ObjYear      = CTRL_YEAR+1;
        $this->exam_type    = 1; //固定値
        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->examhallcd   = strlen(VARS::post("EXAMHALLCD")) ? VARS::post("EXAMHALLCD") : VARS::post("HID_EXAMHALLCD");
        $this->testsubclasscd   = strlen(VARS::post("TESTSUBCLASSCD")) ? VARS::post("TESTSUBCLASSCD") : VARS::post("HID_TESTSUBCLASSCD");
        $db = Query::dbCheckOut();
        $this->testsubclasscdSaki = $db->getOne(knjl084iQuery::getTestsubclasscdSaki($this));
        Query::dbCheckIn($db);
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        $this->updField = array();
        $this->upd_receptno = array();
        $this->upd_receptno = explode(",", VARS::post("UPD_RECEPTNO")); //更新(受験番号)

        $hid_receptno = VARS::post("HID_RECEPTNO");
        $receptnoArray = explode(",", $hid_receptno);
        foreach ($receptnoArray as $key => $receptno) {
            if (!in_array($receptno, $this->upd_receptno, true)) {
                continue;
            }
            $this->updField[$receptno]["SCORE"] = VARS::post("SCORE-{$receptno}");
            $this->updField[$receptno]["ATTEND_FLG"]  = VARS::post("ATTEND_FLG-{$receptno}");
        }

        //更新
        knjl084iQuery::getUpdateQuery($this);
        if ($this->testsubclasscdSaki != '') {
            knjl084iQuery::getUpdateSakiQuery($this, $receptnoArray);
        }
        $this->setMessage("MSG201");
    }
}
