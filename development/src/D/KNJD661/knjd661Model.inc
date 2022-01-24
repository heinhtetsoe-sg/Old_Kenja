<?php

require_once('for_php7.php');

class knjd661Model extends Model {
    var $field    = array();
    var $cmd;
    var $control; //コントロールマスタ

    function knjd661Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("DATA_DIV"         =>  VARS::post("DATA_DIV"), //データ種別
                             "MOCKCD"           =>  VARS::post("MOCKCD"),   //テスト名称
                             "GRADE"            =>  VARS::post("GRADE"),    //学年
                             "CHK_TENTAI"       =>  VARS::post("CHK_TENTAI"),    //転退学者除く
                             "DBNAME"           =>  VARS::post("DBNAME")    //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
    }

    function clean() {}
}
?>
