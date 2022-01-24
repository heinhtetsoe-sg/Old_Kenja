<?php
//メニュー画面
class knjl032yModel extends model
{
    var $cmd;                   //一覧表示時のcmd
    var $mode;
    var $applicantdiv;
    var $appHold;
    var $testdiv;
    var $field = array();
    var $year;
    
    function knjl032yModel()
    {
    }        
    function init()
    {
        $this->year = CTRL_YEAR + 1;
        $this->cmd      = VARS::request("cmd");

        if (VARS::get("PROGRAMID")){
            unset($this->pattern_no);
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->mode);
        }

        if (VARS::get("pattern_no")){
            $this->pattern_no   = VARS::get("pattern_no");
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
        //試験時間割名
        $this->field["PATTERN_NAME"] = VARS::post("PATTERN_NAME");
        //校時数
        $this->field["PERIOD_CNT"] = VARS::post("PERIOD_CNT");
        //校時・試験科目・時間
        if (strlen($this->field["PERIOD_CNT"]) && 0 < (int) $this->field["PERIOD_CNT"]) {
            for ($periodcd = 1; $periodcd <= (int) $this->field["PERIOD_CNT"]; $periodcd++) {
                $this->field["TESTSUBCLASSCD".$periodcd] = VARS::post("TESTSUBCLASSCD".$periodcd);
                $this->field["S_HOUR"        .$periodcd] = VARS::post("S_HOUR"        .$periodcd);
                $this->field["S_MINUTE"      .$periodcd] = VARS::post("S_MINUTE"      .$periodcd);
                $this->field["E_HOUR"        .$periodcd] = VARS::post("E_HOUR"        .$periodcd);
                $this->field["E_MINUTE"      .$periodcd] = VARS::post("E_MINUTE"      .$periodcd);
            }
        }
        $this->field["CHECKED"] = VARS::post("CHECKED");
        if ($this->mode == "insert"){
            unset($this->pattern_no);
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
        if (!strlen($this->testdiv)) {
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }
        if ($this->cmd == "delete") {
            if (!is_array($this->field["CHECKED"])) {
                $this->warning = "チェックボックスが選択されていません。";
                return false;
            }
        } else if ($this->cmd == "insert" || $this->cmd == "update" ) {
            if (!strlen($this->field["PATTERN_NAME"]) || !is_numeric($this->field["PERIOD_CNT"]) || $this->field["PERIOD_CNT"] == 0) {
                $this->setWarning("MSG301","試験時間割名、校時数 ( 1以上 ) は必ず入力して下さい。");
                return false;
            }
            if (strlen($this->field["PATTERN_NAME"]) > 60) {
                $this->setWarning("MSG914","( 試験時間割名 )");
                return false;
            }
            $checkTestSubclasscd = array();
            for ($periodcd = 1; $periodcd <= (int) $this->field["PERIOD_CNT"]; $periodcd++) {
                if (!strlen($this->field["TESTSUBCLASSCD".$periodcd])) {
                    $this->setWarning("MSG301","試験科目、時間 は必ず入力して下さい。");
                    return false;
                }
                if (!strlen($this->field["S_HOUR".$periodcd]) || !strlen($this->field["S_MINUTE".$periodcd]) || 
                    !strlen($this->field["E_HOUR".$periodcd]) || !strlen($this->field["E_MINUTE".$periodcd])) {
                    $this->setWarning("MSG301","試験科目、時間 は必ず入力して下さい。");
                    return false;
                }
                $checkTestSubclasscd[$this->field["TESTSUBCLASSCD".$periodcd]] = $periodcd;
            }
            if (count($checkTestSubclasscd) != $this->field["PERIOD_CNT"]) {
                $this->setWarning("MSG901","( 試験科目 )");
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
        $result = knjl032yQuery::update($this);
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
        $result = knjl032yQuery::insert($this);
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
        $result = knjl032yQuery::delete($this);
        return $result;
    }
}
?>
