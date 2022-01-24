<?php

require_once('for_php7.php');

class knjl060eModel extends Model {
    var $cmd;
    var $ObjYear;
    var $applicantdiv;
    var $nittei;
    var $exam_type;

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->nittei);
        }

        $this->ObjYear      = CTRL_YEAR + 1;
        $this->exam_type    = "1";  //固定値

        $this->cmd          = VARS::request("cmd");
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->nittei       = VARS::post("NITTEI");
    }

    function &getMainModel() {}

    function validateInput() {
        //必須チェック
        if (!strlen($this->applicantdiv)) {
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }

        //DB接続
        $db = Query::dbCheckOut();
        $baseCnt = $db->getOne(knjl060eQuery::checkBaseCnt($this));
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
            $ret = knjl060eQuery::ExecuteQuery($this);
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
