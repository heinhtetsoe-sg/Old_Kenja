<?php

require_once('for_php7.php');

class knjl660aModel extends Model
{
    public $cmd;
    public $examyear;
    public $applicantdiv;
    public $testdiv;

    public function init()
    {
        $this->examyear = CTRL_YEAR + 1;

        $this->cmd              = VARS::request("cmd");
        $this->applicantdiv     = VARS::post("APPLICANTDIV");
        $this->testdiv          = VARS::post("TESTDIV");

        if ($this->cmd == "exec") {
            $this->applicantdiv     = VARS::post("HIDDEN_APPLICANTDIV");
            $this->testdiv          = VARS::post("HIDDEN_TESTDIV");
        }
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

        $db = Query::dbCheckOut();
        $recCnt = $db->getOne(knjl660aQuery::checkRecCnt($this));
        $scoCnt = $db->getOne(knjl660aQuery::checkScoCnt($this));
        if ($recCnt == 0 || $scoCnt == 0) {
            $this->setWarning("MSG303", "\\n対象者がいません。または、得点入力が行われていません。");
            Query::dbCheckIn($db);
            return false;
        }
        Query::dbCheckIn($db);

        return true;
    }

    public function &getExecModel()
    {
        if ($this->validateInput()) {
            $ret = knjl660aQuery::ExecuteQuery($this);
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
