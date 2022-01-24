<?php

require_once('for_php7.php');

class knjd674fModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ

    function knjd674fModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コード
                             "TESTCD"               =>  VARS::post("TESTCD"),               //テスト
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");
        
        //プロパティファイル
        $this->getPropertiesAll();
    }

    function clean() {}
}
?>
