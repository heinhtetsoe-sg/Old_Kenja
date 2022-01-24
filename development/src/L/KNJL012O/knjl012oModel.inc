<?php

require_once('for_php7.php');

class knjl012oModel extends Model {
    var $testdiv;
    var $applicantdiv;
    var $cmd;
    var $ObjYear;           //入試年度

    function knjl012oModel()
    {
        $this->ObjYear = CTRL_YEAR+1;
    }
    
    function init()
    {
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
            $this->setWarning("MSG301", "( 出願区分 )");
            return false;
        }
        return true;
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        if($this->validateInput()){
            knjl012oQuery::ExecuteQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
