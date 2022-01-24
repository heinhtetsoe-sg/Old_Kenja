<?php

require_once('for_php7.php');


class knjl051bModel extends model
{
    var $field      = array();
    var $cmd;
    var $applicantdiv;
    var $appHold;
    var $testdiv;
    var $exam_type;
    var $examno;
    var $ObjYear;
    var $special_reason_div;

    function knjl051bModel()
    {
    }

    function init()
    {
        $this->ObjYear     = CTRL_YEAR+1;

        $this->cmd    = VARS::request("cmd");
        $this->field = array("S_EXAMNO"         =>  VARS::post("S_EXAMNO"),
                             "E_EXAMNO"         =>  VARS::post("E_EXAMNO"),
                            );   //受験番号の開始
        
        $this->applicantdiv   = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->appHold        = VARS::post("APP_HOLD");
        $this->testdiv        = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->exam_type      = "1";
        $this->special_reason_div = VARS::post("SPECIAL_REASON_DIV");
        $this->hid_receptno   = VARS::post("HID_RECEPTNO");//"RECEPTNO"-"COUNT"で取得
        
        //面接評価を受験番号ごとに配置
        $this->interView_A = array();
        $this->interView_B = array();
        $this->interView_C = array();
        $receptno_array = array();
        $tmp = explode(",", $this->hid_receptno);
        foreach ($tmp as $key => $receptno_count) {
            $receptno_array = explode("-", $receptno_count);
            $this->interView_A[$receptno_array[1]]  = VARS::post("A_CHECK-".$receptno_array[1]);
            $this->interView_B[$receptno_array[1]]  = VARS::post("B_CHECK-".$receptno_array[1]);
            $this->interView_C[$receptno_array[1]]  = VARS::post("C_CHECK-".$receptno_array[1]);
        }
    }

    //受験番号をセット
    function &getMainModel()
    {

        if ($this->cmd == "reset") {
            $this->cmd = "read";
        }

    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        $this->interViewCheck = array();
        knjl051bQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
