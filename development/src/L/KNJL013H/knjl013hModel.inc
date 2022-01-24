<?php

require_once('for_php7.php');

class knjl013hModel extends Model {
    var $testdiv;
    var $applicantdiv;
    var $recept_div;
    var $cmd;
    var $ObjYear;           //入試年度

    function knjl013hModel()
    {
    }
    
    function init()
    {
        $this->ObjYear = CTRL_YEAR+1;

        $this->cmd          = VARS::request("cmd");
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->testdiv      = VARS::post("TESTDIV");
        $this->recept_div   = VARS::post("RECEPT_DIV");
    }

    function &getMainModel()
    {
    }

    function validateInput()
    {
        if(!strlen($this->applicantdiv)){
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }
        if(!strlen($this->testdiv)){
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }
        //受験番号を配列にセット（１件も無い場合、存在エラー）
        $this->examno = array();
        $this->examno = knjl013hQuery::existCheck($this);
        if (get_count($this->examno) == 0) {
            $this->setWarning("MSG303", "( 志願者基礎データ )");
            return false;
        }
        //重複データチェック(同じ受験番号が違う入試制度に在る場合、重複エラー）
        $existdata = knjl013hQuery::existCheck2($this);
        if (is_array($existdata)) {
            $msg  = "この受験番号は違う入試制度に既に登録されています。　　\\n\\n";
            $msg .= "座席番号 ： ".$existdata["RECEPTNO"]."\\n";
            $msg .= "受験番号 ： ".$existdata["EXAMNO"]."\\n";
            $msg .= "入試制度 ： ".$existdata["APPLICANTDIV"]."\\n";
            $msg .= "入試区分 ： ".$existdata["TESTDIV"];
            $this->setWarning("MSG302", $msg);
            return false;
        }
        return true;
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        if($this->validateInput()){
            knjl013hQuery::ExecuteQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
