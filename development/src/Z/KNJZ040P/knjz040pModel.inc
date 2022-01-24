<?php

require_once('for_php7.php');

//メニュー画面
class knjz040pModel extends model {
    var $cmd;                   //一覧表示時のcmd
    var $year;                  //入試年度
    var $applicantdiv;          //primary
    var $testdiv;               //primary
    var $exam_type;             //primary
    var $examhallcd;            //primary
    var $field  = array();
    var $fields = array();

    function knjz040pModel() {
    }

    function init() {
        $this->cmd  = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;

        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->examhallcd);
            unset($this->testdiv);
            unset($this->applicantdiv);
            unset($this->exam_type);
            unset($this->fields);
        }

        //左画面コンボ変更時
        if (VARS::request("cmd") == "list2") {
            $this->fields = array("APPLICANTDIV"       =>  VARS::post("APPLICANTDIV"),        //入試区分
                                 );
        }

        //左フレームから取得
        if (VARS::get("EXAMHALLCD") != "") {
            $this->examhallcd   = VARS::get("EXAMHALLCD");
        }
        if (VARS::get("TESTDIV") != "") {
            $this->testdiv      = VARS::get("TESTDIV");
        }
        if (VARS::get("APPLICANTDIV") != "") {
            $this->fields["APPLICANTDIV"] = VARS::get("APPLICANTDIV");
        }

        $this->exam_type = "1";//固定値
        //入試区分
        $this->field["TESTDIV"]     = VARS::post("TESTDIV");
        //会場名
        $this->field["EXAMHALL_NAME"] = VARS::post("EXAMHALL_NAME");

    }

    //メイン表示用のモデルを返します
    function &getMainModel() {
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if (strlen($this->field["TESTDIV"]) == 0) {
            $this->setWarning("MSG310","( 入試区分 )");
            return false;
        }

        if ($this->cmd == "add" || $this->cmd == "update" ) {
            //空文字チェック
            if (strlen($this->field["EXAMHALL_NAME"]) == 0) {
                $this->setWarning("MSG301","( 会場名 )");
                return false;
            }
            if (strlen($this->field["EXAMHALL_NAME"]) > 30){
                $this->setWarning("MSG914","( 会場名 )");
                return false;
            }
        }

        if ($this->cmd == "update" || $this->cmd == "delete" ) {
            if ($this->examhallcd == "") {
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->testdiv != $this->field["TESTDIV"]) {
                $this->setWarning("MSG308");
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
            $this->setMessage("MSG201");
        }
    }
    function &update() {
        knjz040pQuery::update($this);
        return;
    }

    // 追加用のモデルを返します
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()) {
            $examhallcd = $this->insert();

            $db = Query::dbCheckOut();
            $db->autoCommit(false);
            $this->setMessage("MSG201");
            $this->testdiv      = $this->field["TESTDIV"];
            $this->examhallcd   = $examhallcd;
        }
    }
    function &insert() {
        $examhallcd = knjz040pQuery::insert($this);
        return $examhallcd;
    }

    // 削除用のモデルを返します
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->delete();
            $this->setMessage("MSG201");
            unset($this->testdiv);
            unset($this->examhallcd);
        }
    }
    function &delete() {
        knjz040pQuery::delete($this);
        return;
    }

    function clean() {
    }
}
?>
