<?php

require_once('for_php7.php');

//メニュー画面
class knjl030tModel extends model
{
    var $cmd;                   //一覧表示時のcmd
    var $mode;
    var $applicantdiv;
    var $exam_type;
    var $field = array();
    var $year;
    
    function knjl030tModel()
    {
    }        
    function init()
    {
        $this->year = CTRL_YEAR + 1;
        $this->cmd      = VARS::request("cmd");
        $this->exam_type = "1";

        if (VARS::get("PROGRAMID")){
            unset($this->examhallcd);
            unset($this->applicantdiv);
            unset($this->mode);
        }

        if (VARS::get("examhallcd")){
            $this->examhallcd   = VARS::get("examhallcd");
        }
        if (VARS::post("APPLICANTDIV")){
            $this->applicantdiv = VARS::post("APPLICANTDIV");
        }
        if (VARS::request("mode")){
            $this->mode   = VARS::request("mode");
        }
        //会場名
        $this->field["EXAMHALL_NAME"] = VARS::post("EXAMHALL_NAME");
        //人数
        $this->field["CAPA_CNT"] = VARS::post("CAPA_CNT");
        //開始座席番号
        $this->field["S_RECEPTNO"] = VARS::post("S_RECEPTNO");
        $this->field["CHECKED"] = VARS::post("CHECKED");
        if ($this->mode == "insert"){
            unset($this->examhallcd);
        }
    }
    /**
     * メイン表示用のモデルを返します
    */
    function &getMainModel()
    {
    }

    //入力項目チェック
    function validateInput()
    {
        if (!strlen($this->applicantdiv)) {
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }
        if ($this->cmd == "delete") {
            if (!is_array($this->field["CHECKED"])) {
                $this->warning = "チェックボックスが選択されていません。";
                return false;
            }
        } else if ($this->cmd == "insert" || $this->cmd == "update" ) {
            if (strlen($this->field["EXAMHALL_NAME"]) == 0 || 
                !is_numeric($this->field["CAPA_CNT"]) || 
                $this->field["CAPA_CNT"] == 0 || 
                strlen($this->field["S_RECEPTNO"]) == 0) 
            {
                $this->setWarning("MSG301","会場名、人数 ( 1以上 ) 、開始座席番号は必ず入力して下さい。");
                $this->setWarning("MSG301");
                return false;
            }
            if (strlen($this->field["EXAMHALL_NAME"]) > 30) {
                $this->setWarning("MSG914","( 会場名 )");
                return false;
            }
            //座席番号重複チェック
            $cnt = knjl030tQuery::receptnoCheck($this);
            if ($cnt > 0) {
                $this->setWarning("MSG302","人数または開始座席番号を確認して下さい。");
                return false;
            }
        }
        return true;
    }
    
    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
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

    function &update()
    {
        $result = knjl030tQuery::update($this);
        return $result;
    }
    // 編集完了用のモデルを返します
    function &getInsertModel()
    {
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

    function &insert()
    {
        $result = knjl030tQuery::insert($this);
        return $result;
    }
    // 削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->delete();
            $this->setMessage("MSG201");
        }
    }
    function &delete()
    {
        $result = knjl030tQuery::delete($this);
        return $result;
    }
}
?>
