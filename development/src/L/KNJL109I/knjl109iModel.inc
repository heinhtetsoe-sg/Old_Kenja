<?php
class knjl109iModel extends model {
    var $cmd;
    var $ObjYear;
    var $applicantdiv;
    var $testdiv;
    var $move_enter;

    function knjl109iModel() {}

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
        }

        $this->cmd = VARS::request("cmd");
        $this->objYear      = CTRL_YEAR+1;
        $this->applicantdiv = VARS::post("APPLICANTDIV");

    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        foreach ($this->examnoArray as $examno) {
            $postEntSchregno = VARS::post("ENT_SCHREGNO_{$examno}");
            $this->entSchregno[$examno] = ($postEntSchregno != "") ? sprintf("%08d", $postEntSchregno) : "";
        }

        knjl109iQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }

}
?>
