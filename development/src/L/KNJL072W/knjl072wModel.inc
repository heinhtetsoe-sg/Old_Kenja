<?php

require_once('for_php7.php');

class knjl072wModel extends Model {
    var $cmd;
    var $selectdata;
    var $selectdata2;

    //メンバ変数の初期化
    function knjl072wModel() {}

    function init() {
        $this->cmd          = VARS::request("cmd");
        $this->ObjYear      = CTRL_YEAR + 1;
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->testdiv      = VARS::post("TESTDIV");
        $this->testdiv2     = VARS::post("TESTDIV2");
        $this->wishdiv      = VARS::post("WISHDIV");
        $this->examcoursecd = VARS::post("EXAMCOURSECD");
        $this->judge        = VARS::post("JUDGE");
        $this->selectdata   = VARS::post("selectdata");     //合格者一覧
        $this->selectdata2  = VARS::post("selectdata2");    //受検者一覧
    }

    //更新
    function &getUpdateModel() {
        knjl072wQuery::getUpdateQuery($this);
        $this->setMessage("MSG202");
        return;
    }
}
?>
