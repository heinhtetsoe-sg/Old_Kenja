<?php

require_once('for_php7.php');

class knjl073mModel extends Model {
    var $cmd;
    var $field = array();
    var $test_year;

    function knjl073mModel() {}

    function init() {
        $this->test_year = CTRL_YEAR + 1;
        $this->cmd = VARS::request("cmd");

        $this->field = $_POST;
    }

    function validateInput() {
        return true;
    }

    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjl073mQuery::updateEntexamApplicantbaseDat($this);
            $this->setMessage("MSG201");
        }
    }

    function &update() {}

    function clean() {}
}
?>
