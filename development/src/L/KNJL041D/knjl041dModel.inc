<?php

require_once('for_php7.php');

class knjl041dModel extends model {
    var $cmd;
    var $applicantdiv;
    var $testdiv;
    var $examType;
    var $examhallCd;
    var $ObjYear;

    function knjl041dModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;
        $this->applicantdiv = 1;//1固定
        $this->examType     = 1;//1固定

        $this->cmd = VARS::request("cmd");

        $this->testdiv    = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->examhallCd = strlen(VARS::post("EXAMHALLCD")) ? VARS::post("EXAMHALLCD") : VARS::post("HID_EXAMHALLCD");

        $this->tmp_score  = VARS::post("SCORE");    //配列
        $this->hid_ExamNo = VARS::post("HID_EXAMNO");
    }

    function &getMainModel() {}

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        $this->setExamNo = array();
        $this->arrTesAb  = array();
        $this->arrIntAb  = array();

        //再配置
        $this->setExamNo = explode(",", $this->hid_ExamNo);
        foreach ($this->setExamNo as $key => $examno) {
            $this->arrTesAb[$examno] = VARS::post("TEST_ABSENCE-".$examno);
            $this->arrIntAb[$examno] = VARS::post("INTERVEW_ABSENCE-".$examno);
        }

        //更新
        $result = knjl041dQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
