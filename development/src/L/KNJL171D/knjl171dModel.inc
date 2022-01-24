<?php

require_once('for_php7.php');

class knjl171dModel extends model {
    var $cmd;
    var $applicantdiv;
    var $testdiv;
    var $ObjYear;

    function knjl171dModel() {}

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->testdiv);
        }

        $this->ObjYear      = VARS::post("YEAR");
        $this->applicantdiv = "1";  //固定値

        $this->cmd = VARS::request("cmd");

        $this->testdiv          = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");

        $this->hid_examno       = VARS::post("HID_EXAMNO");

        $this->judge = array();
        $array = explode(",", $this->hid_examno);
        foreach ($array as $key => $examno) {
            $this->judge[$examno]   = VARS::post("JUDGEMENT_".$examno);
        }
    }

    function &getMainModel() {}

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //更新
        knjl171dQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
?>