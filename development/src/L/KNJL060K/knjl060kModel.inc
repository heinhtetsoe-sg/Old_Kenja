<?php

require_once('for_php7.php');
//メニュー画面
class knjl060kModel extends model
{
    var $cmd;                   //一覧表示時のcmd
    var $testdiv;
    var $year;
        
    function knjl060kModel()
    {
    }
    function init()
    {
        $this->year = CTRL_YEAR + 1;
        $this->cmd      = VARS::request("cmd");
        $this->testdiv   = VARS::post("TESTDIV");
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
        return true;
    }
    
    // 割り振り実行のモデルを返します
    function &getExecModel()
    {

       //氏名データ重複チェック 2005/11/14 
       $db = Query::dbCheckOut();
       $result = $db->query(knjl060kQuery::selectQueryRepeated_Chck($this));
       while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
       {
           $wk_msg .= $row["NAME"] ."   ";
       }

       if (is_array($wk_msg) || $wk_msg != ''){
           $this->setWarning("MSG302","（ 氏名：" . $wk_msg ."）");
           return false;
       }

        $this->exec();
        $this->setMessage("MSG201");

    }

    function &exec()
    {
        //割り振り実行
        knjl060kQuery::exec($this);
    }    
}
?>
