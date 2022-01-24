<?php

require_once('for_php7.php');

class knjl074mModel extends Model {
    var $cmd;
    var $field = array();
    var $test_year;

    function knjl074mModel() {}

    function init() {
        $this->test_year = VARS::post("TEST_YEAR");
        if (!strlen($this->test_year)) $this->test_year = CTRL_YEAR + 1;
        $this->cmd = VARS::request("cmd");

        $this->field = $_POST;
    }

    function validateInput() {
        return true;
    }

    //更新
    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjl074mQuery::updateEntexamApplicantbaseDat($this);
            $this->setMessage("MSG201");
        }
    }

    function &update() {}

    function clean() {}
}
?>
