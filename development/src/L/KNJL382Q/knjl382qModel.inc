<?php

require_once('for_php7.php');

class knjl382qModel extends Model {
    var $field      = array();
    var $cmd;
    var $mockcd = "";        //primary
    var $grade = "";       //primary
    var $mockyear;
    var $copyyear;

    function knjl382qModel()
    {
    }

    function init()
    {
        $this->cmd          = VARS::request("cmd");
        $this->left_field["SEARCH_PREF"] = VARS::post("SEARCH_PREF");
        if(VARS::request("SEARCH_PREF") != ""){
            $this->left_field["SEARCH_PREF"] = VARS::request("SEARCH_PREF");
        }
        $this->left_field["COPY_YEAR"] = VARS::post("COPY_YEAR");
        
        $this->GROUP_CD = VARS::request("GROUP_CD") != "" ? VARS::request("GROUP_CD") : $this->GROUP_CD;

        $this->right_field = array("SHORI"                  =>  VARS::post("SHORI") != "" ? VARS::post("SHORI") : "1",
                                   "GROUPCD"                =>  VARS::post("GROUPCD"),
                                   "GROUPNAME"              =>  VARS::post("GROUPNAME"),
                                   "GROUPZIP"               =>  VARS::post("GROUPZIP"),
                                   "GROUPPREF"              =>  VARS::post("GROUPPREF"),
                                   "GROUPADDR1"             =>  VARS::post("GROUPADDR1"),
                                   "GROUPADDR2"             =>  VARS::post("GROUPADDR2"),
                                   "GROUPTEL"               =>  VARS::post("GROUPTEL"),
                                   "GROUPSTAFF"             =>  VARS::post("GROUPSTAFF"),
                                  );
        if(VARS::request("GROUP_CD") != ""){
            $this->right_field["SHORI"] = 2;
        }
        //処理を新規にしたとき
        if($this->right_field["SHORI"] == "1" || $this->cmd == "edit3"){
            $this->right_field = array();
            $this->right_field["SHORI"] = "1";
            $this->GROUP_CD = "";
        }
    }

    //UPDATE
    function getUpdateModel()
    {

        //NULLをチェック
        if($this->right_field["GROUPCD"] == "" || $this->right_field["GROUPNAME"] == ""){
            $this->setMessage("団体コード・団体名は必ず入力してください。");
            $this->right_field["SHORI"] = "2";
            return false;
        }
        
        //入れる前にエラーチェックしたい…
        if($this->right_field["GROUPCD"] != $this->GROUP_CD){
            $this->setMessage("団体コードの変更は出来ません。");
            $this->right_field["SHORI"] = "2";
            return false;
            
        }
        //DB接続
        $db = Query::dbCheckOut();
        

        $this->update = array();
        
        $this->update["REGISTERCD"] = STAFFCD;
        $this->update["UPDATED"] = "sysdate()";
        
        $this->update["GROUPNAME"] = $this->right_field["GROUPNAME"];
        
        $this->update["GROUPZIP"]   = $this->right_field["GROUPZIP"] != "" ? $this->right_field["GROUPZIP"] : " ";
        $this->update["GROUPPREF"]  = $this->right_field["GROUPPREF"] != "" ? $this->right_field["GROUPPREF"] : " ";
        $this->update["GROUPADDR1"] = $this->right_field["GROUPADDR1"] != "" ? $this->right_field["GROUPADDR1"] : " ";
        $this->update["GROUPADDR2"] = $this->right_field["GROUPADDR2"] != "" ? $this->right_field["GROUPADDR2"] : " ";
        $this->update["GROUPTEL"]   = $this->right_field["GROUPTEL"] != "" ? $this->right_field["GROUPTEL"] : " ";
        $this->update["GROUPSTAFF"] = $this->right_field["GROUPSTAFF"] != "" ? $this->right_field["GROUPSTAFF"] : " ";
        
        $where = " WHERE GROUPCD = '".$this->GROUP_CD."' AND YEAR = '".CTRL_YEAR."' ";
        
        common::_update("SAT_GROUP_DAT", "", $where, $this->update, $db, "1");
        
        $this->setMessage("更新しました。");

        //DB切断
        Query::dbCheckIn($db);
        
        return true;

    }
    
