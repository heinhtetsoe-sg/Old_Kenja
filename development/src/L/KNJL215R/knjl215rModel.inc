<?php

require_once('for_php7.php');

class knjl215rModel extends Model {
    var $testdiv;
    var $applicantdiv;
    var $cmd;
    var $examyear;
    var $fixApplicantDiv;  //入試制度(ログイン校種により固定)

    function init()
    {
        $this->examyear = CTRL_YEAR + 1;

        $this->cmd          = VARS::request("cmd");
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->testdiv      = VARS::post("TESTDIV");
        $this->fixApplicantDiv = '1';
        if (SCHOOLKIND == 'J') {
                $this->fixApplicantDiv = '2';
        }
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
            $ret = knjl215rQuery::ExecuteQuery($this);
            $this->setMessage("MSG201", "\\n事前相談未処理　" . $ret . "件");
        }
    }
}
?>
