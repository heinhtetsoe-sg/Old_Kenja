<?php

require_once('for_php7.php');

class knjl550jModel extends model {
    var $cmd;
    var $applicantdiv;
    var $testdiv;
    var $evaltype;
    var $s_findno;
    var $e_findno;
    var $ObjYear;
    var $exp_year;
    var $examtype;
    var $score;

    function knjl550jModel() {}

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->evaltype);
            unset($this->s_findno);
            unset($this->e_findno);
            unset($this->examtype);
            unset($score);
        }
        $this->cmd = VARS::request("cmd");

        $this->ObjYear        = CTRL_YEAR;
        $this->exp_year       = CTRL_YEAR + 1;
        $this->examtype       = VARS::post("EXAMTYPE");

        $this->applicantdiv          = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv          = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->evaltype         = strlen(VARS::post("EVALTYPE")) ? VARS::post("EVALTYPE") : (strlen(VARS::post("HID_EVALTYPE")) ? VARS::post("HID_EVALTYPE") : "1");
        $this->s_findno         = VARS::post("S_FINDNO");
        $this->e_findno         = VARS::post("E_FINDNO");

        $this->hid_examno       = VARS::post("HID_EXAMNO");
        $tmp_score              = VARS::post("SCORE");    //配列

        $this->score = array();
        $array = explode(",", $this->hid_examno);
        foreach ($array as $key => $receptno) {
            $this->score[$receptno]  = $tmp_score[$key];

        }
    }

    function &getMainModel() {}

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //更新
        knjl550jQuery::getUpdateQuery($this);
        $this->setMessage("MSG201","\\n全ての得点入力が完了したら\\n成績確定処理を実行して下さい。");
    }

}
?>
