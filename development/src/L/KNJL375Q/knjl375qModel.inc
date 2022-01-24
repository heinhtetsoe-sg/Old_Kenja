<?php

require_once('for_php7.php');

class knjl375qModel extends Model {
    var $cmd;
    var $selectdata;    //新クラス（左リスト）
    var $selectdataLabel;    //新クラス（左リスト）
    var $control;

    function knjl375qModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->schregno = array();
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        
        
        $this->top_field = array("L_KUBUN"               =>  VARS::post("L_KUBUN") != "" ? VARS::post("L_KUBUN") : "1",
                             
                                 "L_KAMOKU"              =>  VARS::post("L_KAMOKU") != "" ? VARS::post("L_KAMOKU") : "1",
                             
                                 "L_KAISU"               =>  VARS::post("L_KAISU") != "" ? VARS::post("L_KAISU") : "1",
                                 
                                 "R_KAMOKU"              =>  VARS::post("R_KAMOKU"),
                             
                                 );
        $this->file = VARS::file("FILE");
        
        if(VARS::request("KUBUN") != ""){
            $this->req_field = array("KUBUN"             =>  VARS::request("KUBUN"),
                                        
                                        "KAMOKU"            =>  VARS::request("KAMOKU"),
                                        
                                        "KAISU"             =>  VARS::request("KAISU"),
                                        
                                       );
        }

    }

    function validateInput() {
        return true;
    }
    
    //上フレーム追加時
    function getImportModel()
    {
        //ファイル名取得
        /*$target = $_FILES['FILE']['name'];
        $fileInfo = pathinfo($target);
        $extension = $fileInfo['extension'];
        $extension = strtoupper($extension);

        //拡張子チェック
        if ($extension != "CSV") {
            $this->warning = "CSVファイルを指定して下さい。\\n(".$extension."ファイルの取込は出来ません。)";
            return false;
        }*/
        
        //エラー
        $this->error = array();
        

        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        if(get_count($this->error)>0){
            $cnm = "";
            $error = "";
            $this->warning = "データに不備があります。";
            
            foreach($this->error as $val){
                $this->warning .= "\\n行：".$val." 受験番号：".$this->data_arr[$val]["SAT_NO"];
            }

            return false;
        }else if(get_count($this->data_arr) > 0){
            //更新
            $this->UpdateSatExamDat();
            
            $this->warning = "更新しました";
            
            $this->top_field["R_KAMOKU"] = "";
            
            return true;
        }else{
            $this->warning = "データに不備があります。";
            return false;
        }
        
    }
    //ファイルの文字列の取得
    function GetFileString()
    {
        $temp_arr = array();
        
        $db = Query::dbCheckOut();
        
        if (is_uploaded_file($this->file["tmp_name"])) {

            $fp = fopen ($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            while (!feof ($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    //headerありにするなら
                    if ($line == 1) {
                        $header = true;
                        $line++;
                        continue;
                    }
                    
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    //$tmp    = common::csv2array($buffer);
                    $buffer = str_replace("\"", "",$buffer);
                    $buffer = str_replace("↓", "\n",$buffer);
                    $tmp = explode(",", $buffer);

                    
                    $data = array("SAT_NO"   =>  strlen(trim($tmp[4]))>5 ? substr(trim($tmp[4]), -5) : $tmp[4],
                                  "NAME"     =>  $tmp[5],
                                  "POINT1"    =>  $tmp[6],
                                  "POINT2"    =>  $tmp[7],
                                  );
                    
                    $temp_arr[$line] = $data;

                    //エラーチェック    データはすべて読み込む
                    if (!$this->errCheck($db, $data)) {
                        //エラーチェックだめだったら
                        $this->error[$i] = $line;
                        $i++;
                    }
                    $line++;
                }
            }
            fclose($fp);

        } else {
            $this->warning = "ファイル名が不正です。";
        }
        
        Query::dbCheckIn($db);
        
        return $temp_arr;
    }
    
    //エラーチェック
    function errCheck($db, $data)
    {
        $returnFlg = true;
        
        //SAT_NOがSAT_EXAM_DATに存在するか確認
        $query = knjl375qQuery::checkSatNo($data["SAT_NO"]);
        $cnt = $db->getOne($query);
        if($cnt > 0){
            return true;
        }else{
            return false;
        }
        
    }
    
    //SAT_EXAM_DAT更新
    function UpdateSatExamDat()
    {
        if($this->top_field["R_KAMOKU"] == "1"){
            $kamoku = "ENGLISH";
        } else if($this->top_field["R_KAMOKU"] == "2"){
            $kamoku = "JAPANESE";
        } else if($this->top_field["R_KAMOKU"] == "3"){
            $kamoku = "MATH";
        } else {
            $kamoku = "";
        }
        
        $db = Query::dbCheckOut();
        
        $this->update = array();
        
        $this->update["REGISTERCD"] = STAFFCD;
        $this->update["UPDATED"] = "sysdate()";
        
        foreach($this->data_arr as $key => $val){
            $this->update["SCORE_".$kamoku] = $val["POINT1"];
            $this->update["SCORE_".$kamoku."2"] = $val["POINT1"];
            
            $where = " WHERE SAT_NO = '".$val["SAT_NO"]."' AND YEAR = '".CTRL_YEAR."' ";
            
            common::_update("SAT_EXAM_DAT", "", $where, $this->update, $db, "1");
            
        }
        
        Query::dbCheckIn($db);
    }
    
    
    //下フレーム更新時
    function getUpdateModel() 
    {
        //データベース接続
        $db = Query::dbCheckOut();
        
        $this->update = array();

        $kyouka = array("1" =>  "ENGLISH", "2" => "JAPANESE", "3" => "MATH");
        if($this->req_field["KAISU"] != "1"){
            $kaisu = "2";
        }else{
            $kaisu = "";
        }
        $choice = $kyouka[$this->req_field["KAMOKU"]].$kaisu;
        
        $this->update["REGISTERCD"] = STAFFCD;
        $this->update["UPDATED"] = "sysdate()";
        
        //データ1件ごとに更新かける
        foreach($this->satNo as $key => $val){
            if(VARS::post("TOKUTEN{$val}") != ""){//中身が入っていたら更新
                $this->update["SCORE_{$choice}"] = VARS::post("TOKUTEN{$val}") != "" ? VARS::post("TOKUTEN{$val}") : " ";
                
                $where = " WHERE SAT_NO = '".$val."' AND YEAR = '".CTRL_YEAR."' ";
                
                common::_update("SAT_EXAM_DAT", "", $where, $this->update, $db, "1");
            }
        }

        //DB切断
        Query::dbCheckIn($db);

        $this->setMessage("更新しました。");
        return true;

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
        
        $cntQuery = knjl375qQuery::getJudgeCnt($field["JUDGE{$cnt}"], $pref, $in, $out, $commentno);
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
        
        $delQuery = knjl375qQuery::deleteComment($this->CommentNo);
        $db->query($delQuery);
        
        //DB切断
        Query::dbCheckIn($db);
        
        $this->setMessage('削除しました。');

        $this->CommentNo = "";
        $this->CountNo = "";
        return true;


    }
}
?>
