<?php

require_once('for_php7.php');

class knjc310Model extends Model {
    
    var $cmd;                    //コマンド
    var $control;                //コントロールマスタ
    var $auth;
    var $receptno;
    var $chgno;
    var $seno;
    var $field = array();
    var $meisai = array();
    var $addMeisai = array();
    
    function knjc310Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }
    
    //イニシャライズ
    function init()
    {
        $this->cmd     = VARS::request("cmd");
        $this->joken   = VARS::post("JOKEN") != "" ? VARS::post("JOKEN")   : "1";
        
        $this->field = array("C_STAFF"          => VARS::post("C_STAFF"));
        
        if ($this->cmd == ""){
            $this->field["C_STAFF"] = STAFFCD;
        }
    }
    
    //削除処理
    function getDeleteModel() {
        if ($this->validateInput($this->cmd)){
            
            //DB接続
            $db     = Query::dbCheckOut();
            $db->autoCommit(false);
            
            //削除用フィールド取得
            //個別削除
            if ($this->cmd == "delete"){
                $this->delete    = VARS::post("DELCHK");
            }
            //全件削除
            if ($this->cmd == "deleteAll"){
                $this->delete    = VARS::post("MESSAGENO");
            }
            
            if (is_array($this->delete)) {
                foreach ($this->delete as $key => $val) {
                    //echo $key." = ".$val."<BR>";
                    $MESSAGENO = $val;
                    $query = knjc310Query::getDeleteQuery($MESSAGENO);
                    $db->query($query);
                }
            }
            
            $db->commit(); // トランザクションをコミットする。
            $this->setMessage("MSG201");
            //DB切断
            Query::dbCheckIn($db);
        }
    }
    
    //入力項目チェック　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　
    function validateInput()
    {
        if ($this->cmd == "delete"){
            $this->delete    = VARS::post("DELCHK");
            $deldata = "false";
            
            if (is_array($this->delete)) {
                foreach ($this->delete as $key => $val) {
                    $deldata = "true";
                }
            }
            if ($deldata == "false"){
                $this->setWarning("選択されてません！！！");
                return false;
            }
        }
        //エラーなし
        return true;
    }
    
}
?>
