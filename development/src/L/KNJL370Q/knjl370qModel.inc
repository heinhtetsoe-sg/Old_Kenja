<?php

require_once('for_php7.php');

class knjl370qModel extends Model {
    var $field      = array();
    var $cmd;
    var $mockcd = "";        //primary
    var $grade = "";       //primary
    var $mockyear;
    var $copyyear;

    function knjl370qModel()
    {
    }

    function init()
    {
        $this->cmd          = VARS::request("cmd");
        if($this->cmd == ""){
            $this->left_field = array();
        }
        $this->left_field = array( "EXAMNO"                =>  VARS::post("EXAMNO"),
                                   "NAME_SEI"              =>  VARS::post("NAMESEI"),
                                   "NAME_MEI"              =>  VARS::post("NAMEMEI"),
                                   "PLACECD"               =>  VARS::post("PLACECD"),
                                   "SCHOOLCD"              =>  VARS::post("SCHOOLCD"),
                                   "GROUPCD"               =>  VARS::post("GROUPCD"),
                                  );

        $this->SAT_NO = VARS::request("SAT_NO") != "" ? VARS::request("SAT_NO") : $this->SAT_NO;

        $this->right_field = array("SHORI"                  =>  VARS::post("SHORI") != "" ? VARS::post("SHORI") : "1",
                                   "EXAM_NO"                =>  VARS::post("EXAM_NO"),
                                   "NAME_SEI"               =>  VARS::post("NAME_SEI"),
                                   "NAME_MEI"               =>  VARS::post("NAME_MEI"),
                                   "NAME_KANA_SEI"          =>  VARS::post("NAME_KANA_SEI"),
                                   "NAME_KANA_MEI"          =>  VARS::post("NAME_KANA_MEI"),
                                   "SEX"                    =>  VARS::post("SEX"),
                                   "BIRTHDAY"               =>  VARS::post("BIRTHDAY"),
                                   "GRADUATE"               =>  VARS::post("GRADUATE"),
                                   "SCHOOL_PREFCD"          =>  VARS::post("SCHOOL_PREFCD"),
                                   "SCHOOLCD"               =>  VARS::post("SCHOOLCD"),
                                   "SEARCH_SCHOOL"          =>  VARS::post("SEARCH_SCHOOL"),
                                   
                                   "ZIPCD"                  =>  VARS::post("ZIPCD"),
                                   "ADDR1"                  =>  VARS::post("ADDR1"),
                                   "ADDR2"                  =>  VARS::post("ADDR2"),
                                   "TELNO"                  =>  VARS::post("TELNO"),
                                   "EXAMPLACECD"            =>  VARS::post("EXAMPLACECD"),
                                   "GROUPCD"                =>  VARS::post("GROUPCD"),
                                   "SEARCH_GROUP"           =>  VARS::post("SEARCH_GROUP"),
                                   "FROM_PREFCD"            =>  VARS::post("FROM_PREFCD"),
                                   "IN_STUDENTNO"           =>  VARS::post("IN_STUDENTNO"),
                                   "INPUT_DATE"             =>  VARS::post("INPUT_DATE") != "" ? VARS::post("INPUT_DATE") : date("Y-m-d"),
                                  );
                                  
        if(VARS::request("SAT_NO") != ""){
            $this->right_field["SHORI"] = 2;
            $this->search = array();
            $this->search["EXAM"] = VARS::request("EXAM");
            $this->search["SEI"] = VARS::request("SEI");
            $this->search["MEI"] = VARS::request("MEI");
            $this->search["PLACE"] = VARS::request("PLACE");
            $this->search["SCHOOL"] = VARS::request("SCHOOL");
            $this->search["GROUP"] = VARS::request("GROUP");
            
        }
        //処理を新規にしたとき
        if(($this->right_field["SHORI"] == "1" && $this->cmd != "change"  && $this->cmd != "change1" && $this->cmd != "change2") || $this->cmd == "edit3"){
            $this->right_field = array();
            $this->right_field["GROUPCD"] = VARS::post("GROUPCD") == "" ? "" : VARS::post("GROUPCD");
            $this->right_field["SHORI"] = "1";
            $this->right_field["INPUT_DATE"] = str_replace("-","/",date("Y-m-d"));
            $this->SAT_NO = "";
        }
    }

