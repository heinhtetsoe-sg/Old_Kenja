<?php

require_once('for_php7.php');

class knjl074qModel extends model {
    var $cmd;
    var $year;
    var $applicantdiv;
    var $testdiv;
    var $field = array();

    function knjl074qModel() {}

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->year = CTRL_YEAR+1;
        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");

    }

    function &getMainModel() {}

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        $this->field = array();
        foreach ($this->arr_examno as $key => $examno) {
            $this->field["CHK_DATA"][$examno] = VARS::post("CHK_DATA"."-".$examno);
        }
        if (isset($this->field["CHK_DATA"])) {
            $result = knjl074qQuery::getUpdateQuery($this);
            if ($result) {
                $this->setMessage("MSG201");
            }
        } else {
            $this->setMessage("MSG310");
        }
    }
}
?>
