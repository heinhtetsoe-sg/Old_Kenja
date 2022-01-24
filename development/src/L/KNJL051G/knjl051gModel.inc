<?php

require_once('for_php7.php');

class knjl051gModel extends model {
    var $cmd;
    var $applicantdiv;
    var $testdiv;
    var $shdiv;
    var $examcoursecd;
    var $examhallcd;
    var $ObjYear;
    var $isKeiai;
    var $isKasiwara;
    var $special_reason_div;

    function knjl051gModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->cmd = VARS::request("cmd");

        //学校コードを取得　敬愛：30270254001、柏原：30270247001
        $rtnRow = knjl051gQuery::getNameMstZ010();
        $this->isKeiai      = $rtnRow["NAME2"] == "30270254001" ? "1" : "";
        $this->isKasiwara   = $rtnRow["NAME2"] == "30270247001" ? "1" : "";

        $this->applicantdiv       = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv            = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->shdiv              = strlen(VARS::post("SHDIV")) ? VARS::post("SHDIV") : VARS::post("HID_SHDIV");
        $this->examcoursecd       = strlen(VARS::post("EXAMCOURSECD")) ? VARS::post("EXAMCOURSECD") : VARS::post("HID_EXAMCOURSECD");
        $this->examhallcd         = strlen(VARS::post("EXAMHALLCD")) ? VARS::post("EXAMHALLCD") : VARS::post("HID_EXAMHALLCD");
        $this->special_reason_div = VARS::post("SPECIAL_REASON_DIV");

        $this->tmp_interview_value  = VARS::post("INTERVIEW_VALUE");    //配列
        $this->tmp_interview_remark = VARS::post("INTERVIEW_REMARK");   //配列
        $this->tmp_remark1          = VARS::post("REMARK1");            //配列
        $this->hid_receptno         = VARS::post("HID_RECEPTNO");
        $this->hid_examno           = VARS::post("HID_EXAMNO");
    }

    function &getMainModel() {}

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        $this->interview_value = array();
        $this->interview_remark = array();
        $this->remark1 = array();
        $this->examno = array();

        //再配置
        $tmpR = explode(",", $this->hid_receptno);
        $tmpE = explode(",", $this->hid_examno);
        foreach ($tmpR as $key => $receptno) {
            $this->interview_value[$receptno]   = $this->tmp_interview_value[$key];
            $this->interview_remark[$receptno]  = $this->tmp_interview_remark[$key];
            $this->remark1[$receptno]           = $this->tmp_remark1[$key];
            $this->examno[$receptno]            = $tmpE[$key];
        }

        //更新
        knjl051gQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