    //UPDATE
    function getUpdateModel()
    {

        //NULLをチェック
        if($this->right_field["NAME_SEI"] == "" || $this->right_field["NAME_MEI"] == ""){
            $this->setMessage("氏名は必ず入力してください。");
            $this->right_field["SHORI"] = "2";
            return false;
        }
        
        //入れる前にエラーチェック
        if($this->right_field["EXAM_NO"] != $this->SAT_NO){
            $this->setMessage("受験番号の変更は出来ません。");
            $this->right_field["SHORI"] = "2";
            return false;
            
        }
        //DB接続
        $db = Query::dbCheckOut();
        

        $this->update = array();
        
        $this->update["REGISTERCD"] = STAFFCD;
        $this->update["UPDATED"] = "sysdate()";
        
        $this->update["NAME1"] = $this->right_field["NAME_SEI"]."　".$this->right_field["NAME_MEI"];
        $this->update["NAME2"] = $this->right_field["NAME_SEI"].$this->right_field["NAME_MEI"];
        $this->update["LASTNAME"] = $this->right_field["NAME_SEI"];
        $this->update["FIRSTNAME"] = $this->right_field["NAME_MEI"];
        
        $this->update["KANA1"] = $this->right_field["NAME_KANA_SEI"] != "" ? $this->right_field["NAME_KANA_SEI"] : " ";
        $this->update["KANA2"] = $this->right_field["NAME_KANA_MEI"] != "" ? $this->right_field["NAME_KANA_MEI"] : " ";
        
        $this->update["SEX"] = $this->right_field["SEX"] != "" ? $this->right_field["SEX"] : " ";
        
        $this->update["SCHOOLCD"] = $this->right_field["SCHOOLCD"] != "" ? $this->right_field["SCHOOLCD"] : " ";
        
        $this->update["BIRTHDAY"] = $this->right_field["BIRTHDAY"] != "" ? str_replace("/", "-", $this->right_field["BIRTHDAY"]) : " ";
        
        $this->update["GRADUATION"] = $this->right_field["GRADUATE"] != "" ? $this->right_field["GRADUATE"] : " ";
        
        $this->update["ZIPCODE"] = $this->right_field["ZIPCD"] != "" ? str_replace("-","",$this->right_field["ZIPCD"]) : " ";
        
        $this->update["ADDR1"] = $this->right_field["ADDR1"] != "" ? $this->right_field["ADDR1"] : " ";
        
        $this->update["ADDR2"] = $this->right_field["ADDR2"] != "" ? $this->right_field["ADDR2"] : " ";
        
        $this->update["TELNO1"] = $this->right_field["TELNO"] != "" ? $this->right_field["TELNO"] : " ";
        $this->update["TELNO2"] = $this->right_field["TELNO"] != "" ? str_replace("-", "", $this->right_field["TELNO"]) : " ";
                
        $this->update["PREFCD"]   = $this->right_field["FROM_PREFCD"] != "" ? $this->right_field["FROM_PREFCD"] : " ";
        
        $this->update["GROUPCD"]  = $this->right_field["GROUPCD"] != "" ? $this->right_field["GROUPCD"] : " ";
        
        $this->update["PLACECD"] = $this->right_field["EXAMPLACECD"] != "" ? $this->right_field["EXAMPLACECD"] : " ";
        
        $this->update["INSIDERNO"] = $this->right_field["IN_STUDENTNO"] != "" ? $this->right_field["IN_STUDENTNO"] : " ";
        
        $this->update["INPUT_DATE"] = $this->right_field["INPUT_DATE"] != "" ? str_replace("/", "-", $this->right_field["INPUT_DATE"]) : " ";
        
        
        $where = " WHERE SAT_NO = '".$this->SAT_NO."' AND YEAR = '".CTRL_YEAR."' ";
        
        common::_update("SAT_APP_FORM_MST", "", $where, $this->update, $db, "1");
        
        $this->setMessage("更新しました。");

        //DB切断
        Query::dbCheckIn($db);
        
        return true;

    }
    
