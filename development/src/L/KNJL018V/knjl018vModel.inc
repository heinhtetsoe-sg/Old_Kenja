<?php

require_once('for_php7.php');

class knjl018vModel extends model
{
    public $cmd;
    public $examYear;
    public $examSchoolKind;
    public $applicantDiv;
    public $placeID;
    public $finschoolCD;
    public $order;
    public $hidReceptno;
    public $selectedAbsenceFlg;

    public function knjl018vModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->examSchoolKind);
            unset($this->examID);
            unset($this->placeID);
            unset($this->finschoolCD);
            unset($this->order);
        }

        $this->cmd = VARS::request("cmd");
        $this->examYear = CTRL_YEAR + 1;

        $this->examSchoolKind = strlen(VARS::post("EXAM_SCHOOL_KIND")) ? VARS::post("EXAM_SCHOOL_KIND") : "";
        $this->examID = strlen(VARS::post("EXAM_ID")) ? VARS::post("EXAM_ID") : "";
        $this->placeID = strlen(VARS::post("PLACE_ID")) ? VARS::post("PLACE_ID") : "ALL";
        $this->finschoolCD = strlen(VARS::post("FINSCHOOLCD")) ? VARS::post("FINSCHOOLCD") : "ALL";
        $this->order = strlen(VARS::post("ORDER")) ? VARS::post("ORDER") : "1";
        $this->hidReceptno = VARS::post("HID_RECEPTNO");
        $this->selectedAbsenceFlg = array();
        if ($this->cmd != "reset") {
            if (VARS::post("ABSENCE_FLG") != "") {
                $this->selectedAbsenceFlg = VARS::post("ABSENCE_FLG");
            }
        }
    }

    public function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        $this->updField = array();

        $receptnoArray = explode(",", $this->hidReceptno);
        for ($idx = 0; $idx < get_count($receptnoArray); $idx++) {
            $receptno_absence_flg = explode("-", $receptnoArray[$idx]);
            $absence_flg = VARS::post("ABSENCE_FLG_{$receptno_absence_flg[0]}");
            $absence_flg = $absence_flg == "" ? "0" : $absence_flg;
            $this->updField[$receptno_absence_flg[0]]["ABSENCE_FLG"] = $absence_flg;
            $this->updField[$receptno_absence_flg[0]]["UPD_FLG"] = ($absence_flg == $receptno_absence_flg[1]) ? "0" : "1";
        }

        //更新
        knjl018vQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
