<?php

require_once('for_php7.php');

class knjl130eModel extends Model {
    var $cmd;
    var $ObjYear;
    var $applicantdiv;
    var $exam_type;

    function init() {

        $this->ObjYear      = CTRL_YEAR + 1;
        $this->exam_type    = "1";  //固定値

        $this->cmd          = VARS::request("cmd");
        $this->applicantdiv = VARS::post("APPLICANTDIV");
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
        $row = $db->getRow(knjl130eQuery::checkAvgCnt($this), DB_FETCHMODE_ASSOC);
        $ret = true;
        if (empty($row)) {
            $this->setWarning("MSG303", "\\n序列確定処理が未処理です。");
            $ret = false;
        } else if ($row["COUNT_A"] == 0) {
            $this->setWarning("MSG303", "\\n{$row["NAME_A"]}の序列確定処理が未処理です。");
            $ret = false;
        } else if ($row["COUNT_B"] == 0) {
            $this->setWarning("MSG303", "\\n{$row["NAME_B"]}の序列確定処理が未処理です。");
            $ret = false;
        }
        //DB切断
        Query::dbCheckIn($db);
        return $ret;
    }

    function &getExecModel() {
        if ($this->validateInput()) {
            $ret = knjl130eQuery::ExecuteQuery($this);
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
