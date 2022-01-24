<?php

require_once('for_php7.php');

class knjd658bModel extends Model {
    var $field          = array();
    var $opt_classcd    = array();
    var $opt_subclasscd = array();
    var $selectdata;
    var $selectdata2;
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード

    function knjd658bModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init() {
        $this->field = array("YEAR"          =>  VARS::post("YEAR"),           //年度
                            "SEMESTER"       =>  VARS::post("SEMESTER"),       //学期コード
                            "GRADE_HR_CLASS" =>  VARS::post("GRADE_HR_CLASS"), //学年
                            "TESTKINDCD"     =>  VARS::post("TESTKINDCD"),     //テスト種別
                            "JUNI"           =>  VARS::post("JUNI"),           //出力順位
                            "KIJUNTEN"       =>  VARS::post("KIJUNTEN"),       //順位の基準点
                            "SAIDAIKAMOKU"   =>  VARS::post("SAIDAIKAMOKU"),   //最大科目数
                             "KANSAN"        =>  VARS::post("KANSAN"),         //100点に換算する
                            "DBNAME"         =>  VARS::post("DBNAME")          //DB変更に備えて
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();

        //学校名取得
        $query = knjd658bQuery::getSchoolName();
        $db = Query::dbCheckOut();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName = $rowZ010["NAME1"];
        $this->schoolCode = $rowZ010["NAME2"];
        Query::dbCheckIn($db);
    }

    function clean() {}
}
?>
