<?php

require_once('for_php7.php');

class knjd623aModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $testTable;

    function knjd623aModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {               
        $this->field = array("SEMESTER"     =>  VARS::post("SEMESTER"),     //学期
                             "GRADE"        =>  VARS::post("GRADE"),        //学年
                             "TESTKINDCD"   =>  VARS::post("TESTKINDCD"),   //テスト種別
                             "GRAPH"        =>  VARS::post("GRAPH"),        //棒グラフ基準
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                            );

        $this->cmd   = VARS::request("cmd");
        $this->getPropertiesAll();

        //初期値の設定
        if (strlen($this->Properties["useTestCountflg"])) {
            $this->testTable = $this->Properties["useTestCountflg"];
        } else {
            $this->testTable = "TESTITEM_MST_COUNTFLG_NEW";
        }
    }

    function clean()
    {
    }
}
?>
