<?php

require_once('for_php7.php');

class knjl041aModel extends model {
    var $cmd;
    var $applicantdiv;
    var $testdiv;
    var $examType;
    var $ObjYear;

    function knjl041aModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;
        $this->examType     = 1;//1固定

        $this->applicantdiv = VARS::post("APPLICANTDIV");

        $this->cmd = VARS::request("cmd");

        $this->testdiv    = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");

        $this->tmp_score  = VARS::post("SCORE");    //配列
        $this->hid_ReceptNo = VARS::post("HID_RECEPTNO");
    }

    function &getMainModel() {}

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        $this->setReceptNo = array();
        $this->arrTesAb  = array();
        $this->arrreceptno  = array();

        //再配置
        $this->setReceptNo = explode(",", $this->hid_ReceptNo);
        foreach ($this->setReceptNo as $key => $receptno) {
            $this->arrTesAb[$receptno] = VARS::post("TEST_ABSENCE-".$receptno);
            $this->arrReceptNo[$receptno] = VARS::post("");
        }

        //更新
        $result = knjl041aQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
