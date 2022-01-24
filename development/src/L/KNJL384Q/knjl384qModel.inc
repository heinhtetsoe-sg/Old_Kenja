<?php

require_once('for_php7.php');

class knjl384qModel extends Model {
    var $field      = array();
    var $cmd;
    var $mockcd = "";        //primary
    var $grade = "";       //primary
    var $mockyear;
    var $copyyear;

    function knjl384qModel()
    {
    }

    function init()
    {
        $this->cmd          = VARS::request("cmd");
        $this->PLACECD = VARS::request("PLACE_CD") != "" ? VARS::request("PLACE_CD") : $this->PLACECD;
        
        $this->COPY_YEAR = VARS::post("COPY_YEAR");

        $this->field = array("SHORI"                =>  VARS::post("SHORI") != "" ? VARS::post("SHORI") : "1",
                             "PLACECD"              =>  VARS::post("PLACECD"),
                             "PLACEAREA"            =>  VARS::post("PLACEAREA"),
                             "PLACENAME_SHORT"      =>  VARS::post("PLACENAME_SHORT"),
                             "PLACENAME"            =>  VARS::post("PLACENAME"),
                             );
        if(VARS::request("PLACE_CD") != ""){
            $this->field["SHORI"] = 2;
        }
        //処理を新規にしたとき
        if($this->field["SHORI"] == "1" || $model->cmd == "edit3"){
            $this->field = array();
            $this->field["SHORI"] = "1";
            $this->PLACECD = "";
        }
    }

    //UPDATE
    function getUpdateModel()
    {
        //NULLをチェック
        if($this->field["PLACECD"] == "" || $this->field["PLACEAREA"] == "" || $this->field["PLACENAME_SHORT"] == "" || $this->field["PLACENAME"] == ""){
            $this->setMessage("すべての項目を入力してください。");
            $this->field["SHORI"] = "2";
            return false;
        }
        
        //DB接続
        $db = Query::dbCheckOut();
        //入れる前にエラーチェックしたい…
        if($this->field["PLACECD"] != $this->PLACECD){
            $this->setMessage("会場コードの変更は出来ません。");
            $this->field["SHORI"] = "2";
            return false;
            
        }
        

        $this->update = array();
        
        $this->update["REGISTERCD"] = STAFFCD;
        $this->update["UPDATED"] = "sysdate()";
        
        $this->update["PLACEAREA"] = $this->field["PLACEAREA"];
        $this->update["PLACENAME_SHORT"] = $this->field["PLACENAME_SHORT"];
        
        $this->update["PLACENAME"] = $this->field["PLACENAME"];
        
        $where = " WHERE PLACECD = '".$this->PLACECD."' AND YEAR = '".CTRL_YEAR."' ";
        
        common::_update("SAT_EXAM_PLACE_DAT", "", $where, $this->update, $db, "1");
        
        $this->setMessage("更新しました。");

        //DB切断
        Query::dbCheckIn($db);

    }
    
    //INSERT
    function getInsertModel()
    {
        $this->field = array("PLACECD"              =>  VARS::post("PLACECD"),
                             "PLACEAREA"            =>  VARS::post("PLACEAREA"),
                             "PLACENAME_SHORT"      =>  VARS::post("PLACENAME_SHORT"),
                             "PLACENAME"            =>  VARS::post("PLACENAME"),
                             );
        //NULLをチェック
        if($this->field["PLACECD"] == "" || $this->field["PLACEAREA"] == "" || $this->field["PLACENAME_SHORT"] == "" || $this->field["PLACENAME"] == ""){
            $this->setMessage("すべての項目を入力してください。");
            $this->field["SHORI"] = "2";
            return false;
        }
        
        //DB接続
        $db = Query::dbCheckOut();
        //入れる前にエラーチェックしたい…
        $checkQuery = knjl384qQuery::countCode($this->field["PLACECD"]);
        $checkCnt = $db->getOne($checkQuery);
        if($checkCnt > 0){
            $this->setMessage("会場コードが重複しています。");
            $this->field["SHORI"] = "1";
            return false;
        }
        
        $this->insert = array();
        
        $this->insert["REGISTERCD"] = STAFFCD;
        $this->insert["UPDATED"] = "sysdate()";
        $this->insert["YEAR"] = CTRL_YEAR;
        
        $this->insert["PLACEAREA"] = $this->field["PLACEAREA"];
        $this->insert["PLACENAME_SHORT"] = $this->field["PLACENAME_SHORT"];
        
        $this->insert["PLACENAME"] = $this->field["PLACENAME"];
        
        $this->insert["PLACECD"] = $this->field["PLACECD"];
        
        common::_update("SAT_EXAM_PLACE_DAT", "", "", $this->insert, $db, "2");
        
        $this->setMessage("追加しました。");
        
        $this->field["SHORI"] = "2";
        $this->PLACECD = $this->insert["PLACECD"];

        //DB切断
        Query::dbCheckIn($db);


    }

    //DELETE
    function getDeleteModel() {
        //DB接続
        $db = Query::dbCheckOut();

        $delquery = knjl384qQuery::delete($this->PLACECD);
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
        $checkQuery = knjl384qQuery::betweenCnt($field["JUKEN_NO_FROM"], $recno);
        $checkCnt = $db->getOne($checkQuery);
        if($checkCnt > 0){
            return false;
        }
        
        //他のにTOが含まれていないか
        $checkQuery2 = knjl384qQuery::betweenCnt($field["JUKEN_NO_TO"], $recno);
        $checkCnt2 = $db->getOne($checkQuery2);
        if($checkCnt2 > 0){
            return false;
        }
        
        //FROM・TOに他のを含んでいないか
        $checkQuery3 = knjl384qQuery::inCheck($field["JUKEN_NO_FROM"], $field["JUKEN_NO_TO"], $recno);
        $checkCnt3 = $db->getOne($checkQuery3);
        if($checkCnt3 > 0){
            return false;
        }
        
        if($recno == ""){
            //同じ会場・個人団体・郵送窓口のデータが存在していないか
            $checkQuery4 = knjl384qQuery::sameChack($field["PLACECD"], $field["IND_KUBUN"], $field["SEND_KUBUN"]);
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
        $query = knjl384qQuery::delYear();
        $db->query($query);
        
        //選択した年度のデータを取得
        $query = knjl384qQuery::getCopyBase($this->COPY_YEAR);
        $result = $db->query($query);
        

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $this->update = array();
            
            $this->update = $row;
            
            $this->update["YEAR"] = CTRL_YEAR;
            $this->update["REGISTERCD"] = STAFFCD;
            $this->update["UPDATED"] = "sysdate()";
            
            common::_update("SAT_EXAM_PLACE_DAT", "", "", $this->update, $db, "2");
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
        
        $contents = "会場コード,会場地区名,会場略称,会場名\n";
        
        $result = $db->query(knjl384qQuery::getList($this));

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            
            $contents .= $row["PLACECD"].",".$row["PLACEAREA"].",".$row["PLACENAME_SHORT"].",".$row["PLACENAME"]."\n";
            
            $header_data = true;
        }
        
        //DB切断
        Query::dbCheckIn($db);

        
        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv","試験会場一覧");
            common::downloadContents($contents, $filename);
            
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
