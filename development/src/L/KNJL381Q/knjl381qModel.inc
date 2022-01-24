<?php

require_once('for_php7.php');

class knjl381qModel extends Model {
    var $cmd;
    var $selectdata;    //新クラス（左リスト）
    var $selectdataLabel;    //新クラス（左リスト）
    var $control;

    function knjl381qModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->schregno = array();
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        
        //上フレーム
        $this->topRadio = VARS::post("Radio") != "" ? VARS::post("Radio") : "1";
        
        $this->top_field = array("MIN0"               =>  VARS::post("MIN0"),
                             
                                 "MAX0"                  =>  VARS::post("MAX0"),
                             
                                 );
        for($i=0;$i<2;$i++){
            $this->top_field["COMMENT0{$i}"] = VARS::post("COMMENT0{$i}");
        }
        

        //下フレーム
        $this->COPY_YEAR = VARS::post("COPY_YEAR");
        
        if(VARS::request("KAMOKU") != ""){
            $this->btmRadio = VARS::request("KAMOKU");
        }else{
            $this->btmRadio = $this->topRadio;
        }
        for($b=1;$b<=$this->dataCnt;$b++){
            $this->bottom_field["MIN{$b}"] =  VARS::post("MIN{$b}");
            $this->bottom_field["MAX{$b}"] =  VARS::post("MAX{$b}");
            
            //hiddenにあるCOMENT_NO
            $this->bottom_field["COMMENTNO{$b}"] = VARS::post("COMMENTNO{$b}");
            
            for($i=0;$i<2;$i++){
                $this->bottom_field["COMMENT{$b}{$i}"] = VARS::post("COMMENT{$b}{$i}");
            }
        }

    }

    function validateInput() {
        return true;
    }
    
    //上フレーム追加時
    function getInsertModel()
    {
        
        $this->insert = array();
        
        $this->insert["REGISTERCD"] = STAFFCD;
        $this->insert["UPDATED"] = "sysdate()";
        $this->insert["YEAR"] = CTRL_YEAR;
        
        if($this->topRadio == "1"){
            $table = "ENGLISH";
        }else if($this->topRadio == "2"){
            $table = "JAPANESE";
        }else{
            $table = "MATH";
        }
        
        //データベース接続
        $db = Query::dbCheckOut();
        
        //COMMENTNOの最大値取得
        $cmtMaxQuery = knjl381qQuery::getCmtMax($table);
        $cmtMax = $db->getOne($cmtMaxQuery);
        $this->insert["COMMENT_NO"] = $cmtMax + 1;
        
        $this->insert["SCORE_FROM"] = $this->top_field["MIN0"];
        $this->insert["SCORE_TO"] = $this->top_field["MAX0"];
        
        for($i=0;$i<2;$i++){
            $ii = $i+1;
            $this->insert["COMMENT{$ii}"] = $this->top_field["COMMENT0{$i}"];
        }
        
        $insertTable = "SAT_COMMENT_".$table."_DAT";
        
        common::_update($insertTable, "", "", $this->insert, $db, "2");
        
        //DB切断
        Query::dbCheckIn($db);
        
        
        if($this->ErrorCheck($insertTable)){
            $this->setMessage("追加しました。");
        }else{
            $this->setMessage("追加しました。\\n\\n点数の範囲に誤りがあります。\\n確認してください。");
        }
        $this->top_field = array();
        return true;
    }
    
    
    //下フレーム更新時
    function getUpdateModel() {

        
        $this->update = array();
        
        if($this->btmRadio == "1"){
            $table = "SAT_COMMENT_ENGLISH_DAT";
        }else if($this->btmRadio == "2"){
            $table = "SAT_COMMENT_JAPANESE_DAT";
        }else{
            $table = "SAT_COMMENT_MATH_DAT";
        }
        
        $this->update["REGISTERCD"] = STAFFCD;
        $this->update["UPDATED"] = "sysdate()";

        
        //データベース接続
        $db = Query::dbCheckOut();
        
        for($i=1;$i<=$this->dataCnt;$i++){
            
            if($this->bottom_field["MIN{$i}"] == "" && $this->bottom_field["MAX{$i}"] == ""){       //どっちも空だったら削除
                $delQuery = knjl381qQuery::deleteComment($table, $this->bottom_field["COMMENTNO{$i}"]);
                $db->query($delQuery);
            }else{
                
                $this->update["SCORE_FROM"] = $this->bottom_field["MIN{$i}"];
                $this->update["SCORE_TO"] = $this->bottom_field["MAX{$i}"];
                
                for($j=0;$j<2;$j++){
                    $jj = $j+1;
                    if($this->bottom_field["COMMENT{$i}{$j}"] != ""){
                        $this->update["COMMENT{$jj}"] = $this->bottom_field["COMMENT{$i}{$j}"];
                    }else{
                        $this->update["COMMENT{$jj}"] = " ";
                    }
                }
                
                $where = " WHERE COMMENT_NO = '".$this->bottom_field["COMMENTNO{$i}"]."' AND YEAR = '".CTRL_YEAR."' ";
                
                common::_update($table, "", $where, $this->update, $db, "1");
            }
        }
        
        //DB切断
        Query::dbCheckIn($db);
            
        if($this->ErrorCheck($table)){
            $this->setMessage("更新しました。");
        }else{
            $this->setMessage("更新しました。\\n\\n点数の範囲に誤りがあります。\\n確認してください。");
        }
        return true;
        
        
    }
    
    
    //エラーチェック
    function ErrorCheck($table){
        //データベース接続
        $db = Query::dbCheckOut();
        
        //データ取得
        $dataQuery = knjl381qQuery::getData($table);
        $dataResult = $db->query($dataQuery);
        $cnt = 0;
        $data = array();
        
        while($dataRow = $dataResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $data[$cnt]["MIN"] = $dataRow["SCORE_FROM"];
            $data[$cnt]["MAX"] = $dataRow["SCORE_TO"];
            
            $cnt++;
        }
        $errorCnt = 0;
        $count = get_count($data);
        $count = $count - 1;
        foreach($data as $key => $val){
            if($key < $count){
                $k = $key + 1;
                $max = $val["MAX"] + 1;
                if($max != $data[$k]["MIN"]){
                    $errorCnt++;
                    break;
                }
            }
        }
        
        //DB切断
        Query::dbCheckIn($db);
        
        if($errorCnt > 0){
            return false;
        }else{
            return true;
        }
    }
    
    
    //削除時
    function getDeleteModel()
    {
        //データベース接続
        $db = Query::dbCheckOut();
        
        $delQuery = knjl381qQuery::deleteComment($this->CommentNo);
        $db->query($delQuery);
        
        //DB切断
        Query::dbCheckIn($db);
        
        $this->setMessage('削除しました。');

        $this->CommentNo = "";
        $this->CountNo = "";
        return true;


    }

    //COPY
    function getCopyModel()
    {
        if($this->btmRadio == "1"){
            $table = "SAT_COMMENT_ENGLISH_DAT";
        }else if($this->btmRadio == "2"){
            $table = "SAT_COMMENT_JAPANESE_DAT";
        }else{
            $table = "SAT_COMMENT_MATH_DAT";
        }

        //DB接続
        $db = Query::dbCheckOut();
        
        //ログイン年度のデータを削除
        $query = knjl381qQuery::delYear($table);
        $db->query($query);
        
        //選択した年度のデータを取得
        $query = knjl381qQuery::getCopyBase($this->COPY_YEAR, $table);
        $result = $db->query($query);
        

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $this->update = array();
            
            $this->update = $row;
            
            $this->update["YEAR"] = CTRL_YEAR;
            $this->update["REGISTERCD"] = STAFFCD;
            $this->update["UPDATED"] = "sysdate()";
            
            common::_update($table, "", "", $this->update, $db, "2");
        }
        
        //DB切断
        Query::dbCheckIn($db);
        
        $this->setMessage("コピーしました。");
        
        
    }

}
?>
