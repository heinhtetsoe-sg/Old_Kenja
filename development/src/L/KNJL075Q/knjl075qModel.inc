<?php

require_once('for_php7.php');

class knjl075qModel extends model {
    var $cmd;
    var $applicantdiv;
    var $testdiv;
    var $ObjYear;

    function knjl075qModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->cmd = VARS::request("cmd");

        $this->applicantdiv     = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv          = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");

        if ($this->arr_examno) {
            foreach ($this->arr_examno as $key => $examno) {
                $this->stoukyu[$examno]     = VARS::post("STOUKYU-".$examno);
                $this->htoukyu[$examno]     = VARS::post("HTOUKYU-".$examno);
            }
        }
    }

    function &getMainModel() {}

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //更新
        knjl075qQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
