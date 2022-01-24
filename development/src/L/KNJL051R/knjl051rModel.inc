<?php

require_once('for_php7.php');

class knjl051rModel extends model
{
    var $cmd;
    var $applicantdiv;
    var $appHold;
    var $testdiv;
    var $exam_type;
    var $examno;
    var $ObjYear;
    var $examhallcd;

    function knjl051rModel()
    {
    }

    function init()
    {
        $this->ObjYear     = CTRL_YEAR+1;

        $this->cmd    = VARS::request("cmd");

        $this->fixApplicantDiv = '1';
        if (SCHOOLKIND == 'J') {
            $this->fixApplicantDiv = '2';
        }

        $this->applicantdiv   = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->appHold        = VARS::post("APP_HOLD");
        $this->testdiv        = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->exam_type      = "1";
        $this->examhallcd     = strlen(VARS::post("EXAMHALLCD")) ? VARS::post("EXAMHALLCD") : VARS::post("HID_EXAMHALLCD");

        $this->hid_receptno   = VARS::post("HID_RECEPTNO");
    }

    function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        $this->cCheck = array();
        //得点を再配置
        $tmp = explode(",", $this->hid_receptno);
        foreach ($tmp as $key => $receptno) {
            $this->cCheck[$receptno]  = VARS::post("C_CHECK-".$receptno);
        }
        knjl051rQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
