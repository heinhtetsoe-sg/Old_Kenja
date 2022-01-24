<?php

require_once('for_php7.php');

class knjl580jModel extends model {
    var $field      = array();
    var $cmd;
    var $applicantdiv;
    var $testdiv;        //入試種別
    var $year;

    function knjl580jModel() {}

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->testdiv);
        }

        $this->year = CTRL_YEAR + 1;

        $this->cmd = VARS::request("cmd");

        $temp = VARS::request("APPLICANTDIV");
        if (isset($temp)) {
            $this->applicantdiv = VARS::request("APPLICANTDIV");
        }
        $temp = VARS::request("TESTDIV");
        if (isset($temp)) {
            $this->testdiv = VARS::request("TESTDIV");
        }

        $this->selectdata      = VARS::post("selectdata");

        $this->file = VARS::file("FILE");
    }

    function &getMainModel() {}

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //更新
        knjl580jQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
