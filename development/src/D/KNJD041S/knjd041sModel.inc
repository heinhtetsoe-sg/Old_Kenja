<?php

require_once('for_php7.php');

class knjd041sModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;
    var $schoolName;
    var $schoolCode;

    function knjd041sModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),       //年度
                             "SEMESTER"     =>  VARS::post("SEMESTER"),   //学期コード
                             "TESTCD"       =>  VARS::post("TESTCD"),     //テスト
                             "RECORD_DIV"   =>  VARS::post("RECORD_DIV"), //素点・評価のラジオボタン
                             "OUTPUT"       =>  VARS::post("OUTPUT"),     //帳票出力指定
                             "DBNAME"       =>  VARS::post("DBNAME")      //DB
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル

        //学校名取得
        $query = knjd041sQuery::getSchoolName();
        $db = Query::dbCheckOut();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName = $rowZ010["NAME1"];
        $this->schoolCode = $rowZ010["NAME2"];
        Query::dbCheckIn($db);
    }
}
?>
