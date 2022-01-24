<?php

require_once('for_php7.php');

class knjl060pModel extends Model {
    var $cmd;
    var $examyear;
    var $applicantdiv;
    var $testdiv;
    var $suisenFlg;

    function init() {
        $this->examyear = CTRL_YEAR + 1;

        $this->cmd          = VARS::request("cmd");
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->testdiv      = VARS::post("TESTDIV");
        //県内高校推薦か？
        $this->suisenFlg    = ($this->applicantdiv == "2" && $this->testdiv == "1") ? "1" : "";
    }

    function &getMainModel()
    {
    }

    function validateInput() {
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
        $recCnt = $db->getOne(knjl060pQuery::checkRecCnt($this));
        if ($this->suisenFlg == "1") {
            $sakubunCnt = $db->getOne(knjl060pQuery::checkSakubunCnt($this));
            $JissekiCnt = $db->getOne(knjl060pQuery::checkJissekiCnt($this));
            if ($recCnt === 0 || $sakubunCnt === 0 || $JissekiCnt === 0) {
                $this->setMessage("MSG303", "\\n対象者がいません。または、作文および実績入力が行われていません。");
                Query::dbCheckIn($db);
                return false;
            }
        } else {
            $scoCnt = $db->getOne(knjl060pQuery::checkScoCnt($this));
            if ($recCnt === 0 || $scoCnt === 0) {
                $this->setMessage("MSG303", "\\n対象者がいません。または、得点入力が行われていません。");
                Query::dbCheckIn($db);
                return false;
            }
        }
        //DB切断
        Query::dbCheckIn($db);
        return true;
    }

    function &getExecModel() {
        if ($this->validateInput()) {
            if ($this->suisenFlg == "1") {
                $ret = knjl060pQuery::ExecuteQuerySuisen($this);
            } else {
                $ret = knjl060pQuery::ExecuteQuery($this);
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
