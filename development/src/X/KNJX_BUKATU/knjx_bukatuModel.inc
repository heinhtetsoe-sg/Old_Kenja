<?php

require_once('for_php7.php');

class knjx_bukatuModel extends Model {
    var $cmd;
    var $schregno = "";
    var $setField;

    function knjx_bukatuModel() {}

    function init() {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->schregno);
            unset($this->setField);
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("SETFIELD") != "") {
            $this->setField = VARS::get("SETFIELD");
        }

    }
}
?>
