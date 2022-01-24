<?php

require_once('for_php7.php');

class knjl012cModel extends Model {
    var $testdiv;
    var $applicantdiv;
    var $cmd;
    var $ObjYear;

    function knjl012cModel()
    {
    }
    
    function init()
    {
        $this->ObjYear = CTRL_YEAR+1;

        $this->cmd          = VARS::request("cmd");
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->testdiv      = VARS::post("TESTDIV");
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
            knjl012cQuery::ExecuteQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
