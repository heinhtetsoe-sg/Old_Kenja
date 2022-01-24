<?php

require_once('for_php7.php');

class knjl056eModel extends model {
    var $cmd;
    var $applicantdiv;
    var $testdiv;
    var $examhallcd;
    var $s_examno;
    var $e_examno;
    var $ObjYear;

    function knjl056eModel() {}

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->examhallcd);
            unset($this->subclasscd);
        }

        $this->ObjYear      = CTRL_YEAR+1;
        $this->applicantdiv = "1";  //固定値
        $this->exam_type    = "1";  //固定値

        $this->cmd = VARS::request("cmd");

        $this->applicantdiv     = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv          = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->examhallcd       = strlen(VARS::post("EXAMHALLCD")) ? VARS::post("EXAMHALLCD") : VARS::post("HID_EXAMHALLCD");
        $this->subclasscd       = strlen(VARS::post("SUBCLASSCD")) ? VARS::post("SUBCLASSCD") : VARS::post("HID_SUBCLASSCD");

        $this->score = array();
        if (isset($this->examnoArray)) {
            foreach ($this->examnoArray as $key => $examno) {
                $this->score[$examno] = VARS::post("SCORE_".$examno);
            }
        }
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        knjl056eQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }

}
?>
