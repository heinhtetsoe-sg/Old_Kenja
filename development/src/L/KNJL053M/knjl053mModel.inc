<?php

require_once('for_php7.php');

class knjl053mModel extends Model {
    var $cmd;
    var $field;
    var $year;  //入試年度

    function knjl053mModel() {}

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
        }

        $this->field = $_POST;

        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;
    }

    function validateInput() {
        return true;
    }

    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjl053mQuery::updateEntexamControlDat($this);
            $this->setMessage("MSG201");
        }
    }
}
?>