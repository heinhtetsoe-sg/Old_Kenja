<?php

require_once('for_php7.php');

//メニュー画面
class knjl230yModel extends model
{
    var $cmd;                   //一覧表示時のcmd
    var $mode;
    var $applicantdiv;
    var $appHold;
    var $testdiv;
    var $exam_type;
    var $field = array();
    var $year;
    var $selectdata;
    var $selectdata2;
    
    function knjl230yModel() {
    }

    function init() {
        $this->year = CTRL_YEAR + 1;
        $this->cmd      = VARS::request("cmd");
        $this->exam_type = "1";

        if (VARS::get("PROGRAMID")){
            unset($this->examhallcd);
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->mode);
        }

        if (VARS::get("examhallcd")){
            $this->examhallcd   = VARS::get("examhallcd");
        }
        if (VARS::post("APPLICANTDIV")){
            $this->applicantdiv = VARS::post("APPLICANTDIV");
        }
        if (VARS::post("APP_HOLD")){
            $this->appHold = VARS::post("APP_HOLD");
        }
        if (VARS::post("TESTDIV")){
            $this->testdiv   = VARS::post("TESTDIV");
        }
        if (VARS::request("mode")){
            $this->mode   = VARS::request("mode");
        }
        //グループ名
        $this->field["EXAMHALLCD"] = VARS::post("EXAMHALLCD");
        $this->field["EXAMHALL_NAME"] = VARS::post("EXAMHALL_NAME");
        $this->selectdata   = VARS::post("selectdata"); //対象者一覧
        $this->selectdata2  = VARS::post("selectdata2");//志願者一覧
        //削除
        $this->field["CHECKED"] = VARS::post("CHECKED");
        //追加
        if ($this->mode == "insert"){
            unset($this->examhallcd);
        }
    }
    /**
     * メイン表示用のモデルを返します
    */
    function &getMainModel() {
    }

    //入力項目チェック
    function validateInput() {
        if (!strlen($this->applicantdiv)) {
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }
        if (!strlen($this->testdiv)) {
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }
        if ($this->cmd == "delete") {
            if (!is_array($this->field["CHECKED"])) {
                $this->warning = "チェックボックスが選択されていません。";
                return false;
            }
        } else if ($this->cmd == "insert" || $this->cmd == "update") {
            if (!strlen($this->field["EXAMHALL_NAME"])) {
                $this->setWarning("MSG301","( グループ名 )");
                return false;
            }
            if (strlen($this->field["EXAMHALL_NAME"]) > 30) {
                $this->setWarning("MSG914","( グループ名 )");
                return false;
            }
            if (!strlen($this->field["EXAMHALLCD"])) {
                $this->setWarning("MSG301","( グループ番号 )");
                return false;
            }
            $this->field["EXAMHALLCD"] = sprintf("%04d", $this->field["EXAMHALLCD"]);
            //キー値変更チェック
            if ($this->cmd == "update" && $this->field["EXAMHALLCD"] != $this->examhallcd) {
                //$this->setWarning("MSG300", "更新はリストよりデータを選択してから行ってください。");
                $this->setWarning("MSG300", "グループ番号は変更できません。");
                return false;
            }
            //重複・存在チェック
            $cnt = knjl230yQuery::examhallcdCheck($this);
            //重複チェック
            if (0 < $cnt && $this->cmd == "insert") {
                $this->setWarning("MSG302");
                return false;
            }
            //存在チェック
            if (0 == $cnt && $this->cmd == "update") {
                $this->setWarning("MSG303");
                return false;
            }
        }
        return true;
    }
    
    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()){
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
        $result = knjl230yQuery::update($this);
        return $result;
    }
    // 編集完了用のモデルを返します
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
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

    function &insert() {
        $result = knjl230yQuery::insert($this);
        return $result;
    }
    // 削除用のモデルを返します
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput()){
            $this->delete();
            $this->setMessage("MSG201");
        }
    }
    function &delete() {
        $result = knjl230yQuery::delete($this);
        return $result;
    }
}
?>
