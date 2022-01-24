<?php

require_once('for_php7.php');

class knjl060jModel extends Model {
    var $testdiv;
    var $cmd;
    var $examyear;
        
    function knjl060jModel()
    {
        $this->examyear = CTRL_YEAR + 1;
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
            $ret = knjl060jQuery::ExecuteQuery($this);
            if ($ret == DB_OK) {
                $this->setMessage("MSG202");
            } else if ($ret < 0) {
                $this->setWarning("MSG203", "\\nエラーが発生しました。しばらくして処理を再開してください。\\n( それでも解決しない場合は管理者へ連絡して下さい。)");
            } else {
                $this->setMessage("MSG303", "\\n対象者がいません。");
            }
        }
    }
}
?>
