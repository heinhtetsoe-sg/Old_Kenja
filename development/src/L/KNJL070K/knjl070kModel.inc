<?php

require_once('for_php7.php');

//メニュー画面
class knjl070kModel extends model
{
    var $cmd;                   //一覧表示時のcmd
    var $mode;
    var $testdiv;
    var $exam_type;
    var $field = array();
    var $year;
    
    function knjl070kModel()
    {
    }        
    function init()
    {
        $this->year = CTRL_YEAR + 1;
        $this->cmd      = VARS::request("cmd");

        if (VARS::get("examhallcd")){
            $this->examhallcd   = VARS::get("examhallcd");
        }
        if (VARS::post("TESTDIV")){
            $this->testdiv   = VARS::post("TESTDIV");
        }
        if (VARS::post("EXAM_TYPE")){
            $this->exam_type   = VARS::post("EXAM_TYPE");
        }
        if (VARS::request("mode")){
            $this->mode   = VARS::request("mode");
        }
        //会場名
        $this->field["EXAMHALL_NAME"] = VARS::post("EXAMHALL_NAME");
        //人数
        $this->field["CAPA_CNT"] = VARS::post("CAPA_CNT");
        //受験開始番号
        $this->field["S_EXAMNO"] = VARS::post("S_EXAMNO");
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
        if ($this->cmd == "delete"){
            if (!is_array($this->field["CHECKED"])){
                $this->warning = "チェックボックスが選択されていません。";
                return false;
            }
        }else if($this->cmd == "insert" || $this->cmd == "update" ){
            if (strlen($this->field["EXAMHALL_NAME"]) == 0 || !is_numeric($this->field["CAPA_CNT"])){
                $this->setWarning("MSG301");
                return false;
            }
            if (strlen($this->field["EXAMHALL_NAME"]) > 30){
                $this->setWarning("MSG914","(会場名)");
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
        $result = knjl070kQuery::update($this);
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
        $result = knjl070kQuery::insert($this);
        return $result;
    }
    // 割り振り実行のモデルを返します
    function &getExecModel()
    {
        $this->exec();
        $this->setMessage("MSG201");
    }
    function &exec()
    {
        //割り振り実行
        knjl070kQuery::exec($this);
    }    
    // 削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->delete();
            $this->setMessage("MSG201","再度割振りを実行して下さい。");
        }
    }
    function &delete()
    {
        $result = knjl070kQuery::delete($this);
        return $result;
    }
    // 受験番号クリアのモデルを返します
    function &getClearModel()
    {
        $this->clear();
        $this->setMessage("MSG201");
    }
    function &clear()
    {
        $result = knjl070kQuery::clear($this);
        return $result;
    }
}
?>
