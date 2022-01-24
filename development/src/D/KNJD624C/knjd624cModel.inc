<?php

require_once('for_php7.php');

class knjd624cModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ

    function knjd624cModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");

        $this->field = array("YEAR"         =>  VARS::post("YEAR"),        //年度
                             "SEMESTER"     =>  VARS::post("SEMESTER"),    //学期
                             "GRADE"        =>  VARS::post("GRADE"),       //学年
                             "DATA_DIV"     =>  VARS::post("DATA_DIV"),    //データ種別
                             "MOCKCD"       =>  VARS::post("MOCKCD"),      //テスト名称
                             "DBNAME"       =>  VARS::post("DBNAME")       //DB変更に備えて
                            );
        $this->getPropertiesAll();
    }

    function clean()
    {
    }
}
?>
