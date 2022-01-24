<?php

require_once('for_php7.php');

class knjl540aModel extends model {
    var $cmd;
    var $testdiv;
    var $hopeCourseCode;
    var $examType;
    var $ObjYear;
    var $CONST_SELALL;
    var $finSchoolCd;
    var $searchExamNo;

    function knjl540aModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;
        $this->examType     = 1;//1固定
        $this->applicantdiv = 1;//1固定
        $this->CONST_SELALL = "99999";
        $this->testdiv = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");

        $this->cmd = VARS::request("cmd");

        $this->hopeCourseCode    = strlen(VARS::post("HOPE_COURSECODE")) ? VARS::post("HOPE_COURSECODE") : VARS::post("HID_HOPE_COURSECODE");

        $this->searchExamNo = VARS::post("SEARCH_EXAMNO");
        $this->finSchoolCd = VARS::post("FINSCHOOLCD");

        $this->tmp_score  = VARS::post("SCORE");    //配列
        $this->hid_ReceptNo = VARS::post("HID_RECEPTNO");
    }

    function &getMainModel() {}

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        $this->setReceptNo = array();
        $this->arrTesAb  = array();

        //再配置
        $this->setReceptNo = explode(",", $this->hid_ReceptNo);
        foreach ($this->setReceptNo as $key => $receptno) {
            $this->arrTesAb[$receptno] = VARS::post("TEST_ABSENCE-".$receptno);
        }

        //更新
        $result = knjl540aQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
