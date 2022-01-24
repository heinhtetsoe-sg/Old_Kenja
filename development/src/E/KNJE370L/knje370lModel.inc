<?php

require_once('for_php7.php');

class knje370lModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ

    function knje370lModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"       =>  VARS::post("YEAR"),       //年度
                             "SEMESTER"   =>  VARS::post("SEMESTER"),   //学期コード
                             "OUTPUT"     =>  VARS::post("OUTPUT"),     //進学/就職
                             "FILTER"     =>  VARS::post("FILTER"),     //抽出条件
                             "DBNAME"     =>  VARS::post("DBNAME")      //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

    }

    function clean() {}
}
?>
