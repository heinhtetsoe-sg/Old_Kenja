<?php

require_once('for_php7.php');

class knjl401qModel extends Model {
    var $field      = array();
    var $cmd;
    var $mockcd = "";        //primary
    var $grade = "";       //primary
    var $mockyear;
    var $copyyear;

    function knjl401qModel()
    {
    }

    function init()
    {
        $this->cmd          = VARS::request("cmd");
        if($this->cmd == ""){
            $this->field = array();
        }
        $this->field = array( "EXAMNO"                =>  VARS::post("EXAMNO"),
                              "NAME_SEI"              =>  VARS::post("NAME_SEI"),
                              "NAME_MEI"              =>  VARS::post("NAME_MEI"),
                              "PLACECD"               =>  VARS::post("PLACECD"),
                              "FEE"                   =>  VARS::post("FEE"),
                              "APPLYDIV"              =>  VARS::post("APPLYDIV"),
                              "BAN_EXAMNO"            =>  VARS::post("BAN_EXAMNO")
                              );
        if ($this->cmd == 'name_search_search') {
            $this->field["SEARCH_NAME"] = VARS::post("SEARCH_NAME");
        }
    }

    //INSERT
    function getInsertModel()
    {
        //NULLをチェック
        if($this->field["NAME_SEI"] == "" || $this->field["NAME_MEI"] == ""){
            $this->setMessage("氏名は必ず入力してください。");
            $this->field["SHORI"] = "1";
            return false;
        }
        
        //DB接続
        $db = Query::dbCheckOut();

        //入れる前にエラーチェック
        $cntQuery = knjl401qQuery::getCnt($this->field["BAN_EXAMNO"]);
        $cnt = $db->getOne($cntQuery);
        if($cnt > 0){
            $this->setMessage("受験番号が重複しています。");
            $this->field["SHORI"] = "1";
            return false;
        }else{
            //受験番号帯に登録されているか
            $checkQuery = knjl401qQuery::getNoGroup2($this, "COUNT");
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
        $this->insert["SAT_NO"] = $this->field["BAN_EXAMNO"];
        
        $this->insert["NAME1"] = $this->field["NAME_SEI"]."　".$this->field["NAME_MEI"];
        $this->insert["NAME2"] = $this->field["NAME_SEI"].$this->field["NAME_MEI"];
        $this->insert["LASTNAME"] = $this->field["NAME_SEI"];
        $this->insert["FIRSTNAME"] = $this->field["NAME_MEI"];
        
        $this->insert["PLACECD"] = $this->field["PLACECD"] != "" ? $this->field["PLACECD"] : " ";
        
        common::_update("SAT_APP_FORM_MST", "", "", $this->insert, $db, "2");
        
        $this->setMessage("データを登録しました。");
        
        $this->field["SHORI"] = "2";
        $this->SAT_NO = $this->insert["SAT_NO"];

        //DB切断
        Query::dbCheckIn($db);

        $this->field["EXAMNO"] = $this->field["BAN_EXAMNO"];
        $this->field["BAN_EXAMNO"] = "";
        
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
