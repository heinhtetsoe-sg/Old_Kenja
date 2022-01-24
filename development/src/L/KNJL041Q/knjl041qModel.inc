<?php

require_once('for_php7.php');


class knjl041qModel extends model {
    var $cmd;
    var $year;
    var $applicantdiv;
    var $testdiv;
    var $exam_type;
    var $field = array();

    function knjl041qModel() {
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->year = CTRL_YEAR+1;
        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->exam_type    = "1";

        $this->hid_examno   = VARS::post("HID_EXAMNO");

    }

    function &getMainModel() {
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        $this->field = array();
        $this->hid_receptno   = VARS::post("HID_RECEPTNO");
        $this->hid_examno2   = VARS::post("HID_EXAMNO2");
        $this->field["CHK_DATA"][$this->hid_receptno] = VARS::post("CHK_DATA"."-".$this->hid_receptno);

        knjl041qQuery::getUpdateQuery($this);
        //更新後カーソルセット用
        $this->nextId = VARS::post("NEXT_ID");
        $this->set_sc_val = VARS::post("SET_SC_VAL");
    }
}
?>
