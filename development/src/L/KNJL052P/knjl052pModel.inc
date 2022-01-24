<?php

require_once('for_php7.php');

class knjl052pModel extends model {
    var $cmd;
    var $applicantdiv;
    var $testdiv;
    var $examno;
    var $ObjYear;
    var $field = array();

    function knjl052pModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->cmd = VARS::request("cmd");

        $this->applicantdiv   = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv        = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");

        $this->tmp_score      = VARS::post("SCORE");    //配列
        $this->tmp_score1     = VARS::post("SCORE1");    //配列
        $this->tmp_score2     = VARS::post("SCORE2");    //配列
        $this->hid_receptno   = VARS::post("HID_RECEPTNO");

        $this->s_receptno   = VARS::post("s_receptno");
        $this->e_receptno   = VARS::post("e_receptno");

    }

    function &getMainModel() {}

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        $this->score = array();
        $this->score1 = array();
        $this->score2 = array();
        $this->receptno = array();

        //県内高校推薦か？
        $suisenFlg = ($this->applicantdiv == "2" && $this->testdiv == "1") ? "1" : "";

        //面接を再配置
        $tmp = explode(",",$this->hid_receptno);
        foreach ($tmp as $key => $receptno) {
            if ($suisenFlg == "1") {
                $this->score1[$receptno] = $this->tmp_score1[$key];
                $this->score2[$receptno] = $this->tmp_score2[$key];
            } else {
                $this->score[$receptno]  = $this->tmp_score[$key];
            }
            $this->receptno[$receptno]  = $receptno;
        }
        $result = knjl052pQuery::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201","\\n全ての面接入力が完了したら\\n成績確定処理を実行して下さい。");
        }
    }
}
?>
