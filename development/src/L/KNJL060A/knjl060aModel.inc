<?php

require_once('for_php7.php');

class knjl060aModel extends Model
{
    public $cmd;
    public $examyear;
    public $applicantdiv;
    public $testdiv;

    public function init()
    {
        $this->examyear = CTRL_YEAR + 1;

        $this->cmd          = VARS::request("cmd");
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->testdiv      = VARS::post("TESTDIV");
    }

    public function &getMainModel()
    {
    }

    public function validateInput()
    {
        if (!strlen($this->applicantdiv)) {
            $this->setWarning("MSG301", "( 受験校種 )");
            return false;
        }
        if (!strlen($this->testdiv)) {
            $this->setWarning("MSG301", "( 試験区分 )");
            return false;
        }
        //DB接続
        $db = Query::dbCheckOut();
        $recCnt = $db->getOne(knjl060aQuery::checkRecCnt($this));
        $scoCnt = $db->getOne(knjl060aQuery::checkScoCnt($this));
        if ($recCnt == 0 || $scoCnt == 0) {
            $this->setMessage("MSG303", "\\n対象者がいません。または、得点入力が行われていません。");
            Query::dbCheckIn($db);
            return false;
        }
        //DB切断
        Query::dbCheckIn($db);
        return true;
    }

    public function &getExecModel()
    {
        if ($this->validateInput()) {
            $ret = knjl060aQuery::ExecuteQuery($this);
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
