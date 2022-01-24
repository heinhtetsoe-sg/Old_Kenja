<?php

require_once('for_php7.php');

class knjd626dModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ

    function knjd626dModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コード
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "SUB_TESTCD"           =>  VARS::post("SUB_TESTCD"),           //テスト
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
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
