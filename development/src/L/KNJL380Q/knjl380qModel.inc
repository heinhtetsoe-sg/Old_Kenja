<?php

require_once('for_php7.php');

class knjl380qModel extends Model {
    var $cmd;
    var $selectdata;    //新クラス（左リスト）
    var $selectdataLabel;    //新クラス（左リスト）
    var $control;

    function knjl380qModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->schregno = array();
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");

        $this->COPY_YEAR = VARS::post("COPY_YEAR");

        
        $this->top_field = array("JUDGE0"               =>  VARS::post("JUDGE0"),
                             
                                 "CHECK0"                  =>  VARS::post("CHECK0"),
                             
                                 "NOTE0"                  =>  VARS::post("NOTE0"),
                             
                                 );
        for($i=0;$i<7;$i++){
            $this->top_field["COMMENT0{$i}"] = VARS::post("COMMENT0{$i}");
        }
        
        
        $this->CommentNo = VARS::post("COMMENT_NO");
        $this->CountNo = VARS::post("COUNT_NO");
        
        for($b=1;$b<=$this->dataCnt;$b++){
            $this->bottom_field["JUDGE{$b}"] =  VARS::post("JUDGE{$b}");
            $this->bottom_field["CHECK{$b}"] =  VARS::post("CHECK{$b}");
            $this->bottom_field["NOTE{$b}"] =  VARS::post("NOTE{$b}");
            
            for($i=0;$i<7;$i++){
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
        if($this->top_field["JUDGE0"] == "" || empty($this->top_field["CHECK0"])){
            $this->setMessage("項目を選択してください。");
            return false;
        }
        
        $this->insert = array();
        
        $this->insert["REGISTERCD"] = STAFFCD;
        $this->insert["UPDATED"] = "sysdate()";
        $this->insert["YEAR"] = CTRL_YEAR;

        if($this->ErrorCheck($this->top_field, 0, "")){
            //データベース接続
            $db = Query::dbCheckOut();
            
            //COMMENTNOの最大値取得
            $cmtMaxQuery = knjl380qQuery::getCmtMax();
            $cmtMax = $db->getOne($cmtMaxQuery);
            $this->insert["COMMENTNO"] = $cmtMax + 1;
            
            $this->insert["JUDGE"] = $this->top_field["JUDGE0"];
            
            //PREF_FLG、IN_FLG、OUT_FLG
            if(in_array("0",$this->top_field["CHECK0"])){
                $pref = 1;
            }else{
                $pref = "0";
            }
            if(in_array("1",$this->top_field["CHECK0"])){
                $in = 1;
            }else{
                $in = "0";
            }
            if(in_array("2",$this->top_field["CHECK0"])){
                $out = 1;
            }else{
                $out = "0";
            }
            
            $this->insert["PREF_FLG"] = $pref;
            $this->insert["IN_FLG"] = $in;
            $this->insert["OUT_FLG"] = $out;
            
            $this->insert["NOTE"] = $this->top_field["NOTE0"];

            for($i=0;$i<3;$i++){
                $ii = $i+1;
                $this->insert["COMMENT{$ii}"] = $this->top_field["COMMENT0{$i}"];
            }
            //TYPE=1のデータ作成
            $this->insert["TYPE"] = "1";
            
            common::_update("SAT_COMMENT_JUDGE_DAT", "", "", $this->insert, $db, "2");
            
            for($i=3;$i<7;$i++){
                $ii = $i-2;
                $this->insert["COMMENT{$ii}"] = $this->top_field["COMMENT0{$i}"];
            }
            //TYPE=2のデータ作成
            $this->insert["COMMENTNO"] = $cmtMax + 2;
            $this->insert["TYPE"] = "2";
            
            common::_update("SAT_COMMENT_JUDGE_DAT", "", "", $this->insert, $db, "2");
            
            //DB切断
            Query::dbCheckIn($db);
            
            $this->setMessage("追加しました。");
            $this->top_field = array();
            return true;
        }else{
            $this->setMessage("データが重複しています。");
            return false;
        }
        
    }
    
    
    //下フレーム更新時
    function getUpdateModel() {

        if($this->bottom_field["JUDGE{$this->CountNo}"] == "" || empty($this->bottom_field["CHECK{$this->CountNo}"])){
            $this->setMessage("項目を選択してください。");
            return false;
        }
        
        $this->update = array();
        
        $this->update["REGISTERCD"] = STAFFCD;
        $this->update["UPDATED"] = "sysdate()";

        if($this->ErrorCheck($this->bottom_field, $this->CountNo, $this->CommentNo)){
            //$this->CommentNoを2つに分ける
            $commentNo = array();
            $commentNo = explode(",", $this->CommentNo);
            
            //データベース接続
            $db = Query::dbCheckOut();
            
            $this->update["JUDGE"] = $this->bottom_field["JUDGE{$this->CountNo}"];
            
            //PREF_FLG、IN_FLG、OUT_FLG
            if(in_array("0",$this->bottom_field["CHECK{$this->CountNo}"])){
                $pref = 1;
            }else{
                $pref = "0";
            }
            if(in_array("1",$this->bottom_field["CHECK{$this->CountNo}"])){
                $in = 1;
            }else{
                $in = "0";
            }
            if(in_array("2",$this->bottom_field["CHECK{$this->CountNo}"])){
                $out = 1;
            }else{
                $out = "0";
            }
            
            $this->update["PREF_FLG"] = $pref;
            $this->update["IN_FLG"] = $in;
            $this->update["OUT_FLG"] = $out;
            
            if($this->bottom_field["NOTE{$this->CountNo}"] != ""){
                $this->update["NOTE"] = $this->bottom_field["NOTE{$this->CountNo}"];
            }else{
                $this->update["NOTE"] = " ";
            }
            
            for($i=0;$i<3;$i++){
                $ii = $i+1;
                if($this->bottom_field["COMMENT{$this->CountNo}{$i}"] != ""){
                    $this->update["COMMENT{$ii}"] = $this->bottom_field["COMMENT{$this->CountNo}{$i}"];
                }else{
                    $this->update["COMMENT{$ii}"] = " ";
                }
            }
            
            //TYPE = 1のデータを更新
            $where = " WHERE COMMENTNO = '".$commentNo[0]."' AND YEAR = '".CTRL_YEAR."' ";
            
            common::_update("SAT_COMMENT_JUDGE_DAT", "", $where, $this->update, $db, "1");
            
            for($i=3;$i<7;$i++){
                $ii = $i-2;
                if($this->bottom_field["COMMENT{$this->CountNo}{$i}"] != ""){
                    $this->update["COMMENT{$ii}"] = $this->bottom_field["COMMENT{$this->CountNo}{$i}"];
                }else{
                    $this->update["COMMENT{$ii}"] = " ";
                }
            }
            //TYPE = 2のデータを更新
            $where = " WHERE COMMENTNO = '".$commentNo[1]."' AND YEAR = '".CTRL_YEAR."' ";
            
            common::_update("SAT_COMMENT_JUDGE_DAT", "", $where, $this->update, $db, "1");
            
            //DB切断
            Query::dbCheckIn($db);
            
            $this->setMessage("更新しました。");
            $this->CommentNo = "";
            $this->CountNo = "";
            return true;
        }else{
            $this->setMessage("データが重複しています。");
            $this->CommentNo = "";
            $this->CountNo = "";
            return false;
        }
        
        
    }
    
    
    //エラーチェック
    function ErrorCheck($field, $cnt, $commentno){
        //データベース接続
        $db = Query::dbCheckOut();

        //JUDGEとPREF_FLG、IN_FLG、OUT_FLGの条件が重複しているものをカウントする。
        if(in_array("0",$field["CHECK{$cnt}"])){
            $pref = 1;
        }else{
            $pref = "";
        }
        if(in_array("1",$field["CHECK{$cnt}"])){
            $in = 1;
        }else{
            $in = "";
        }
        if(in_array("2",$field["CHECK{$cnt}"])){
            $out = 1;
        }else{
            $out = "";
        }
        
        $cntQuery = knjl380qQuery::getJudgeCnt($field["JUDGE{$cnt}"], $pref, $in, $out, $commentno);
        $errorCnt = $db->getOne($cntQuery);
        
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
        
        $delQuery = knjl380qQuery::deleteComment($this->CommentNo);
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
        //DB接続
        $db = Query::dbCheckOut();
        
        //ログイン年度のデータを削除
        $query = knjl380qQuery::delYear();
        $db->query($query);
        
        //選択した年度のデータを取得
        $query = knjl380qQuery::getCopyBase($this->COPY_YEAR);
        $result = $db->query($query);
        

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $this->update = array();
            
            $this->update = $row;
            
            $this->update["YEAR"] = CTRL_YEAR;
            $this->update["REGISTERCD"] = STAFFCD;
            $this->update["UPDATED"] = "sysdate()";
            
            common::_update("SAT_COMMENT_JUDGE_DAT", "", "", $this->update, $db, "2");
        }
        
        //DB切断
        Query::dbCheckIn($db);
        
        $this->setMessage("コピーしました。");
        
        
    }
}
?>
