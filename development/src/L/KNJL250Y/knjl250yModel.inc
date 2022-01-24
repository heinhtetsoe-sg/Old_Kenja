<?php

require_once('for_php7.php');


class knjl250yModel extends model
{
    var $cmd;
    var $applicantdiv;
    var $appHold;
    var $testdiv;
    var $exam_type;
    var $examno;
    var $ObjYear;
    var $examhallcd;

    function knjl250yModel() {
    }

    function init() {
        $this->ObjYear     = CTRL_YEAR+1;

        $this->cmd    = VARS::request("cmd");

        $this->applicantdiv   = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->appHold        = VARS::post("APP_HOLD");
        $this->testdiv        = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->exam_type      = "1";
        $this->examhallcd     = strlen(VARS::post("EXAMHALLCD")) ? VARS::post("EXAMHALLCD") : VARS::post("HID_EXAMHALLCD");

        $this->tmp_score = array();
        if (0 < get_count($this->subcdArray)) {
            foreach ($this->subcdArray as $key => $sub) {
                $name  = "SCORE".$sub["TESTSUBCLASSCD"];
                $this->tmp_score[$name] = VARS::post($name); //配列
            }
        }
        $this->hid_receptno = VARS::post("HID_RECEPTNO");
    }

    function &getMainModel() {
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        $this->score = array();

        //得点を再配置
        $tmp = explode(",", $this->hid_receptno);
        foreach ($tmp as $tmpkey => $receptno) {
            foreach ($this->subcdArray as $key => $sub) {
                $name  = "SCORE".$sub["TESTSUBCLASSCD"];
                $this->score[$receptno][$name]  = $this->tmp_score[$name][$tmpkey];
            }
        }
        $result = knjl250yQuery::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201","\\n全ての得点入力が完了したら\\n成績確定処理を実行して下さい。");
        }
    }
}
?>
