<?php
class knjl121iModel extends model {
    var $cmd;
    var $examYear;
    var $applicantdiv;
    var $testdiv;
    var $hid_ExamNo;
    var $hid_JudgeKindCd;
    var $arrExamJdCd;

    function knjl121iModel() {}

    function init() {
        $this->cmd = VARS::request("cmd");
        $this->examYear = CTRL_YEAR + 1;

        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->testdiv = VARS::post("TESTDIV");
        $this->hid_ExamNo = VARS::post("HID_EXAMNO");
        $this->hid_JudgeKindCd = VARS::post("HID_JUDGEKINDCD");
    }

    function &getMainModel() {}

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        $setExamNo = array();
        $this->arrExamJdCd  = array();

        //再配置
        $setExamNo = explode(",", $this->hid_ExamNo);
        $arrJdCd = explode(",", $this->hid_JudgeKindCd);

        foreach ($setExamNo as $examno) {
            $setJdCd = "";
            foreach ($arrJdCd as $jdCd) {
                if (strlen(VARS::post("JUDGE_KIND_{$jdCd}-{$examno}"))) {
                    $setJdCd = VARS::post("JUDGE_KIND_{$jdCd}-{$examno}");
                }
            }
            $this->arrExamJdCd[$examno] = $setJdCd;
        }

        //更新
        $result = knjl121iQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
