<?php

require_once('for_php7.php');

class knjl432hModel extends model
{
    public $field = array();
    public $cmd;
    public $year;
    public $applicantdiv;
    public $appHold;
    public $testdiv;
    public $totalcd;
    public $exam_type;
    public $examno;

    public function knjl432hModel()
    {
    }

    public function init()
    {
        $this->year = CTRL_YEAR + 1;

        $this->cmd = VARS::request("cmd");
        $this->field["MOVE_ENTER"] = VARS::post("MOVE_ENTER");  //受験番号の開始

        $this->applicantdiv   = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->appHold        = VARS::post("APP_HOLD");
        $this->testdiv        = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->totalcd        = strlen(VARS::post("TOTALCD")) ? VARS::post("TOTALCD") : VARS::post("HID_TOTALCD");
        $this->exam_type      = strlen(VARS::post("EXAM_TYPE")) ? VARS::post("EXAM_TYPE") : VARS::post("HID_EXAM_TYPE");

        $this->hid_receptno   = VARS::post("HID_RECEPTNO"); //"RECEPTNO"-"COUNT"で取得

        //面接評価を受験番号ごとに配置
        $this->interView_Value = array();
        $this->interView_Remark = array();
        $receptno_array = array();
        $tmp = explode(",", $this->hid_receptno);
        foreach ($tmp as $key => $receptno_count) {
            $receptno_array = explode("-", $receptno_count);
            $this->interView_Value[$receptno_array[1]]   = VARS::post("INTERVIEW_A-".$receptno_array[1]);
            $this->interView_Remark[$receptno_array[1]]  = VARS::post("INTERVIEW_REMARK-".$receptno_array[1]);
        }
    }

    //受験番号をセット
    public function &getMainModel()
    {
        if ($this->cmd == "reset") {
            $this->cmd = "read";
        }
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //データチェック
        if (!$this->hid_receptno) {
            $this->setWarning("MSG303");
            return false;
        }
        //サイズチェック
        $receptno_array = array();
        $tmp = explode(",", $this->hid_receptno);
        foreach ($tmp as $key => $receptno_count) {
            $receptno_array = explode("-", $receptno_count);
            if (strlen($this->interView_Value[$receptno_array[1]]) > 1) {
                $this->setWarning("MSG914", "面接評定は1バイトまでです。(受験番号:".$receptno_array[0].")");
                return false;
            }
            if (strlen($this->interView_Remark[$receptno_array[1]]) > 45) {
                $this->setWarning("MSG914", "面接は45バイトまでです。(受験番号:".$receptno_array[0].")");
                return false;
            }
        }

        //更新処理
        knjl432hQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
