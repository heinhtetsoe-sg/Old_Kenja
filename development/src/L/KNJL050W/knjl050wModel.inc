<?php

require_once('for_php7.php');

class knjl050wModel extends model {
    var $cmd;
    var $applicantdiv;
    var $testdiv;
    var $examno;
    var $ObjYear;
    var $field = array();

    function knjl050wModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->cmd = VARS::request("cmd");

        $this->applicantdiv   = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv        = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->testsubclasscd = strlen(VARS::post("TESTSUBCLASSCD")) ? VARS::post("TESTSUBCLASSCD") : VARS::post("HID_TESTSUBCLASSCD");
        $this->examcoursecd   = VARS::post("EXAMCOURSECD");
        $this->testdiv2       = VARS::post("TESTDIV2");

        $this->tmp_score      = VARS::post("SCORE");    //配列
        $this->hid_receptno   = VARS::post("HID_EXAMNO");

        $this->s_receptno   = VARS::post("s_receptno");
        $this->e_receptno   = VARS::post("e_receptno");

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
        $result = knjl050wQuery::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201");
        }
    }
}
?>
