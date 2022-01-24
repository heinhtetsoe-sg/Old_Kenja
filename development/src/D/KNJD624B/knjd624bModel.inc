<?php

require_once('for_php7.php');

class knjd624bModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ

    function knjd624bModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル

        $this->field = array("YEAR"         =>  VARS::post("YEAR"),        //年度
                             "SEMESTER"     =>  VARS::post("SEMESTER"),    //学期
                             "GRADE"        =>  VARS::post("GRADE"),       //学年
                             "TESTCD"       =>  VARS::post("TESTCD"),      //テスト
                             "DBNAME"       =>  VARS::post("DBNAME")       //DB変更に備えて
                            );
    }

    function clean()
    {
    }
}
?>
