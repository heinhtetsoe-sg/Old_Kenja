<?php

require_once('for_php7.php');

class knjl532jModel extends model {
    var $cmd;
    var $applicantdiv;
    var $testdiv;
    var $examtype;
    var $s_receptno;
    var $e_receptno;
    var $chg_srchnoflg;
    var $ObjYear;
    var $field      = array();
    var $orderno;

    function knjl532jModel() {}

    function init() {
        $this->ObjYear   = CTRL_YEAR+1;
        $this->exam_type = '1'; //固定

        $this->cmd = VARS::request("cmd");

        $this->applicantdiv     = "1"; //1:中学固定
        $this->testdiv          = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->examtype   = strlen(VARS::post("EXAMTYPE")) ? VARS::post("EXAMTYPE") : VARS::post("HID_EXAMTYPE");
        $this->chg_srchnoflg    = false; // VARS::post("HID_S_RECEPTNO") == VARS::post("S_RECEPTNO") ? false : true;
        $this->s_receptno       = VARS::post("HID_S_RECEPTNO");  // ($this->chg_srchnoflg && strlen(VARS::post("S_RECEPTNO"))) ? VARS::post("S_RECEPTNO") : VARS::post("HID_S_RECEPTNO");
        $this->e_receptno       = VARS::post("HID_E_RECEPTNO"); // ($this->cmd == "back") ? VARS::post("HID_S_RECEPTNO") : VARS::post("HID_E_RECEPTNO");

        $this->tmp_orderno      = VARS::post("ORDERNO");    //配列
        $this->hid_receptno     = VARS::post("HID_RECEPTNO");

        $this->field = array("HEADER"           =>  VARS::post("HEADER")             //ヘッダ有
                            );

        $this->file = VARS::file("FILE");

        $this->orderno = array();
        $tmp = explode(",", $this->hid_receptno);
        foreach ($tmp as $key => $receptno) {
            $this->orderno[$receptno]  = $this->tmp_orderno[$key];
        }
    }

    function &getMainModel() {}

    // 編集完了用のモデルを返します
    function &getUpdateModel() {

        //更新
        $result = knjl532jQuery::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201");
        }
    }

}
?>
