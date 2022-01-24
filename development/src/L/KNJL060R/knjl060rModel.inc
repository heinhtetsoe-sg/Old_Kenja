<?php

require_once('for_php7.php');

class knjl060rModel extends Model {
    var $testdiv;
    var $applicantdiv;
    var $appHold;
    var $cmd;
    var $examyear;

    function init()
    {
        $this->examyear = CTRL_YEAR + 1;

        $this->cmd          = VARS::request("cmd");
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->appHold      = VARS::post("APP_HOLD");
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
            if ($this->applicantdiv == '1') {
                $ret = knjl060rQuery::ExecuteQueryH($this);
            } else {
                $ret = knjl060rQuery::ExecuteQueryJ($this);
            }
            if ($ret == DB_OK) {
                $this->setMessage("MSG202");
            } else if ($ret < 0) {
                $this->setWarning("MSG203", "\\nエラーが発生しました。しばらくして処理を再開してください。\\n( それでも解決しない場合は管理者へ連絡して下さい。)");
            } else {
                $this->setMessage("MSG303", "\\n対象者がいません。または、得点入力が行われていません。");
            }
        }
    }
}
?>