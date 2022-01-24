<?php
class knjl040vModel extends model
{
    public $cmd;
    public $ObjYear;
    public $examSchoolKind;
    public $examId;
    public $wkExamId;
    public $keyArray;
    public $examnoArray;
    public $depositCheckArray;
    public $depositDateArray;
    public $feeCheckArray;
    public $feeDateArray;
    public $declineCheckArray;
    public $declineDateArray;
   
    public function knjl040vModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->examSchoolKind);
            unset($this->examId);
            unset($this->wkExamId);
        }

        $this->cmd = VARS::request("cmd");
        $this->objYear = CTRL_YEAR + 1;
        $this->examSchoolKind = VARS::post("EXAM_SCHOOL_KIND");
        $this->examId = VARS::post("EXAM_ID");
        $this->examnoArray = array();
        $this->advanceCheckArray = array();
    }

    //入力項目チェック
    public function validateInput()
    {
        $returnFlg = false;
        $msg = "";

        foreach ($this->keyArray as $idx => $key) {
            //更新対象チェック
            $returnFlg = true;
            $this->examnoArray[$key]        = VARS::post("EXAMNO_{$key}");
            $this->depositCheckArray[$key]  = VARS::post("DEPOSIT_CHECK_{$key}");
            $this->depositDateArray[$key]   = VARS::post("DEPOSIT_DATE_{$key}");
            $this->feeCheckArray[$key]      = VARS::post("FEE_CHECK_{$key}");
            $this->feeDateArray[$key]       = VARS::post("FEE_DATE_{$key}");
            $this->declineCheckArray[$key]  = VARS::post("DECLINE_CHECK_{$key}");
            $this->declineDateArray[$key]   = VARS::post("DECLINE_DATE_{$key}");
        }

        if (!$returnFlg) {
            $msg .= "\\n更新対象者が存在しません。";
            $this->setWarning("MSG203", $msg);
        }
        return $returnFlg;
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            knjl040vQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
