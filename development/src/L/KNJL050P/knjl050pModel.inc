<?php

require_once('for_php7.php');

class knjl050pModel extends model {
    var $cmd;
    var $applicantdiv;
    var $testdiv;
    var $testsubclasscd;
    var $s_receptno;
    var $e_receptno;
    var $examno;
    var $ObjYear;
    var $field = array();

    function knjl050pModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->cmd = VARS::request("cmd");

        $this->applicantdiv   = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv        = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->testsubclasscd = strlen(VARS::post("TESTSUBCLASSCD")) ? VARS::post("TESTSUBCLASSCD") : VARS::post("HID_TESTSUBCLASSCD");
        $this->s_receptno       = strlen(VARS::post("S_RECEPTNO")) ? VARS::post("S_RECEPTNO") : VARS::post("HID_S_RECEPTNO");
        $this->e_receptno       = strlen(VARS::post("E_RECEPTNO")) ? VARS::post("E_RECEPTNO") : VARS::post("HID_E_RECEPTNO");

        $this->tmp_score      = VARS::post("SCORE");    //配列
        $this->hid_receptno   = VARS::post("HID_RECEPTNO");
    }

    function &getMainModel() {}

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        $this->score = array();

        //得点を再配置
        $tmp = explode(",",$this->hid_receptno);
        foreach ($tmp as $key => $receptno) {
            $this->score[$receptno]  = $this->tmp_score[$key];
        }
        $result = knjl050pQuery::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201","\\n全ての得点入力が完了したら\\n成績確定処理を実行して下さい。");
        }
    }
}
?>
