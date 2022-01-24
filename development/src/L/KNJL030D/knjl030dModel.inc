<?php

require_once('for_php7.php');

class knjl030dModel extends model {
    var $cmd;
    var $examyear;
    var $mode;
    var $testdiv;
    var $applicantdiv;
    var $exam_type;
    var $field = array();

    function knjl030dModel() {}

    function init() {
        $this->cmd      = VARS::request("cmd");
        $this->examyear = CTRL_YEAR + 1;

        if (VARS::get("PROGRAMID")) {
            unset($this->examhallcd);
            unset($this->testdiv);
            unset($this->applicantdiv);
            unset($this->exam_type);
            unset($this->mode);
        }

        $this->applicantdiv = "1";  //固定値
        $this->exam_type    = "1";  //固定値

        if (VARS::get("examhallcd")) {
            $this->examhallcd   = VARS::get("examhallcd");
        }
        if (VARS::post("TESTDIV")) {
            $this->testdiv   = VARS::post("TESTDIV");
        }
        if (VARS::request("mode")) {
            $this->mode   = VARS::request("mode");
        }
        //会場名
        $this->field["EXAMHALL_NAME"] = VARS::post("EXAMHALL_NAME");
        //人数
        $this->field["CAPA_CNT"]    = VARS::post("CAPA_CNT");
        $this->field["CHECKED"]     = VARS::post("CHECKED");
        if ($this->mode == "insert"){
            unset($this->examhallcd);
        }
    }

    function &getMainModel() {}

    //入力項目チェック
    function validateInput() {
        if (!strlen($this->testdiv)) {
            $this->setWarning("MSG301", "( 受験種別 )");
            return false;
        }
        if ($this->cmd == "delete") {
            if (!is_array($this->field["CHECKED"])) {
                $this->warning = "チェックボックスが選択されていません。";
                return false;
            }
        } else if ($this->cmd == "insert" || $this->cmd == "update") {
            if (strlen($this->field["EXAMHALL_NAME"]) == 0 || !is_numeric($this->field["CAPA_CNT"]) || $this->field["CAPA_CNT"] == 0) {
                $this->setWarning("MSG301","会場名、人数 ( 1以上 ) は必ず入力して下さい。");
                return false;
            }
            if (strlen($this->field["EXAMHALL_NAME"]) > 30) {
                $this->setWarning("MSG914","( 会場名 )");
                return false;
            }
        }
        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $msg = $this->errorMessage("MSG201");
            echo <<<EOP
                <script language="javascript">                
                    top.main_frame.closeit();
                    alert('$msg');
                    top.main_frame.document.forms[0].cmd.value = "main";
                    top.main_frame.document.forms[0].submit();
                </script>
            
EOP;
        }
    }

    function &update() {
        knjl030dQuery::update($this);
        return;
    }

    // 割り振り実行のモデルを返します
    function &getExecModel() {
        if ($this->validateInput()) {
            $this->exec();
            $this->setMessage("MSG201");
        }
    }

    function &exec() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //割り振り実行
        knjl030dQuery::exec($db, $this);
        $db->commit(); 
        Query::dbCheckIn($db);
    }

    // 削除用のモデルを返します
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->delete();
            $this->setMessage("MSG201");
        }
    }

    function &delete() {
        $result = knjl030dQuery::delete($this);
        return $result;
    }
}
?>