    //INSERT
    function getInsertModel()
    {
        $this->right_field = array("EXAM_NO"                =>  VARS::post("EXAM_NO"),
                                   "NAME_SEI"               =>  VARS::post("NAME_SEI"),
                                   "NAME_MEI"               =>  VARS::post("NAME_MEI"),
                                   "NAME_KANA_SEI"          =>  VARS::post("NAME_KANA_SEI"),
                                   "NAME_KANA_MEI"          =>  VARS::post("NAME_KANA_MEI"),
                                   "SEX"                    =>  VARS::post("SEX"),
                                   "BIRTHDAY"               =>  VARS::post("BIRTHDAY"),
                                   "GRADUATE"               =>  VARS::post("GRADUATE"),
                                   "SCHOOL_PREFCD"          =>  VARS::post("SCHOOL_PREFCD"),
                                   "SCHOOLCD"               =>  VARS::post("SCHOOLCD"),
                                   
                                   "ZIPCD"                  =>  VARS::post("ZIPCD"),
                                   "ADDR1"                  =>  VARS::post("ADDR1"),
                                   "ADDR2"                  =>  VARS::post("ADDR2"),
                                   "TELNO"                  =>  VARS::post("TELNO"),
                                   "EXAMPLACECD"            =>  VARS::post("EXAMPLACECD"),
                                   "GROUPCD"                =>  VARS::post("GROUPCD"),
                                   "FROM_PREFCD"            =>  VARS::post("FROM_PREFCD"),
                                   "IN_STUDENTNO"           =>  VARS::post("IN_STUDENTNO"),
                                   "INPUT_DATE"             =>  VARS::post("INPUT_DATE") != "" ? VARS::post("INPUT_DATE") : date("Y-m-d"),
                                  );

        //NULLをチェック
        if($this->right_field["NAME_SEI"] == "" || $this->right_field["NAME_MEI"] == ""){
            $this->setMessage("氏名は必ず入力してください。");
            $this->right_field["SHORI"] = "1";
            return false;
        }
        
        //DB接続
        $db = Query::dbCheckOut();

        //入れる前にエラーチェック
        $cntQuery = knjl370qQuery::getCnt($this->right_field["EXAM_NO"]);
        $cnt = $db->getOne($cntQuery);
        if($cnt > 0){
            $this->setMessage("受験番号が重複しています。");
            $this->right_field["SHORI"] = "1";
            return false;
        }else{
            //受験番号帯に登録されているか
            $checkQuery = knjl370qQuery::getNoGroup($this->right_field["EXAM_NO"], "1");
            $checkCnt = $db->getOne($checkQuery);
            
            if($checkCnt == 0){
                $this->setMessage("入力した受験番号が登録されている受験番号帯に含まれていません。");
                return false;
            }
        }

        $this->insert = array();
        
        $this->insert["REGISTERCD"] = STAFFCD;
        $this->insert["UPDATED"] = "sysdate()";
        
        $this->insert["YEAR"] = CTRL_YEAR;
        $this->insert["SAT_NO"] = $this->right_field["EXAM_NO"];
        
        //受験区分取得
        $kubunQuery =knjl370qQuery::getNoGroup($this->right_field["EXAM_NO"]);
        $kubunRow = $db->getRow($kubunQuery, DB_FETCHMODE_ASSOC);
        $this->insert["INOUT_KUBUN"] = $kubunRow["INOUT_KUBUN"];
        $this->insert["SEND_KUBUN"] = $kubunRow["SEND_KUBUN"];
        $this->insert["IND_KUBUN"] = $kubunRow["IND_KUBUN"];
        
        
        $this->insert["NAME1"] = $this->right_field["NAME_SEI"]."　".$this->right_field["NAME_MEI"];
        $this->insert["NAME2"] = $this->right_field["NAME_SEI"].$this->right_field["NAME_MEI"];
        $this->insert["LASTNAME"] = $this->right_field["NAME_SEI"];
        $this->insert["FIRSTNAME"] = $this->right_field["NAME_MEI"];
        
        $this->insert["KANA1"] = $this->right_field["NAME_KANA_SEI"] != "" ? $this->right_field["NAME_KANA_SEI"] : " ";
        $this->insert["KANA2"] = $this->right_field["NAME_KANA_MEI"] != "" ? $this->right_field["NAME_KANA_MEI"] : " ";
        
        $this->insert["SEX"] = $this->right_field["SEX"] != "" ? $this->right_field["SEX"] : " ";
        
        $this->insert["SCHOOLCD"] = $this->right_field["SCHOOLCD"] != "" ? $this->right_field["SCHOOLCD"] : " ";
        
        $this->insert["BIRTHDAY"] = $this->right_field["BIRTHDAY"] != "" ? str_replace("/", "-", $this->right_field["BIRTHDAY"]) : " ";
        
        $this->insert["GRADUATION"] = $this->right_field["GRADUATE"] != "" ? $this->right_field["GRADUATE"] : " ";
        
        $this->insert["ZIPCODE"] = $this->right_field["ZIPCD"] != "" ? str_replace("-","",$this->right_field["ZIPCD"]) : " ";
        
        $this->insert["ADDR1"] = $this->right_field["ADDR1"] != "" ? $this->right_field["ADDR1"] : " ";
        
        $this->insert["ADDR2"] = $this->right_field["ADDR2"] != "" ? $this->right_field["ADDR2"] : " ";
        
        $this->insert["TELNO1"] = $this->right_field["TELNO"] != "" ? $this->right_field["TELNO"] : " ";
        $this->insert["TELNO2"] = $this->right_field["TELNO"] != "" ? str_replace("-", "", $this->right_field["TELNO"]) : " ";
                
        $this->insert["PREFCD"]   = $this->right_field["FROM_PREFCD"] != "" ? $this->right_field["FROM_PREFCD"] : " ";
        
        $this->insert["GROUPCD"]  = $this->right_field["GROUPCD"] != "" ? $this->right_field["GROUPCD"] : " ";
        
        $this->insert["PLACECD"] = $this->right_field["EXAMPLACECD"] != "" ? $this->right_field["EXAMPLACECD"] : " ";
        
        $this->insert["ABSENCE"] = "1";
        
        $this->insert["INSIDERNO"] = $this->right_field["IN_STUDENTNO"] != "" ? $this->right_field["IN_STUDENTNO"] : " ";
        
        $this->insert["INPUT_DATE"] = $this->right_field["INPUT_DATE"] != "" ? str_replace("/", "-", $this->right_field["INPUT_DATE"]) : " ";
        
        common::_update("SAT_APP_FORM_MST", "", "", $this->insert, $db, "2");
        
        //SAT_EXAM_DATも作成したい
        $this->examInsert["REGISTERCD"] = STAFFCD;
        $this->examInsert["UPDATED"] = "sysdate()";
        $this->examInsert["YEAR"] = CTRL_YEAR;
        $this->examInsert["SAT_NO"] = $this->insert["SAT_NO"];
        $this->examInsert["ABSENCE"] = "1";
        $this->examInsert["ABSENCE_ENGLISH"] = "1";
        $this->examInsert["ABSENCE_MATH"] = "1";
        $this->examInsert["ABSENCE_JAPANESE"] = "1";
        if($this->insert["PLACECD"] == "80" && $this->insert["SCHOOLCD"] == "2008005"){ //駿中生
            $this->examInsert["AREA"] = "1";
        }else if($this->insert["INOUT_KUBUN"] == "1"){  //県内
            $this->examInsert["AREA"] = "2";
        }else{          //県外
            $this->examInsert["AREA"] = "3";
        }
        
        common::_update("SAT_EXAM_DAT", "", "", $this->examInsert, $db, "2");
        
        $this->setMessage("追加しました。");
        
        $this->right_field["SHORI"] = "2";
        $this->SAT_NO = $this->insert["SAT_NO"];

        //DB切断
        Query::dbCheckIn($db);
        
        return true;


    }

