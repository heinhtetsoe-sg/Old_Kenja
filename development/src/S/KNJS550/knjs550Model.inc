<?php

require_once('for_php7.php');

class knjs550Model extends Model {
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
                                     
        $this->field = array("YEAR"         => VARS::post("YEAR"),
                             "SCHOOL_KIND"  => VARS::post("SCHOOL_KIND")
                             );

        $this->updfield = array();
        for ($Scount = 0; $Scount < get_count($this->optSubclass); $Scount++) {
            for ($Gcount = 0; $Gcount < get_count($this->optGrade); $Gcount++) {
                //標準時数
                $setName = "_".$Scount."_".$Gcount;
                $this->updfield[$Scount]["STANDARD_TIME_".$Gcount] = VARS::post("STANDARD_TIME".$setName);
            }
        }
    }

    /***************************************
    更新メイン処理
    ****************************************/
    function &getInsertModel() {
    
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //更新処理
        $this->insert($db);

        $db->commit();
        Query::dbCheckIn($db);

        $this->setMessage("MSG201");
        return;
    }

    function insert($db) {

        //データ削除
        for ($Gcount = 0; $Gcount < get_count($this->optGrade); $Gcount++) {
            $delQuery = knjs550Query::getUnitClassDelQuery($this, $Gcount);
            $db->query($delQuery);
        }

        //対象テーブルUpdate
        for ($Scount = 0; $Scount < get_count($this->optSubclass); $Scount++) {
            for ($Gcount = 0; $Gcount < get_count($this->optGrade); $Gcount++) {

                if ($this->updfield[$Scount]["STANDARD_TIME_".$Gcount] != "") {
                    $query = knjs550Query::getInsertQuery($this, $Scount, $Gcount);
                    $db->query($query);
                }

            }
        }
    }
    
    //前年度からのコピーのモデルを返します
    function &getCopyModel() {
        $db = Query::dbCheckOut();
        $this_year_check = $db->getOne(knjs550Query::getLessonMstCount($this, $this->field["YEAR"]));
        $last_year_check = $db->getOne(knjs550Query::getLessonMstCount($this, $this->field["YEAR"] - 1));
        
        if($this_year_check != 0) {
            $this->setWarning("MSG203","既にデータが存在します。");
            return false;
        } else if($last_year_check == 0) {
            $this->setWarning("MSG303");
            return false;
        } else {
            $query = knjs550Query::getLessonMstDate($this, $this->field["YEAR"] - 1);
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //SCHREG_REGD_GDATに登録されているデータの存在チェック
                $query = knjs550Query::getGradeCount($this, $row);
                $getGrade = $db->getOne($query);
                //UNIT_STUDY_CLASS_DATに登録されているデータのみを登録する
                $query = knjs550Query::getDataCheckCount($this, $row);
                $getDataCheck = $db->getOne($query);
                if ($getGrade == 1 && $getDataCheck == 1) {
                    //前年度のデータを今年度のデータとしてINSERT
                    $query = knjs550Query::getCopyQuery($this, $row);
                    $db->query($query);
                } 
            }
        }
        $db->commit();

        $this->setMessage("MSG201");
        
        Query::dbCheckIn($db);
    }
}
?>
