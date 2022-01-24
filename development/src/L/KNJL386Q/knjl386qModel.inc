<?php

require_once('for_php7.php');

class knjl386qModel extends Model {
    var $field      = array();
    var $cmd;
    var $mockcd = "";        //primary
    var $grade = "";       //primary
    var $mockyear;
    var $copyyear;

    function knjl386qModel()
    {
    }

    function init()
    {
        $this->cmd          = VARS::request("cmd");
        $this->Recno = VARS::request("RECNO") != "" ? VARS::request("RECNO") : $this->Recno;

        $this->COPY_YEAR = VARS::post("COPY_YEAR");


        $this->field = array("SHORI"                =>  VARS::post("SHORI") != "" ? VARS::post("SHORI") : "1",
                             "INOUT_KUBUN"          =>  VARS::post("INOUT_KUBUN"),
                             "JUKEN_NO_FROM"        =>  VARS::post("JUKEN_NO_FROM"),
                             "JUKEN_NO_TO"          =>  VARS::post("JUKEN_NO_TO"),
                             "PLACECD"              =>  VARS::post("PLACE"),
                             "IND_KUBUN"            =>  VARS::post("IND_KUBUN"),
                             "SEND_KUBUN"           =>  VARS::post("SEND_KUBUN")
                             );
        if(VARS::request("RECNO") != ""){
            $this->field["SHORI"] = 2;
        }
        //処理を新規にしたとき
        if($this->field["SHORI"] == "1" || $model->cmd == "edit3"){
            $this->field = array();
            $this->field["SHORI"] = "1";
            $this->Recno = "";
        }
    }

    //UPDATE
    function getUpdateModel()
    {
        //NULLと番号前後をチェック
        if($this->field["JUKEN_NO_FROM"] > $this->field["JUKEN_NO_TO"]){
            $this->setMessage("番号が前後しています。");
            $this->field["SHORI"] = "2";
            return false;
        }
        if($this->field["INOUT_KUBUN"] == "" || $this->field["PLACECD"] == "" || $this->field["IND_KUBUN"] == ""
           || $this->field["SEND_KUBUN"] == "" || $this->field["JUKEN_NO_FROM"] == "" || $this->field["JUKEN_NO_TO"] == ""){
            $this->setMessage("すべての項目を入力してください。");
            $this->field["SHORI"] = "2";
            return false;
        }
        
        //DB接続
        $db = Query::dbCheckOut();
        //入れる前にエラーチェックしたい…
        if(!$this->ErrorCheck($db, $this->field, $this->Recno)){
            $this->setMessage("データが重複しています。");
            
            //DB切断
            Query::dbCheckIn($db);
            return false;
        }
        

        $this->update = array();
        
            $this->update["REGISTERCD"] = STAFFCD;
            $this->update["UPDATED"] = "sysdate()";
            
            $this->update["JUKEN_NO_FROM"] = $this->field["JUKEN_NO_FROM"];
            $this->update["JUKEN_NO_TO"] = $this->field["JUKEN_NO_TO"];
            
            $this->update["IND_KUBUN"] = $this->field["IND_KUBUN"];
            $this->update["INOUT_KUBUN"] = $this->field["INOUT_KUBUN"];
            $this->update["SEND_KUBUN"] = $this->field["SEND_KUBUN"];
            
            $this->update["PLACECD"] = $this->field["PLACECD"];
            
            $where = " WHERE RECNO = '".$this->Recno."' AND YEAR = '".CTRL_YEAR."' ";
            
            common::_update("SAT_EXAM_NO_DAT", "", $where, $this->update, $db, "1");
            
            $this->setMessage("更新しました。");

        //DB切断
        Query::dbCheckIn($db);

    }
    
