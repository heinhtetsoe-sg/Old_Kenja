<?php

require_once('for_php7.php');


class knjc300Model extends Model {
    var $cmd;
    var $control;   //コントロールマスタ
    var $add_days;
    var $katei;
    var $joken;
    var $hmrm;
    var $kamoku;
    var $selectdata;
    var $senddate;
    var $termdate;
    var $message1;
    var $message2;
    
    function knjc300Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        
    }
    
    //イニシャライズ
    function init()
    {
        if (VARS::get("PROGRAMID")) {
            
        }
        
        $this->add_days = VARS::post("NUM_DAYS") != "" ? VARS::post("NUM_DAYS") : "1";
        $this->hmrm     = VARS::post("C_HMRM")   != "" ? VARS::post("C_HMRM")   : "";
        
        $this->cmd          = VARS::request("cmd");
        $this->katei        = VARS::post("KATEI");
        $this->joken        = VARS::post("JOKEN");
        $this->selectdata   = VARS::post("selectdata");
        $this->hmrm         = VARS::post("C_HMRM");
        
        $this->field = array("T_NAME"           => VARS::post("T_NAME"),
                             "NUM_DAYS"         => VARS::post("NUM_DAYS"),
                             "D_TEXT1"          => VARS::post("D_TEXT1"),
                             "D_TEXT2"          => VARS::post("D_TEXT2"),
                             "C_HMRM"           => VARS::post("C_HMRM"),
                             "C_KAMK"           => VARS::post("C_KAMK"),
                             "C_KOZA"           => VARS::post("C_KOZA"),
                             "C_CLII"           => VARS::post("C_CLII")
                             );
        if ($this->cmd == "" || $this->cmd == "reset") {
            unset($this->field);
            unset($this->selectdata);
            unset($this->hmrm);
        }
        $this->getPropertiesAll(); //プロパティファイルの読込み
    }
    
    //更新用フィールド
    function getUpdateField()
    {
        
        $this->selectdata = VARS::post("selectdata");
        $this->tanto      = VARS::post("T_NAME");
        $this->message1   = VARS::post("D_TEXT1");
        $this->message2   = VARS::post("D_TEXT2");
        
    }
    
    //更新
    function getUpdateModel()
    {
        
        //更新用フィールド取得
        $this->getUpdateField();
        
        //DB接続
        $db     = Query::dbCheckOut();
        $db->autoCommit(false);
        
        //選択済みの番号を配列に取得
        $row = explode(',',$this->selectdata);
        
        foreach($row as $key => $val){
            $query = knjc300Query::getDengonInsQuery($db, $this, $val);
            $db->query($query);
        }
        
        $db->commit(); // トランザクションをコミットする。
        $this->setMessage("MSG201");
        //DB切断
        Query::dbCheckIn($db);
        
        unset($this->field);
    }

}
?>
