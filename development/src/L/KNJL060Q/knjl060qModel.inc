<?php

require_once('for_php7.php');

class knjl060qModel extends Model
{
    public $testdiv;
    public $applicantdiv;
    public $cmd;
    public $examyear;

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
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }
        if (!strlen($this->testdiv)) {
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }
        //DB接続
        $db = Query::dbCheckOut();
        $recCnt = $db->getOne(knjl060qQuery::checkRecCnt($this));
        $scoCnt = $db->getOne(knjl060qQuery::checkScoCnt($this));
        if ($recCnt === 0 || $scoCnt === 0) {
            $this->setMessage("MSG303", "\\n対象者がいません。または、得点入力が行われていません。");
            return false;
        }
        //DB切断
        Query::dbCheckIn($db);
        return true;
    }

    public function &getExecModel()
    {
        if ($this->validateInput()) {
            if (SCHOOLKIND == "J" || SCHOOLKIND == "P") {
                $ret = knjl060qQuery::executeQueryJP($this);
            } else {
                $ret = knjl060qQuery::executeQueryH($this);
            }
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
?>
