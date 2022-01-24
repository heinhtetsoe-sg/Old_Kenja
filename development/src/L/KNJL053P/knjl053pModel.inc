<?php

require_once('for_php7.php');

class knjl053pModel extends model {
    var $cmd;
    var $applicantdiv;
    var $testdiv;
    var $examno;
    var $ObjYear;
    var $field = array();

    function knjl053pModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->cmd = VARS::request("cmd");

        $this->applicantdiv   = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv        = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");

        $this->tmp_value1     = VARS::post("VALUE1");    //配列
        $this->tmp_value2     = VARS::post("VALUE2");    //配列
        $this->tmp_value3     = VARS::post("VALUE3");    //配列

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

        //作文を再配置
        $tmp = explode(",",$this->hid_receptno);
        foreach ($tmp as $key => $receptno) {
            $this->receptno[$receptno]  = $receptno;
            $this->value[$receptno]["VALUE1"] = $this->tmp_value1[$key];
            $this->value[$receptno]["VALUE2"] = $this->tmp_value2[$key];
            $this->value[$receptno]["VALUE3"] = $this->tmp_value3[$key];
        }
        $result = knjl053pQuery::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201","\\n全ての作文入力が完了したら\\n成績確定処理を実行して下さい。");
        }
    }
}
?>
