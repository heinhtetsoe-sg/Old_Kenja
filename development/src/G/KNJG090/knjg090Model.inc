<?php

require_once('for_php7.php');

class knjg090Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ

    function knjg090Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コード
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "HR_CLASS"             =>  VARS::post("HR_CLASS"),             //組
                             "CATEGORY_IS_CLASS"    =>  VARS::post("CATEGORY_IS_CLASS"),    //表示区分
                             "MONEY"                =>  VARS::post("MONEY"),                //金額
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");

    }

    function clean()
    {
    }
}
?>