    //DELETE
    function getDeleteModel() {
        //DB接続
        $db = Query::dbCheckOut();

        $delquery = knjl370qQuery::deleteApp($this->SAT_NO);
        $db->query($delquery);
        
        $delquery = knjl370qQuery::deleteExam($this->SAT_NO);
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
        $checkQuery = knjl370qQuery::betweenCnt($field["JUKEN_NO_FROM"], $recno);
        $checkCnt = $db->getOne($checkQuery);
        if($checkCnt > 0){
            return false;
        }
        
        //他のにTOが含まれていないか
        $checkQuery2 = knjl370qQuery::betweenCnt($field["JUKEN_NO_TO"], $recno);
        $checkCnt2 = $db->getOne($checkQuery2);
        if($checkCnt2 > 0){
            return false;
        }
        
        //FROM・TOに他のを含んでいないか
        $checkQuery3 = knjl370qQuery::inCheck($field["JUKEN_NO_FROM"], $field["JUKEN_NO_TO"], $recno);
        $checkCnt3 = $db->getOne($checkQuery3);
        if($checkCnt3 > 0){
            return false;
        }
        
        if($recno == ""){
            //同じ会場・個人団体・郵送窓口のデータが存在していないか
            $checkQuery4 = knjl370qQuery::sameChack($field["PLACECD"], $field["IND_KUBUN"], $field["SEND_KUBUN"]);
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
}
?>
