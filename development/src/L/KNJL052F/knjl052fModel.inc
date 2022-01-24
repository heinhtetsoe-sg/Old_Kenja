<?php

require_once('for_php7.php');

class knjl052fModel extends model {
    var $field = array();
    var $cmd;
    var $applicantdiv;
    var $appHold;
    var $testdiv;
    var $testdiv0;
    var $exam_type;
    var $examno;
    var $ObjYear;
    var $special_reason_div;

    function knjl052fModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->cmd = VARS::request("cmd");
        $this->field["MOVE_ENTER"] = VARS::post("MOVE_ENTER");  //受験番号の開始

        $this->applicantdiv   = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->appHold        = VARS::post("APP_HOLD");
        $this->testdiv        = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->testdiv0       = strlen(VARS::post("TESTDIV0")) ? VARS::post("TESTDIV0") : VARS::post("HID_TESTDIV0");
        $this->totalcd        = strlen(VARS::post("TOTALCD")) ? VARS::post("TOTALCD") : VARS::post("HID_TOTALCD");
        $this->exam_type      = strlen(VARS::post("EXAM_TYPE")) ? VARS::post("EXAM_TYPE") : VARS::post("HID_EXAM_TYPE");

        $this->special_reason_div = VARS::post("SPECIAL_REASON_DIV");

        $this->hid_receptno   = VARS::post("HID_RECEPTNO"); //"RECEPTNO"-"COUNT"で取得

        //面接評価を受験番号ごとに配置
        $this->interView_Value = array();
        $this->interView_Remark = array();
        $receptno_array = array();
        $tmp = explode(",", $this->hid_receptno);
        foreach ($tmp as $key => $receptno_count) {
            $receptno_array = explode("-", $receptno_count);
            $this->interView_Value[$receptno_array[1]]   = VARS::post("INTERVIEW_VALUE-".$receptno_array[1]);
            $this->interView_Remark[$receptno_array[1]]  = VARS::post("INTERVIEW_REMARK-".$receptno_array[1]);
        }
    }

    //受験番号をセット
    function &getMainModel() {
        if ($this->cmd == "reset") {
            $this->cmd = "read";
        }
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
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
                $this->setWarning("MSG914","面接評定は1バイトまでです。(受験番号:".$receptno_array[0].")");
                return false;
            }
            if (strlen($this->interView_Remark[$receptno_array[1]]) > 45) {
                $this->setWarning("MSG914","面接は45バイトまでです。(受験番号:".$receptno_array[0].")");
                return false;
            }
        }

        //更新処理
        knjl052fQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
