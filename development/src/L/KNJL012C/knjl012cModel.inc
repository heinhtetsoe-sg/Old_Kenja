<?php

require_once('for_php7.php');

class knjl012cModel extends Model {
    var $testdiv;
    var $applicantdiv;
    var $cmd;
    var $ObjYear;
    var $isCollege;
    var $isGojou;

    function knjl012cModel()
    {
    }
    
    function init()
    {
        $this->ObjYear = CTRL_YEAR+1;

        $this->cmd          = VARS::request("cmd");
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->testdiv      = VARS::post("TESTDIV");

        //学校コードを取得：カレッジを判断するため
        $rtnRow = knjl012cQuery::getNameMstZ010();
        $this->isCollege = $rtnRow["NAME2"] == "30290086001" ? true : false;
        $this->isGojou   = $rtnRow["NAME2"] == "30290053001" ? true : false;
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
