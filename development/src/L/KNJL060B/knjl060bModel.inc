<?php

require_once('for_php7.php');

class knjl060bModel extends Model
{
    public $testdiv;
    public $applicantdiv;
    public $appHold;
    public $cmd;
    public $examyear;
    public $special_reason_div;

    public function init()
    {
        $this->examyear = CTRL_YEAR + 1;

        $this->cmd          = VARS::request("cmd");
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->appHold      = VARS::post("APP_HOLD");
        $this->testdiv      = VARS::post("TESTDIV");
        $this->special_reason_div = VARS::post("SPECIAL_REASON_DIV");
    }

    public function &getMainModel()
    {
    }

    public function validateInput()
    {
        if (!strlen($this->applicantdiv)) {
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }
        if (!strlen($this->testdiv)) {
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }
        return true;
    }

    public function &getExecModel()
    {
        if ($this->validateInput()) {
            $ret = knjl060bQuery::executeQuery($this);
            if ($ret == DB_OK) {
                $this->setMessage("MSG202");
            } elseif ($ret < 0) {
                $this->setWarning("MSG203", "\\nエラーが発生しました。しばらくして処理を再開してください。\\n( それでも解決しない場合は管理者へ連絡して下さい。)");
            } else {
                $this->setMessage("MSG303", "\\n対象者がいません。または、得点入力が行われていません。");
            }
        }
    }
}
