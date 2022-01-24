<?php

require_once('for_php7.php');

class knjl090kModel extends Model {
    var $testdiv;
    var $applicantdiv;
    var $cmd;
    var $examyear;
        
    function knjl090kModel()
    {
        $this->examyear = CTRL_YEAR + 1;
    }
    
    function init()
    {
        $this->cmd          = VARS::request("cmd");
        $this->testdiv      = VARS::post("TESTDIV");
        $this->procedurecd  = VARS::post("procedurecd");
        if ($this->procedurecd == "") {
            $this->procedurecd = "1";//初期値は得点処理
        }
    }

    function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        if (knjl090kQuery::ExecuteQuery($this)) {
            $this->setMessage("MSG202");
        }
    }
}
?>
