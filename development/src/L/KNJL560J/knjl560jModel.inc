<?php

require_once('for_php7.php');

class knjl560jModel extends Model {
    var $cmd;
    var $examyear;
    var $applicantdiv;
    var $testdiv;

    function init() {
        $this->examyear = CTRL_YEAR + 1;

        $this->cmd          = VARS::request("cmd");
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->testdiv      = VARS::post("TESTDIV");
    }

    function &getMainModel()
    {
    }

    function validateInput() {
        if (!strlen($this->applicantdiv)) {
            $this->setWarning("MSG301", "( 学校種別 )");
            return false;
        }
        if (!strlen($this->testdiv)) {
            $this->setWarning("MSG301", "( 入試種別 )");
            return false;
        }
        //DB接続
        $db = Query::dbCheckOut();
        $recCnt = $db->getOne(knjl560jQuery::checkRecCnt($this));
        $scoCnt = $db->getOne(knjl560jQuery::checkScoCnt($this));
        if ($recCnt === 0 || $scoCnt === 0) {
            $this->setMessage("MSG303", "\\n対象者がいません。または、得点入力が行われていません。");
            Query::dbCheckIn($db);
            return false;
        }
        //DB切断
        Query::dbCheckIn($db);
        return true;
    }

    function &getExecModel() {
        if ($this->validateInput()) {
            $ret = knjl560jQuery::ExecuteQuery($this);
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
