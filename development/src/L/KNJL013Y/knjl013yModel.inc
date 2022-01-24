<?php

require_once('for_php7.php');

class knjl013yModel extends Model {
    var $recept_div;
    var $cmd;
    var $ObjYear;           //入試年度
    var $field      = array();
    var $selectData    = array();

    function knjl013yModel()
    {
    }
    
    function init()
    {
        $this->field = array("APPLICANTDIV"	    =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"	        =>  VARS::post("TESTDIV"),          //入試区分
                             "YOBI"             =>  VARS::post("YOBI"),             //予備枚数
                             "APP_HOLD"	        =>  VARS::post("APP_HOLD")          //入試区分
                            );

        $this->recept_div   = "2";
        $this->ObjYear      = CTRL_YEAR+1;
        $this->selectData   = explode(",", VARS::post("selectdata"));
        $this->cmd          = VARS::request("cmd");
    }

    function &getMainModel()
    {
    }

    function validateInput()
    {
        if(!strlen($this->field["APPLICANTDIV"])){
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }
        if(!strlen($this->field["TESTDIV"])){
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }
        //受験番号を配列にセット（１件も無い場合、存在エラー）
        $this->examno = array();
        $this->examno = knjl013yQuery::existCheck($this);
        if (get_count($this->examno) == 0) {
            $this->setWarning("MSG303", "( 志願者基礎データ )");
            return false;
        }
        //重複データチェック(同じ受験番号が違う入試制度に在る場合、重複エラー）
        $existdata = knjl013yQuery::existCheck2($this);
        if (is_array($existdata)) {
            $msg  = "この受験番号は違う入試制度に既に登録されています。　　\\n\\n";
            $msg .= "座席番号 ： ".$existdata["RECEPTNO"]."\\n";
            $msg .= "受験番号 ： ".$existdata["EXAMNO"]."\\n";
            $msg .= "入試制度 ： ".$existdata["APPLICANTDIV"];
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
            knjl013yQuery::ExecuteQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
