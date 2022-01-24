<?php

require_once('for_php7.php');

class knjl215qModel extends Model {
    var $testdiv;
    var $applicantdiv;
    var $cmd;
    var $examyear;
    var $clear_div;

    function init()
    {
        $this->examyear = CTRL_YEAR + 1;

        $this->cmd          = VARS::request("cmd");
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->testdiv      = VARS::post("TESTDIV");
        $this->clear_div    = VARS::post("CLEAR_DIV");
    }

    function &getMainModel()
    {
    }

    function validateInput()
    {
        if(!strlen($this->applicantdiv)){
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }
        if(!strlen($this->testdiv)){
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }
        return true;
    }

    function &getExecModel()
    {
        if($this->validateInput()){
            $ret = knjl215qQuery::ExecuteQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
