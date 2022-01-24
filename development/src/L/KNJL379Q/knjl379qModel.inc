<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjl379qModel extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $file;
    var $control;           //コントロールマスタ
    var $staffcd;           //登録者コード
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knjl379qModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
    }
    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->COPY_YEAR = VARS::post("COPY_YEAR");

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if($this->cmd == ""){
            $this->field = array();
        }


    }
    function &getMainModel()
    {
    }


    
    //UPDATE
    function UpdateModel()
    {
        $db = Query::dbCheckOut();

        $judgeQuery = knjl379qQuery::getJudge();
        $judgeResult = $db->query($judgeQuery);
        
        $judge = array();
        
        while($judgeRow = $judgeResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $judge[] = $judgeRow["NAMECD2"];
        }
        
        foreach($judge as $key => $val){
            for($j=0;$j<4;$j++){
                $this->field["FROM_{$j}{$val}"] = VARS::post("FROM_{$j}{$val}");
                                     
                $this->field["TO_{$j}{$val}"] = VARS::post("TO_{$j}{$val}");
            }
        }
        

        
        
        $this->update["REGISTERCD"] = STAFFCD;
        $this->update["UPDATED"] = "sysdate()";
        
        foreach($judge as $key => $val){
            $this->update["JUDGE"] = $val;
            $i = $val;
            for($j=0;$j<4;$j++){
                $this->update["INOUT"] = $j + 1;
                
                $this->update["SCORE_FROM"] = $this->field["FROM_{$j}{$i}"] != "" ? $this->field["FROM_{$j}{$i}"] : " ";
                $this->update["SCORE_TO"] = $this->field["TO_{$j}{$i}"] != "" ? $this->field["TO_{$j}{$i}"] : " ";
                
                $where = " WHERE JUDGE = '".$this->update["JUDGE"]."' AND INOUT = '".$this->update["INOUT"]."' AND YEAR = '".CTRL_YEAR."' ";
                
                common::_update("SAT_JUDGE_STANDARD_DAT", "", $where, $this->update, $db, "0");
            }
        }
        
        $this->setMessage("更新しました。");
        
        $this->update = array();
        
        Query::dbCheckIn($db);
        
    }
    
    //COPY
    function getCopyModel()
    {
        //DB接続
        $db = Query::dbCheckOut();
        
        //ログイン年度のデータを削除
        $query = knjl379qQuery::delYear();
        $db->query($query);
        
        //選択した年度のデータを取得
        $query = knjl379qQuery::getCopyBase($this->COPY_YEAR);
        $result = $db->query($query);
        

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $this->update = array();
            
            $this->update = $row;
            
            $this->update["YEAR"] = CTRL_YEAR;
            $this->update["REGISTERCD"] = STAFFCD;
            $this->update["UPDATED"] = "sysdate()";
            
            common::_update("SAT_JUDGE_STANDARD_DAT", "", "", $this->update, $db, "2");
        }
        
        //DB切断
        Query::dbCheckIn($db);
        
        $this->setMessage("コピーしました。");
        
        
    }


}
//var_dump($_REQUEST);
?>
