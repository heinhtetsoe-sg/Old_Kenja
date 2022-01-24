<?php

require_once('for_php7.php');

class knjd181lModel extends Model {
    var $field = array();
    var $cmd;
    var $control;
    var $selectdata;

    function knjd181lModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("DATE"                 =>  VARS::post("DATE"),                 //異動対象日付
                             "DESC_DATE"            =>  VARS::post("DESC_DATE"),            //記載日付
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コード
                             "GRADE_HR_CLASS"       =>  VARS::post("GRADE_HR_CLASS"),       //年組
                             "PRINT_SIDE1"          =>  VARS::post("PRINT_SIDE1"),          //1.表紙
                             "PRINT_SIDE2"          =>  VARS::post("PRINT_SIDE2"),          //2.成績
                             "PRINT_SIDE3"          =>  VARS::post("PRINT_SIDE3"),          //3.所見
                             "PRINT_SIDE4"          =>  VARS::post("PRINT_SIDE4"),          //4.修了証
                             "CHECKS"               =>  VARS::post("CHECKS"),               //出欠の記録を出力しない カンマ区切り
                             "HR_CLASS_TYPE"        =>  VARS::post("HR_CLASS_TYPE"),        //実クラス選択
                             "TOTAL_STADY_FIELD"    =>  VARS::post("TOTAL_STADY_FIELD"),    //自立活動
                             "STAMP_FIELD"          =>  VARS::post("STAMP_FIELD"),          //校長・担任印
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                     );

        $this->cmd          = VARS::request("cmd");
        $this->selectdata   = VARS::post("selectdata");

        $this->getPropertiesAll();
    }
}
?>
