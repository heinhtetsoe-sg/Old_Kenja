<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjh400_everyModel extends Model {
    var $field      = array();
    var $cmd;
    var $subclasscd;        //primary
    var $year_code;
    var $isload;
    var $tmp;				//NO001
    var $year;				//NO001
    
    /**
     * メンバ変数の初期化
     */
    function knjh400_everyModel()
    {
    }
    function init()
    {              
        $this->cmd      = VARS::request("cmd");
        $this->schregno = VARS::request("SCHREGNO") != "" ? VARS::request("SCHREGNO") : $this->schregno;
        
        //左側検索用項目
        if($this->cmd == "list" || $this->cmd == "search"){
            $this->search = array(
                                  "FROM_DATE"   =>  str_replace("/", "-", VARS::post("FROM_DATE")),
                                  
                                  "TO_DATE"     =>  str_replace("/", "-", VARS::post("TO_DATE")),
                                  
                                  "STAFF"       =>  VARS::post("STAFF"),
                                  
                                  "TAGSEARCH"   =>  VARS::post("TAGSEARCH"),
                                  
                                  "SEARCH"      =>  VARS::post("SEARCH") != "" ? VARS::post("SEARCH") : "1",
                                  
                                  "SEARCHRADIO" =>  VARS::post("SEARCHRADIO") != "" ? VARS::post("SEARCHRADIO") : "1",
                                  
                                  
                                 );
        }
        if($this->cmd == "clear"){
            $this->search = array();
            $this->search["SEARCH"] = "1";
            $this->search["SEARCHRADIO"] = "1";
        }
        
        $this->recNo = VARS::request("RECNO") != "" ? VARS::request("RECNO") : $this->recNo;
        if($this->cmd == "edit" || $this->cmd == "new" || $this->cmd == "copy"){
            //最初開いたときと新規作成、コピーを選択したときに消す。
            $this->recNo = "";
        }
        
        //右側入力用フィールド
        $this->field = array(
                             "REGISTERCD"   =>  VARS::post("REGISTERCD") != "" ? VARS::post("REGISTERCD") : STAFFCD,
                             
                             "DATE"         =>  str_replace("/", "-", VARS::post("DATE")),

                             "H_TIME"       =>  VARS::post("H_TIME"),

                             "M_TIME"       =>  VARS::post("M_TIME"),
                             
                             "TITLE"        =>  VARS::post("TITLE"),
                             
                             "TEXT"         =>  VARS::post("TEXT"),
                             
                             "TAG"          =>  VARS::post("TAG"),
                             
                            );
        
    }
    
    /*挿入*/
    function getInsertModel()
    {
        unset($this->insert);

        $db = Query::dbCheckOut();
        
        //EVERYDAY_FINDINGS_DATへの挿入
        //RECNO取得
        $query = knjh400_everyQuery::getMaxRecNo($this->schregno);
        $maxRecNo = $db->getOne($query);
        
        $this->insert["RECNO"] = $maxRecNo + 1;
        $this->insert["SCHREGNO"] = $this->schregno;
        
        $this->insert["FIND_DATE"] = $this->field["DATE"];
        $this->insert["FIND_TIME"] = sprintf("%02d", $this->field["H_TIME"]).":".sprintf("%02d", $this->field["M_TIME"]);
        
        $this->insert["TITLE"] = $this->field["TITLE"];
        $this->insert["TEXT"] = $this->field["TEXT"];
        
        $this->insert["REGISTERCD"] = STAFFCD;
        $this->insert["UPDATED"] = "sysdate()";
        
        common::_update("EVERYDAY_FINDINGS_DAT", "", "", $this->insert, $db, "2");
        
        //EVERYDAY_FINDINGS_TAG_DATの挿入
        if($this->field["TAG"] != ""){
            $replace = array("#", "＃");
            $tagdata = str_replace($replace, "", $this->field["TAG"]);  //#削除
            $tagdata = str_replace(" ", "　", $tagdata);    //半角スペースを全角に
            
            $tag = explode("　", $tagdata);     //全角スペースでexplode
            
            foreach($tag as $key => $val){
                if($val != ""){
                    $this->insert["SEQNO"] = $key+1;
                    $this->insert["TAG"] = $val;
                    
                    common::_update("EVERYDAY_FINDINGS_TAG_DAT", "", "", $this->insert, $db, "2");
                }
            }
        }
        
        
        $this->setMessage("追加しました");
        
        $this->recNo = $this->insert["RECNO"];
        
        Query::dbCheckIn($db);
        
        return true;
    }
    
    /*更新*/
    function getUpdateModel()
    {
        unset($this->update);
        $db = Query::dbCheckOut();
        
        //EVERYDAY_FINDINGS_DAT更新
        $this->update["FIND_DATE"] = $this->field["DATE"];
        $this->update["FIND_TIME"] = sprintf("%02d", $this->field["H_TIME"]).":".sprintf("%02d", $this->field["M_TIME"]);
        
        $this->update["TITLE"] = $this->field["TITLE"];
        $this->update["TEXT"] = $this->field["TEXT"];
        
        $this->update["UPDATED"] = "sysdate()";
        
        $where = " WHERE SCHREGNO = '".$this->schregno."' AND RECNO = '".$this->recNo."' ";
        
        common::_update("EVERYDAY_FINDINGS_DAT", "", $where, $this->update, $db, "1");
        
        //EVERYDAY_FINDINGS_TAG_DATを一度すべて削除する
        $query = knjh400_everyQuery::delQuery($this->schregno, $this->recNo, "EVERYDAY_FINDINGS_TAG_DAT");
        $db->query($query);
        
        if($this->field["TAG"] != ""){
            $replace = array("#", "＃");
            $tagdata = str_replace($replace, "", $this->field["TAG"]);  //#削除
            $tagdata = str_replace(" ", "　", $tagdata);    //半角スペースを全角に
            
            $tag = explode("　", $tagdata);     //全角スペースでexplode
            
            $this->update["SCHREGNO"] = $this->schregno;
            $this->update["RECNO"] = $this->recNo;
            $this->update["REGISTERCD"] = STAFFCD;
            $this->update["UPDATED"] = "sysdate()";
            
            foreach($tag as $key => $val){
                if($val != ""){
                    $this->update["SEQNO"] = $key+1;
                    $this->update["TAG"] = $val;
                    
                    common::_update("EVERYDAY_FINDINGS_TAG_DAT", "", "", $this->update, $db, "2");
                }
            }
        }
        
        
        $this->setMessage("更新しました");
        
        Query::dbCheckIn($db);
        
        return true;
    }
    
    /*削除*/
    function getDeleteModel()
    {
        $db = Query::dbCheckOut();
        
        $query = knjh400_everyQuery::delQuery($this->schregno, $this->recNo, "EVERYDAY_FINDINGS_DAT");
        $db->query($query);
        
        $query = knjh400_everyQuery::delQuery($this->schregno, $this->recNo, "EVERYDAY_FINDINGS_TAG_DAT");
        $db->query($query);
        
        $this->setMessage("削除しました");
        
        $this->recNo = "";
        
        Query::dbCheckIn($db);
    }
}
?>
