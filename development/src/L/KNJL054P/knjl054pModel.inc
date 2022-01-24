<?php

require_once('for_php7.php');

class knjl054pModel extends model {
    var $cmd;
    var $applicantdiv;
    var $testdiv;
    var $examno;
    var $ObjYear;
    var $field = array();

    function knjl054pModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->cmd = VARS::request("cmd");

        $this->applicantdiv   = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv        = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");

        $this->tmp_score      = VARS::post("SCORE");    //配列
        $this->tmp_remark1    = VARS::post("REMARK1");    //配列

        $this->hid_receptno   = VARS::post("HID_RECEPTNO");

        $this->s_receptno   = VARS::post("s_receptno");
        $this->e_receptno   = VARS::post("e_receptno");

    }

    function &getMainModel() {}

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        $this->receptno = array();
        $this->value = array();

        //県内高校推薦か？
        $suisenFlg = ($this->applicantdiv == "2" && $this->testdiv == "1") ? "1" : "";

        //実績を再配置
        $tmp = explode(",",$this->hid_receptno);
        foreach ($tmp as $key => $receptno) {
            $this->receptno[$receptno]  = $receptno;
            $this->value[$receptno]["SCORE"] = $this->tmp_score[$key];
            $this->value[$receptno]["REMARK1"] = $this->tmp_remark1[$key];
        }
        if ($this->validateInput()) {
            $result = knjl054pQuery::getUpdateQuery($this);
            if ($result) {
                $this->setMessage("MSG201","\\n全ての実績入力が完了したら\\n成績確定処理を実行して下さい。");
            }
        }
    }

    //入力項目チェック
    function validateInput() {
        $tmp = explode(",",$this->hid_receptno);
        foreach ($tmp as $key => $receptno) {
            if (strlen($this->value[$receptno]["REMARK1"]) > 600) {
                $this->setWarning("MSG915","実績状況は600バイトまでです。\\n受験番号：".$receptno);
                return false;
            }
        }
        return true;
    }
}
?>