    //INSERT
    function getInsertModel()
    {
        $this->field = array("INOUT_KUBUN"          =>  VARS::post("INOUT_KUBUN"),
                             "JUKEN_NO_FROM"        =>  VARS::post("JUKEN_NO_FROM"),
                             "JUKEN_NO_TO"          =>  VARS::post("JUKEN_NO_TO"),
                             "PLACECD"              =>  VARS::post("PLACE"),
                             "IND_KUBUN"            =>  VARS::post("IND_KUBUN"),
                             "SEND_KUBUN"           =>  VARS::post("SEND_KUBUN")
                             );
        //NULLと番号前後をチェック
        if($this->field["JUKEN_NO_FROM"] > $this->field["JUKEN_NO_TO"]){
            $this->setMessage("番号が前後しています。");
            $this->field["SHORI"] = "1";
            return false;
        }
        if($this->field["INOUT_KUBUN"] == "" || $this->field["PLACECD"] == "" || $this->field["IND_KUBUN"] == "" || $this->field["SEND_KUBUN"] == ""){
            $this->setMessage("すべての項目を入力してください。");
            $this->field["SHORI"] = "1";
            return false;
        }
        
        
        //DB接続
        $db = Query::dbCheckOut();
        //入れる前にエラーチェックしたい…
        if(!$this->ErrorCheck($db, $this->field, "")){
            $this->setMessage("データが重複しています。");
            $this->field["SHORI"] = "1";
                
            //DB切断
            Query::dbCheckIn($db);

            return false;
        }


        $this->insert = array();
        
        //RECNOの最大値を取得する
        $recMaxQuery = knjl386qQuery::getRecMax();
        $recMax = $db->getOne($recMaxQuery);
        
        $this->insert["RECNO"] = $recMax + 1;
        
        $this->insert["REGISTERCD"] = STAFFCD;
        $this->insert["UPDATED"] = "sysdate()";
        $this->insert["YEAR"] = CTRL_YEAR;
        
        //入れる前にエラーチェックしたい…
        $this->insert["JUKEN_NO_FROM"] = $this->field["JUKEN_NO_FROM"];
        $this->insert["JUKEN_NO_TO"] = $this->field["JUKEN_NO_TO"];
        
        $this->insert["IND_KUBUN"] = $this->field["IND_KUBUN"];
        $this->insert["INOUT_KUBUN"] = $this->field["INOUT_KUBUN"];
        $this->insert["SEND_KUBUN"] = $this->field["SEND_KUBUN"];
        
        $this->insert["PLACECD"] = $this->field["PLACECD"];
        
        common::_update("SAT_EXAM_NO_DAT", "", "", $this->insert, $db, "2");
        
        $this->setMessage("追加しました。");
        
        $this->field["SHORI"] = "2";
        $this->Recno = $this->insert["RECNO"];

        //DB切断
        Query::dbCheckIn($db);


    }

    //DELETE
    function getDeleteModel() {
        //DB接続
        $db = Query::dbCheckOut();

        $delquery = knjl386qQuery::delete($this->Recno);
        $db->query($delquery);
        
        $this->setMessage("削除しました。");
        
        $this->field["SHORI"] = "1";

        //DB切断
        Query::dbCheckIn($db);

    }

    //エラーチェック
    function ErrorCheck($db, $field, $recno)
    {
        //他のにFROMが含まれていないか
        $checkQuery = knjl386qQuery::betweenCnt($field["JUKEN_NO_FROM"], $recno);
        $checkCnt = $db->getOne($checkQuery);
        if($checkCnt > 0){
            return false;
        }
        
        //他のにTOが含まれていないか
        $checkQuery2 = knjl386qQuery::betweenCnt($field["JUKEN_NO_TO"], $recno);
        $checkCnt2 = $db->getOne($checkQuery2);
        if($checkCnt2 > 0){
            return false;
        }
        
        //FROM・TOに他のを含んでいないか
        $checkQuery3 = knjl386qQuery::inCheck($field["JUKEN_NO_FROM"], $field["JUKEN_NO_TO"], $recno);
        $checkCnt3 = $db->getOne($checkQuery3);
        if($checkCnt3 > 0){
            return false;
        }
        
        if($recno == ""){
            //同じ会場・個人団体・郵送窓口のデータが存在していないか
            $checkQuery4 = knjl386qQuery::sameChack($field["PLACECD"], $field["IND_KUBUN"], $field["SEND_KUBUN"]);
            $checkCnt4 = $db->getOne($checkQuery4);
            if($checkCnt4 > 0){
                return false;
            }
            
        }
        
        return true;
    }
    
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

    //COPY
    function getCopyModel()
    {
        //DB接続
        $db = Query::dbCheckOut();
        
        //ログイン年度のデータを削除
        $query = knjl386qQuery::delYear();
        $db->query($query);
        
        //選択した年度のデータを取得
        $query = knjl386qQuery::getCopyBase($this->COPY_YEAR);
        $result = $db->query($query);
        

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $this->update = array();
            
            $this->update = $row;
            
            $this->update["YEAR"] = CTRL_YEAR;
            $this->update["REGISTERCD"] = STAFFCD;
            $this->update["UPDATED"] = "sysdate()";
            
            common::_update("SAT_EXAM_NO_DAT", "", "", $this->update, $db, "2");
        }
        
        //DB切断
        Query::dbCheckIn($db);
        
        $this->setMessage("コピーしました。");
        
        
    }

    
    //CSV出力
    function getCsvModel()
    {
        //DB接続
        $db = Query::dbCheckOut();
        
        $header_data = "";
        
        $contents = "国内外,受験番号範囲,,会場名,個人/団体,郵送/窓口\n";
        
        $result = $db->query(knjl386qQuery::getList($model));

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            
            $contents .= $row["INOUT"].",".$row["JUKEN_NO_FROM"].",".$row["JUKEN_NO_TO"].",".$row["PLACE"].",".$row["IND"].",".$row["SEND"]."\n";
            
            $header_data = true;
        }
        
        //DB切断
        Query::dbCheckIn($db);

        
        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv","受験番号帯一覧");
            common::downloadContents($contents, $filename);
            
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

}
?>
