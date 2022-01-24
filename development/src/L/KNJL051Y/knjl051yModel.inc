<?php

class knjl051yModel extends model
{
    var $cmd;
    var $applicantdiv;
    var $appHold;
    var $testdiv;
    var $exam_type;
    var $examno;
    var $ObjYear;
    var $examhallcd;
    var $score;

    function knjl051yModel()
    {
    }

    function init()
    {
        $this->ObjYear = CTRL_YEAR+1;

        $this->cmd = VARS::request("cmd");
        
        
        $this->applicantdiv   = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->appHold        = VARS::post("APP_HOLD");
        $this->testdiv        = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->exam_type      = "1";
        $this->examhallcd     = strlen(VARS::post("EXAMHALLCD")) ? VARS::post("EXAMHALLCD") : VARS::post("HID_EXAMHALLCD");
        
        $this->hid_examno   = VARS::post("HID_EXAMNO");

        $this->field = array("INTERVIEW_REMARK"     => VARS::post("INTERVIEW_REMARK"),
                             "INTERVIEW_VALUE"      => VARS::post("INTERVIEW_VALUE"),
                             "COMPOSITION_VALUE"    => VARS::post("COMPOSITION_VALUE"));
    }

    function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        $this->score = array();
        //備考・評価を再配置
        $tmp = explode(",",$this->hid_examno);
        foreach ($tmp as $key => $examno) {
            foreach ($this->field as $col => $val) {
                $this->score[$examno][$col] = $val[$key];
            }
        }
        //入力値チェック
        foreach ($this->score as $examno => $val) {
            foreach ($val as $col => $score) {
                if ($col == "INTERVIEW_REMARK" && strlen($score) > 210) {
                    $this->setWarning("MSG901", "( 面接備考：210バイトまで )\\n\\n受験番号：".$examno);
                    return false;
                }
            }
        }
        //更新処理
        $result = knjl051yQuery::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201");
        }
    }
}
?>
