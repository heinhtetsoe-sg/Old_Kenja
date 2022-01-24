<?php

require_once('for_php7.php');

class knjl530aModel extends model {
    var $cmd;
    var $applicantdiv;
    var $testdiv;
    var $desirediv;
    var $examno;
    var $sort;
    var $ObjYear;

    function knjl530aModel() {}

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->desirediv);
            unset($this->examno);
            unset($this->sort);
        }

        $this->ObjYear      = CTRL_YEAR+1;
        $this->applicantdiv = "1";  //固定値

        $this->cmd = VARS::request("cmd");

        $this->testdiv          = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->desirediv        = strlen(VARS::post("DESIREDIV")) ? VARS::post("DESIREDIV") : VARS::post("HID_DESIREDIV");
        $this->examno           = strlen(VARS::post("EXAMNO")) ? VARS::post("EXAMNO") : "";
        $this->sort             = strlen(VARS::post("SORT")) ? VARS::post("SORT") : VARS::post("HID_SORT");

        $this->hid_examno       = VARS::post("HID_EXAMNO");

        $this->data = array();
        $array = explode(",", $this->hid_examno);
        foreach ($array as $key => $examno) {
            $this->data["GOKAKU"][$examno]           = VARS::post("GOKAKU_".$examno);
        }
    }

    function &getMainModel() {}

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //更新
        knjl530aQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
