<?php

require_once('for_php7.php');

class knjd656Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ

    function knjd656Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->group_div   = "2";
        $this->target_div  = "0";
        $this->stf_auth_cd = "00008888";

        $this->field = array("YEAR"            =>  VARS::post("YEAR"),           //年度
                             "SEMESTER"        =>  VARS::post("SEMESTER"),       //学期コンボ
                             "GROUPCD"         =>  VARS::post("GROUPCD"),        //模試グループ
                             "MOCKCD"          =>  VARS::post("MOCKCD"),         //模試コード
                             "DATE"            =>  VARS::post("DATE"),           //実施日
                             "DBNAME"          =>  VARS::post("DBNAME")          //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
    }

    function clean() {}
}
?>
