<?php

require_once('for_php7.php');


class knjd153tModel extends Model {
    var $field      = array();
    var $selectdata;
    var $cmd;
    var $control;
    var $staffcd;
    var $testTable;

    function knjd153tModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "GAKKI"            =>  VARS::post("GAKKI"),            //学期
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DATE"             =>  VARS::post("DATE"),             //異動対象日付
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テスト名
                             "DEVIATION"        =>  VARS::post("DEVIATION"),        //偏差値出力
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->selectdata  = VARS::post("selectdata");
        $this->cmd         = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        //初期値の設定
        $this->testTable = (strlen($this->Properties["useTestCountflg"])) ? $this->Properties["useTestCountflg"] : "TESTITEM_MST_COUNTFLG_NEW";
    }
}
?>
