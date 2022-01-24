<?php

require_once('for_php7.php');


class knjl072rModel extends model
{
    var $cmd;
    var $applicantdiv;
    var $appHold;
    var $testdiv;
    var $exam_type;
    var $examno;
    var $ObjYear;
    var $examhallcd;

    function knjl072rModel()
    {
    }

    function init()
    {
        $this->ObjYear     = CTRL_YEAR+1;

        $this->fixApplicantDiv = '1';
        if (SCHOOLKIND == 'J') {
            $this->fixApplicantDiv = '2';
        }

        $this->cmd    = VARS::request("cmd");
        $this->applicantdiv   = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->appHold        = VARS::post("APP_HOLD");
        $this->testdiv        = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->sort           = VARS::post("SORT");
        $this->exam_type      = "1";
        $this->examhallcd     = strlen(VARS::post("EXAMHALLCD")) ? VARS::post("EXAMHALLCD") : VARS::post("HID_EXAMHALLCD");

        $this->hid_examno   = VARS::post("HID_EXAMNO");
    }

    function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        $this->judgeKind = array();
        //得点を再配置
        $tmp = explode(",", $this->hid_examno);
        foreach ($tmp as $key => $examno) {
            $this->judgeKind[$examno] = VARS::post("JUDGE_KIND-".$examno);
        }
        knjl072rQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
