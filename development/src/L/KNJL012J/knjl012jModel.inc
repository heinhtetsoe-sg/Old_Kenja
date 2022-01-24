<?php

require_once('for_php7.php');

class knjl012jModel extends Model {
    var $testdiv;
    var $cmd;
    var $ObjYear;           //入試年度

    function knjl012jModel()
    {
        $this->ObjYear = CTRL_YEAR+1;
    }
    
    function init()
    {
        $this->cmd          = VARS::request("cmd");
        $this->testdiv      = VARS::post("TESTDIV");
    }

    function &getMainModel()
    {
    }

    function validateInput()
    {
        if(!strlen($this->testdiv)){
            $this->setWarning("MSG301", "( 入試区分 )");
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
            knjl012jQuery::ExecuteQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
