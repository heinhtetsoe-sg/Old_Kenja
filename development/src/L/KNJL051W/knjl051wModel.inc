<?php

require_once('for_php7.php');
class knjl051wModel extends model {
    var $cmd;
    var $applicantdiv;
    var $testdiv;
    var $examno;
    var $ObjYear;
    var $field = array();

    function knjl051wModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->cmd = VARS::request("cmd");

        $this->applicantdiv    = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv         = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->kaijiSubclasscd = strlen(VARS::post("KAIJI_SUBCLASSCD")) ? VARS::post("KAIJI_SUBCLASSCD") : VARS::post("HID_KAIJI_SUBCLASSCD");
        $this->examcoursecd    = VARS::post("EXAMCOURSECD");
        $this->dataDiv         = VARS::post("DATA_DIV");
        $this->testdiv2        = VARS::post("TESTDIV2");

        $this->tmp_score       = VARS::post("SCORE");    //配列
        $this->hid_receptno    = VARS::post("HID_EXAMNO");

        $this->s_receptno      = VARS::post("s_receptno");
        $this->e_receptno      = VARS::post("e_receptno");

    }

    function &getMainModel() {}

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        $this->score = array();

        //得点を再配置
        $tmp = explode(",",$this->hid_receptno);
        foreach ($tmp as $key => $receptno) {
            $this->score[$receptno]  = $this->tmp_score[$key];
            if (strlen($this->tmp_score[$key]) > 4) {
                $this->setWarning("MSG915","得点は4バイトまでです。");
                return false;
            }
        }
        $result = knjl051wQuery::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201");
        }
    }
}
?>
