<?php

require_once('for_php7.php');

class knjl281gModel extends model {
    var $cmd;
    var $ObjYear;
    var $applicantdiv;

    function knjl281gModel() {}

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->ObjYear          = CTRL_YEAR + 1;
        $this->applicantdiv     = VARS::post("APPLICANTDIV");
        $this->testPattern      = VARS::post("TEST_PATTERN");

        $this->hid_examno       = VARS::post("HID_EXAMNO");
    }

    function &getMainModel() {}

    function validateInput() {
        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        $this->schregno = array();

        //学籍番号を再配置
        $tmp = explode(",", $this->hid_examno);
        foreach ($tmp as $key => $examno) {
            $this->schregno[$examno] = VARS::post("SCHREGNO_{$examno}");
        }

        if ($this->validateInput()) {
            //更新
            $result = knjl281gQuery::getUpdateQuery($this);
            if ($result) {
                $this->setMessage("MSG201");
            }
        }
    }
}
?>
