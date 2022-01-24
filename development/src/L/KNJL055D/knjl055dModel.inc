<?php

require_once('for_php7.php');

class knjl055dModel extends Model {
    var $cmd;
    var $examyear;
    var $applicantdiv;
    var $testdiv;

    function init() {
        $this->examyear = CTRL_YEAR + 1;
        $this->applicantdiv = "1";  //固定値

        $this->cmd          = VARS::request("cmd");

        //フィールド
        $this->field = array("SUBCLASS_CD"              =>  VARS::post("SUBCLASS_CD")              //科目
                            );
    }

    function &getMainModel() {
    }

    function validateInput() {
        if (!strlen($this->field["SUBCLASS_CD"])) {
            $this->setWarning("MSG301", "( 科目 )");
            return false;
        }
        //DB接続
        $db = Query::dbCheckOut();
        $ansCnt = $db->getOne(knjl055dQuery::checkQueAnsCnt($this));
        if ($ansCnt == 0) {
            $this->setMessage("MSG303", "\\n対象データがありません。");
            Query::dbCheckIn($db);
            return false;
        }
        //DB切断
        Query::dbCheckIn($db);
        return true;
    }

    function &getExecModel() {
        if ($this->validateInput()) {
            if (knjl055dQuery::ExecuteQuery($this)) {
                $this->setMessage("MSG201");
            }
        }
    }
}
?>
