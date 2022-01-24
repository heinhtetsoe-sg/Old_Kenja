<?php

require_once('for_php7.php');


class knjl252yModel extends model
{
    var $cmd;
    var $applicantdiv;
    var $appHold;
    var $testdiv;
    var $exam_type;
    var $examno;
    var $ObjYear;
    var $examhallcd;

    function knjl252yModel() {
    }

    function init() {
        $this->ObjYear     = CTRL_YEAR+1;

        $this->cmd    = VARS::request("cmd");

        $this->applicantdiv   = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->appHold        = VARS::post("APP_HOLD");
        $this->testdiv        = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->exam_type      = "1";
        $this->examhallcd     = strlen(VARS::post("EXAMHALLCD")) ? VARS::post("EXAMHALLCD") : VARS::post("HID_EXAMHALLCD");
        
        $this->hid_receptno = VARS::post("HID_RECEPTNO");
        
    }

    function &getMainModel() {
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["EXAMNO"]); $i++) {
            $value2 = "INTERVIEW_VALUE2"."-".$i;
            $remark2 = "INTERVIEW_REMARK2"."-".$i;
            $this->fields["INTERVIEW_VALUE2"][] = VARS::post($value2);
            $this->fields["INTERVIEW_REMARK2"][] = VARS::post($remark2);
            
            //入力チェック
            if (strlen($this->fields["INTERVIEW_VALUE2"][$i]) > 1 ) {
                $this->setWarning("MSG914","評価(受験番号：".$this->data["EXAMNO"][$i].")");
                return false;
            }
            if (strlen($this->fields["INTERVIEW_REMARK2"][$i]) > 150 ) {
                $this->setWarning("MSG914","行動の観察(受験番号：".$this->data["EXAMNO"][$i].")");
                return false;
            }
        }
        
        $result = knjl252yQuery::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201");
        }
    }
}
?>
