<?php

require_once('for_php7.php');

class knjd656bModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ

    function knjd656bModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->group_div   = "2";
        $this->target_div  = "0";
        $this->stf_auth_cd = "00008888";

        $this->field = array("YEAR"     =>  VARS::post("YEAR"),      //年度
                             "SEMESTER" =>  VARS::post("SEMESTER"),  //学期コンボ
                             "GRADE"    =>  VARS::post("GRADE"),     //学年
                             "GROUPCD"  =>  VARS::post("GROUPCD"),   //模試グループ
                             "MOCKCD"   =>  VARS::post("MOCKCD"),    //模試コード
                             "DBNAME"   =>  VARS::post("DBNAME")     //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
    }

    function clean() {}
}
?>
