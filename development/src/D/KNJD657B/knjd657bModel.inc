<?php

require_once('for_php7.php');

class knjd657bModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ

    function knjd657bModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->group_div   = "2";
        $this->target_div  = "0";
        $this->stf_auth_cd = "00008888";

        $this->field = array("YEAR"               =>  VARS::post("YEAR"),              //年度
                             "SEMESTER"           =>  VARS::post("SEMESTER"),          //学期コンボ
                             "GRADE"              =>  VARS::post("GRADE"),             //学年
                             "HR_CLASS"           =>  VARS::post("HR_CLASS"),          //クラス
                             "GROUPCD"            =>  VARS::post("GROUPCD"),           //グループコード
                             "CATEGORY_IS_CLASS"  =>  VARS::post("CATEGORY_IS_CLASS"), //表示区分
                             "OKURIJOU_JUSYO"     =>  VARS::post("OKURIJOU_JUSYO"),    //送り状住所
                             "DATE"               =>  VARS::post("DATE"),              //提出日
                             "DBNAME"             =>  VARS::post("DBNAME")             //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
    }

    function clean() {}
}
?>
