<?php

require_once('for_php7.php');

class knjd660Model extends Model {
    var $field      = array();
    var $cmd;
    var $subclassGroup;
    var $control;        //コントロールマスタ

    function knjd660Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("DATA_DIV"         =>  VARS::post("DATA_DIV"),   //データ種別
                             "SELECT_DIV"       =>  VARS::post("SELECT_DIV"), //選択区分
                             "GRADE"            =>  VARS::post("GRADE"),      //学年
                             "DBNAME"           =>  VARS::post("DBNAME")      //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
    }

    function clean() {}
}
?>
