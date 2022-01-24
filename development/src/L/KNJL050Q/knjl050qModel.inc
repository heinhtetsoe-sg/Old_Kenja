<?php

require_once('for_php7.php');

class knjl050qModel extends model {
    var $cmd;
    var $applicantdiv;
    var $appHold;
    var $testdiv;
    var $examno;
    var $ObjYear;
    var $field = array();

    function knjl050qModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("NYURYOKU"                 =>  VARS::post("NYURYOKU"),                 //入力回数
                             "KIJUN_TEST_DIV"           =>  VARS::post("KIJUN_TEST_DIV")            //基準テスト
                             );

        $this->cmd = VARS::request("cmd");

        $this->applicantdiv   = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->appHold        = VARS::post("APP_HOLD");
        $this->testdiv        = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->testsubclasscd = strlen(VARS::post("TESTSUBCLASSCD")) ? VARS::post("TESTSUBCLASSCD") : VARS::post("HID_TESTSUBCLASSCD");

        $this->tmp_score      = VARS::post("SCORE");    //配列
        $this->tmp_score2     = VARS::post("SCORE2");   //配列
        $this->hid_receptno   = VARS::post("HID_RECEPTNO");

        $this->hid_receptno2 = VARS::post("HID_RECEPTNO2");
        $this->hid_score     = VARS::post("HID_SCORE");

        $this->s_receptno   = VARS::post("s_receptno");
        $this->e_receptno   = VARS::post("e_receptno");

    }

    function &getMainModel() {}

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        $this->score = $this->score2 = array();

        //得点を再配置
        $tmp = explode(",",$this->hid_receptno);
        if ($this->field["NYURYOKU"] == "1") {
            foreach ($tmp as $key => $receptno) {
                $this->score[$receptno]  = $this->tmp_score[$key];
                $this->score2[$receptno] = $this->hidscore2[$key];
            }
        } else {
            foreach ($tmp as $key => $receptno) {
                $this->score[$receptno]  = $this->hidscore[$key];
                $this->score2[$receptno] = $this->tmp_score2[$key];
            }
        }
        knjl050qQuery::getUpdateQuery($this);
        //更新後カーソルセット用
        $this->nextId = VARS::post("NEXT_ID");
        $this->set_sc_val = VARS::post("SET_SC_VAL");
        $this->enter_flg     = VARS::post("ENTER_FLG");
    }
}
?>
