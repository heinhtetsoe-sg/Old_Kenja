<?php

require_once('for_php7.php');

class knjl031eModel extends model {
    var $cmd;
    var $entexamyear;
    var $applicantdiv;
    var $testdiv;
    var $examhall_type;
    var $examhallcd;

    function knjl031eModel() {}

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->entexamyear);
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->examhall_type);
            unset($this->examhallcd);
            unset($this->hidExamNo);
        }

        $this->entexamyear      = CTRL_YEAR+1;

        $this->applicantdiv     = VARS::post("APPLICANTDIV");
        $this->testdiv          = VARS::post("TESTDIV");
        $this->examhall_type    = VARS::post("EXAMHALL_TYPE");
        $this->examhallcd       = VARS::post("EXAMHALLCD");
        $this->hidExamNo        = VARS::post("HID_EXAMNO");
        $this->chkExamNo        = VARS::post("CHECKED");

        $this->cmd = VARS::request("cmd");

        if ($this->cmd == "update" || $this->cmd == "replace") {
            $this->applicantdiv     = VARS::post("HIDDEN_APPLICANTDIV");
            $this->testdiv          = VARS::post("HIDDEN_TESTDIV");
            $this->examhall_type    = VARS::post("HIDDEN_EXAMHALL_TYPE");
            $this->examhallcd       = VARS::post("HIDDEN_EXAMHALLCD");
        }
    }

    function &getMainModel() {}

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //初期化
        $this->updField = array();

        //再配置
        $array = explode(",",$this->hidExamNo);
        foreach ($array as $examno) {
            $this->updField[$examno]["examhallcd"]          = VARS::post("EXAMHALLCD-".$examno);
            $this->updField[$examno]["examhallgroupcd"]     = VARS::post("EXAMHALLGROUPCD-".$examno);
            $this->updField[$examno]["examhallgroup_order"] = VARS::post("EXAMHALLGROUP_ORDER-".$examno);
       }

        //更新
        knjl031eQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