    //INSERT
    function getInsertModel()
    {
        $this->right_field = array("GROUPCD"                =>  VARS::post("GROUPCD"),
                                   "GROUPNAME"              =>  VARS::post("GROUPNAME"),
                                   "GROUPZIP"               =>  VARS::post("GROUPZIP"),
                                   "GROUPPREF"              =>  VARS::post("GROUPPREF"),
                                   "GROUPADDR1"             =>  VARS::post("GROUPADDR1"),
                                   "GROUPADDR2"             =>  VARS::post("GROUPADDR2"),
                                   "GROUPTEL"               =>  VARS::post("GROUPTEL"),
                                   "GROUPSTAFF"             =>  VARS::post("GROUPSTAFF"),
                                  );
        //NULLをチェック
        if($this->right_field["GROUPCD"] == "" || $this->right_field["GROUPNAME"] == ""){
            $this->setMessage("団体コード・団体名は必ず入力してください。");
            $this->right_field["SHORI"] = "2";
            return false;
        }
        
        //DB接続
        $db = Query::dbCheckOut();
        //入れる前にエラーチェックしたい…
        $checkQuery = knjl382qQuery::countCode($this->right_field["GROUPCD"]);
        $checkCnt = $db->getOne($checkQuery);
        if($checkCnt > 0){
            $this->setMessage("団体コードが重複しています。");
            $this->right_field["SHORI"] = "1";
            return false;
        }
        
        $this->insert = array();
        
        $this->insert["REGISTERCD"] = STAFFCD;
        $this->insert["UPDATED"] = "sysdate()";
        $this->insert["YEAR"] = CTRL_YEAR;
        
        $this->insert["GROUPCD"] = $this->right_field["GROUPCD"];
        $this->insert["GROUPNAME"] = $this->right_field["GROUPNAME"];
        
        $this->insert["GROUPZIP"]   = $this->right_field["GROUPZIP"] != "" ? $this->right_field["GROUPZIP"] : " ";
        $this->insert["GROUPPREF"]  = $this->right_field["GROUPPREF"] != "" ? $this->right_field["GROUPPREF"] : " ";
        $this->insert["GROUPADDR1"] = $this->right_field["GROUPADDR1"] != "" ? $this->right_field["GROUPADDR1"] : " ";
        $this->insert["GROUPADDR2"] = $this->right_field["GROUPADDR2"] != "" ? $this->right_field["GROUPADDR2"] : " ";
        $this->insert["GROUPTEL"]   = $this->right_field["GROUPTEL"] != "" ? $this->right_field["GROUPTEL"] : " ";
        $this->insert["GROUPSTAFF"] = $this->right_field["GROUPSTAFF"] != "" ? $this->right_field["GROUPSTAFF"] : " ";
        
        common::_update("SAT_GROUP_DAT", "", "", $this->insert, $db, "2");
        
        $this->setMessage("追加しました。");
        
        $this->right_field["SHORI"] = "2";
        $this->GROUP_CD = $this->insert["GROUPCD"];

        //DB切断
        Query::dbCheckIn($db);
        
        return true;


    }

    //DELETE
    function getDeleteModel() {
        //DB接続
        $db = Query::dbCheckOut();

        $delquery = knjl382qQuery::delete($this->GROUP_CD);
        $db->query($delquery);
        
        $this->setMessage("削除しました。");
        
        $this->right_field["SHORI"] = "1";

        //DB切断
        Query::dbCheckIn($db);

    }

    //エラーチェック
    function ErrorCheck($db, $field, $recno)
    {
        //他のにFROMが含まれていないか
        $checkQuery = knjl382qQuery::betweenCnt($field["JUKEN_NO_FROM"], $recno);
        $checkCnt = $db->getOne($checkQuery);
        if($checkCnt > 0){
            return false;
        }
        
        //他のにTOが含まれていないか
        $checkQuery2 = knjl382qQuery::betweenCnt($field["JUKEN_NO_TO"], $recno);
        $checkCnt2 = $db->getOne($checkQuery2);
        if($checkCnt2 > 0){
            return false;
        }
        
        //FROM・TOに他のを含んでいないか
        $checkQuery3 = knjl382qQuery::inCheck($field["JUKEN_NO_FROM"], $field["JUKEN_NO_TO"], $recno);
        $checkCnt3 = $db->getOne($checkQuery3);
        if($checkCnt3 > 0){
            return false;
        }
        
        if($recno == ""){
            //同じ会場・個人団体・郵送窓口のデータが存在していないか
            $checkQuery4 = knjl382qQuery::sameChack($field["PLACECD"], $field["IND_KUBUN"], $field["SEND_KUBUN"]);
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
        $query = knjl382qQuery::delYear();
        $db->query($query);
        
        //選択した年度のデータを取得
        $query = knjl382qQuery::getCopyBase($this->left_field["COPY_YEAR"]);
        $result = $db->query($query);
        

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $this->update = array();
            
            $this->update = $row;
            
            $this->update["YEAR"] = CTRL_YEAR;
            $this->update["REGISTERCD"] = STAFFCD;
            $this->update["UPDATED"] = "sysdate()";
            
            common::_update("SAT_GROUP_DAT", "", "", $this->update, $db, "2");
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
        
        $contents = "コード,団体名\n";
        
        $result = $db->query(knjl382qQuery::getList($this));
        
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            
            $contents .= $row["GROUPCD"].",".$row["GROUPNAME"]."\n";
            
            $header_data = true;
        }
        
        
        //DB切断
        Query::dbCheckIn($db);

        
        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv","団体一覧");
            common::downloadContents($contents, $filename);
            
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
