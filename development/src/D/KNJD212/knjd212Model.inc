<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd212Model.inc 56580 2017-10-22 12:35:29Z maeshiro $

class knjd212Model extends Model {
    var $field      = array();
    var $cmd;
    var $semes_sdate;
    var $semes_edate;
    var $ITEM = array();
    var $SEM = array();
    var $selectdata = array();

    function knjd212Model()
    {
        $this->field = array();
    }

    function init()
    {
        //学年
        $this->field["GRADE"] = VARS::post("GRADE");
        //学期
        $this->field["SEMESTER"] = VARS::post("SEMESTER");
        $this->cmd     = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        //科目
        $this->field["SUBCLASSCD"] = VARS::post("SUBCLASSCD");
        //類型グループ
        $this->selectdata  = explode(",", VARS::post("selectdata"));
        
        //基準日
        $this->ctrl_date = VARS::post("ctrl_date");

        //空の場合
        if (!strlen($this->ctrl_date)) {
            $this->ctrl_date = CTRL_DATE;

        //年度範囲外の場合
        } elseif (strtotime($this->ctrl_date) < strtotime(CTRL_YEAR."/04/01") 
               || strtotime($this->ctrl_date) > strtotime(((int)CTRL_YEAR+1)."/03/31")) {
            $this->ctrl_date = CTRL_DATE;
        }
        $this->ctrl_date = str_replace("/", "-", $this->ctrl_date);

        //項目名
        $this->ITEM=array( "0101"=>"SEM1_INTER_REC",
                           "0102"=>"SEM1_TERM_REC",
                           "0103"=>"SEM1_REC",
                           "0201"=>"SEM2_INTER_REC",
                           "0202"=>"SEM2_TERM_REC",
                           "0203"=>"SEM2_REC",
                           "0302"=>"SEM3_TERM_REC",
                           "8003"=>"GRADE_RECORD"
                         );
        //学期
        $this->SEM=array( "0101"=>"1",
                          "0102"=>"1",
                          "0103"=>"1",
                          "0201"=>"2",
                          "0202"=>"2",
                          "0203"=>"2",
                          "0302"=>"3",
                          "8003"=>CTRL_SEMESTER
                        );
    }

    /*  更新処理 */
    function &getUpdateModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //類型グループHRクラス毎の合計値のクリア
        $query=knjd212Query::getTGHDClearQuery($this,$this->ITEM);
        $db->query($query);

        //類型グループHRクラス毎の合計値の算出
        $query=knjd212Query::getTGHDCalQuery($db, $this, $this->ITEM, $this->SEM);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row["SUM"] = isset($row["SUM"])? $row["SUM"] : 'Null';
            $row["CNT"] = isset($row["CNT"])? $row["CNT"] : 'Null';
            //類型グループHRクラス毎の合計値の更新
            $query=knjd212Query::getTGHDUpQuery($this,$this->ITEM,$row);
            $db->query($query);
        }

        //類型グループ毎の合計値のクリア
        $query=knjd212Query::getTGMClearQuery($this,$this->ITEM);
        $db->query($query);

        //類型グループ毎の合計値の算出
        $query=knjd212Query::getTGMCalQuery($this,$this->ITEM);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row["SUM"] = isset($row["SUM"])? $row["SUM"] : 'Null';
            $row["CNT"] = isset($row["CNT"])? $row["CNT"] : 'Null';
            //類型グループ毎の合計値の更新
            $query=knjd212Query::getTGMUpQuery($this,$this->ITEM,$row);
            $db->query($query);
        }

        ///類型評定判定のクリア
        $query=knjd212Query::getTASSCDClearQuery($this,$this->ITEM);
        $db->query($query);

        //類型評定の判定
        $query=knjd212Query::getTASSCDCalQuery($this,$this->ITEM);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //類型グループ毎の類型評定の更新
            $query=knjd212Query::getTASSCDUpQuery($this,$this->ITEM,$row);
            $db->query($query);
        }

        //固定評定処理
        $query=knjd212Query::getFixValueSetQuery($this,$this->ITEM);
        $db->query($query);

        //生徒毎の類型評定更新用データ取得
        $query=knjd212Query::getSTDCalQuery($this,$this->ITEM);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //生徒毎の類型評定の更新
            $query=knjd212Query::getSTDUpQuery($this,$this->ITEM,$this->SEM,$row);
            $db->query($query);
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
    }

}
//var_dump($_REQUEST);
?>
