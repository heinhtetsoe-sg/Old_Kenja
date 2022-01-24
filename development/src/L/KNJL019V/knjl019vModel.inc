<?php

require_once('for_php7.php');
class knjl019vModel extends model
{
    public $cmd;
    public $ObjYear;
    public $schoolKind;
    public $examId;
    public $placeId;
    public $examSubclass;
    public $sort;
    public $checkHide;
    public $updField;

    public function knjl019vModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->schoolKind);
            unset($this->examId);
            unset($this->placeId);
            unset($this->examSubclass);
        }

        $this->cmd = VARS::request("cmd");
        $this->ObjYear = CTRL_YEAR + 1;

        $this->schoolKind     = strlen(VARS::post("EXAM_SCHOOL_KIND")) ? VARS::post("EXAM_SCHOOL_KIND") : VARS::post("HID_EXAM_SCHOOL_KIND");
        $this->examId         = strlen(VARS::post("EXAM_ID")) ? VARS::post("EXAM_ID") : VARS::post("HID_EXAM_ID");
        $this->placeId        = strlen(VARS::post("PLACE_ID")) ? VARS::post("PLACE_ID") : VARS::post("HID_PLACE_ID");
        $this->examSubclass   = strlen(VARS::post("EXAM_SUBCLASS")) ? VARS::post("EXAM_SUBCLASS") : VARS::post("HID_EXAM_SUBCLASS");
        $this->sort           = VARS::post("SORT");
        $this->checkHide      = VARS::post("CHECK_HIDE");
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        $this->updField = array();
        $hid_receptno = VARS::post("HID_RECEPTNO");
        $receptnoArray = explode(",", $hid_receptno);
        foreach ($receptnoArray as $receptno) {
            $this->updField[$receptno]["SCORE"]        = VARS::post("SCORE_{$this->examSubclass}-{$receptno}");
            $this->updField[$receptno]["ABSENCE_FLG"]  = VARS::post("ABSENCE_FLG_{$this->examSubclass}-{$receptno}");
        }

        //更新
        knjl019vQuery::getUpdateQuery($this);
        $this->setMessage("MSG201", "\\n全ての得点入力が完了したら\\n成績確定処理を実行して下さい。");
    }
}
