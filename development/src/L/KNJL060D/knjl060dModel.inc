<?php

require_once('for_php7.php');

class knjl060dModel extends Model {
    var $cmd;
    var $ObjYear;
    var $applicantdiv;
    var $testdiv;
    var $exam_type;

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->testdiv);
        }

        $this->ObjYear      = CTRL_YEAR + 1;
        $this->applicantdiv = "1";  //固定値
        $this->exam_type    = "1";  //固定値

        $this->cmd          = VARS::request("cmd");
        $this->testdiv      = VARS::post("TESTDIV");
    }

    function &getMainModel() {}

    function validateInput() {
        //必須チェック
        if (!strlen($this->testdiv)) {
            $this->setWarning("MSG301", "( 受験種別 )");
            return false;
        }

        //DB接続
        $db = Query::dbCheckOut();
        $baseCnt = $db->getOne(knjl060dQuery::checkBaseCnt($this));
        if ($baseCnt == 0) {
            $this->setWarning("MSG303", "\\n対象者がいません。");
            Query::dbCheckIn($db);
            return false;
        }
        //DB切断
        Query::dbCheckIn($db);
        return true;
    }

    function &getExecModel() {
        if ($this->validateInput()) {
            $ret = knjl060dQuery::ExecuteQuery($this);
            if ($ret == DB_OK) {
                $this->setMessage("MSG202");
            } else if ($ret < 0) {
                $this->setWarning("MSG203", "\\nエラーが発生しました。しばらくして処理を再開してください。\\n( それでも解決しない場合は管理者へ連絡して下さい。)");
            } else {
                $this->setWarning("MSG303", "\\n対象者がいません。");
            }
        }
    }
}
?>
