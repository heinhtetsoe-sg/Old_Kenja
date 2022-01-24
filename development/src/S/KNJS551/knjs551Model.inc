<?php

require_once('for_php7.php');

class knjs551Model extends Model {
    var $field = array();
    var $cmd;
    var $staffcd;          //セキュリティー用スタッフコード
    var $sec_competence;   //権限保持
    var $weekArray = array();
    var $updDateArray = array();
    

    function init() {
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //共通関数(コントロールマスタより)
        common::GetControlMaster_Fnc($this->control);
        $this->cmd = VARS::request("cmd");
                                     
        $this->field = array("DATA_DIV"                         => VARS::post("DATA_DIV"),
                             "SCHOOL_KIND"                      => VARS::post("SCHOOL_KIND"),
                             "GRADE"                            => VARS::post("GRADE"),
                             "EXECUTEDATE"                      => VARS::post("EXECUTEDATE"),
                             "HOLIDAY_FLG"                      => VARS::post("HOLIDAY_FLG"),
                             "REMARK1"                          => VARS::post("REMARK1"),
                             "REMARK2"                          => VARS::post("REMARK2"),
                             "UPDATED"                          => VARS::post("UPDATED"),
                             
                             "YEAR"                             => VARS::post("YEAR"),
                             "MONTH"                            => VARS::post("MONTH"),
                             "OUTPUT"                           => VARS::post("OUTPUT"), //初期化対象
                             );
        
        
        $db = Query::dbCheckOut();
        
        $this->updfield = array();
        for ($Scount = 0; $Scount < get_count($this->optSubclass); $Scount++) {
            for ($Gcount = 0; $Gcount < get_count($this->optGrade); $Gcount++) {
                //標準時数
                $setName = "_".$Scount."_".$Gcount;
                $this->updfield[$Scount]["STANDARD_TIME_".$Gcount] = VARS::post("STANDARD_TIME".$setName);
            }
        }
        
        Query::dbCheckIn($db);        

    }
    
    /***************************************
    更新メイン処理
    ****************************************/
    function &getInsertModel() {
    
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        //更新処理
        $this->insert($db);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        $this->setMessage("MSG201");

        return;
    }
    
    /*****************
    更新用の各処理
    ******************/    
    //データの更新
    function insert($db) {
    
        //対象テーブルUpdate
        for ($Scount = 0; $Scount < get_count($this->optSubclass); $Scount++) {
            for ($Gcount = 0; $Gcount < get_count($this->optGrade); $Gcount++) {
                //更新対象データのテーブル内のデータ有無確認
                $query = knjs551Query::getInsertDataCountQuery($this, $Scount, $Gcount);
                $InsertToData = $db->getOne($query);
                if ($InsertToData == 0 && $this->updfield[$Scount]["STANDARD_TIME_".$Gcount] != "") {
                    $query = knjs551Query::getInsertQuery($this, $Scount, $Gcount);
                    $db->query($query);
                } else {
                    $query = knjs551Query::getUpdateQuery($this, $Scount, $Gcount);
                    $db->query($query);
                }
            }
        }
    }
    
    
    /***************************************
    初期化メイン処理
    ****************************************/
    
    //行事予定登録処理
    function &getShokitiModel() {

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        //対象データ削除処理
        $this->dateDelete($db);
        //休暇日登録
        $this->dateCopy($db);
        
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        $this->setMessage("MSG201");

        return;
    }
    
    /*****************
    初期化用の各処理
    ******************/
    
    //行事予定登録処理用休業
    function dateDelete($db) {
        //EVENT_MSTを削除
        $query = knjs551Query::getUnitClassDelQuery($this);

        $db->query($query);
    }
        
    //教務初期化処理
    function dateCopy($db) {
        //UNIT_CLASS_LESSON_MSTを取得し、UNIT_CLASS_LESSONへInsertする
        $query = knjs551Query::getUnitClassMstQuery($this);
        $result = $db->query($query);
        while ($setUnitMstData = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $query = knjs551Query::setUnitMstCopyQuery($setUnitMstData, $this);
            $db->query($query);
        }
        $result->free();
        return;
    }
}
?>
