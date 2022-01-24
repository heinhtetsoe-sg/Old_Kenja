<?php

require_once('for_php7.php');
class knjl061jModel extends Model {
    var $testdiv;
    var $cmd;
    var $examyear;
    var $testsubclasscd_link = array();
    var $nonTestdiv = array();

    function knjl061jModel()
    {
        $this->examyear = CTRL_YEAR + 1;
    }

    function init()
    {
        $this->cmd          = VARS::request("cmd");
        $this->testdiv      = VARS::post("TESTDIV");
        $this->testsubclasscd_link = array();
        for ($i = 1; $i < 6; $i++) {
            $this->testsubclasscd_link[] = VARS::post("TESTSUBCLASSCD" .$i);
        }
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
            $ret = knjl061jQuery::ExecuteQuery($this);
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
