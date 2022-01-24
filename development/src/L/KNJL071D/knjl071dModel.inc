<?php

require_once('for_php7.php');

class knjl071dModel extends model {
    var $cmd;
    var $applicantdiv;
    var $testdiv;
    var $sub_order;
    var $desirediv;
    var $ObjYear;

    function knjl071dModel() {}

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->sub_order);
            unset($this->desirediv);
        }

        $this->ObjYear      = CTRL_YEAR+1;
        $this->applicantdiv = "1";  //固定値
        $this->exam_type    = "1";  //固定値

        $this->cmd = VARS::request("cmd");

        $this->testdiv          = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->sub_order        = strlen(VARS::post("SUB_ORDER")) ? VARS::post("SUB_ORDER") : VARS::post("HID_SUB_ORDER");
        $this->desirediv        = strlen(VARS::post("DESIREDIV")) ? VARS::post("DESIREDIV") : VARS::post("HID_DESIREDIV");

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
        knjl071dQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
