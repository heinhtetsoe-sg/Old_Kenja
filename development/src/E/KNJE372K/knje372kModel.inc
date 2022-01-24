<?php

require_once('for_php7.php');


class knje372kModel extends Model {
    var $field = array();
    var $cmd;
    var $control;

    function knje372kModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "CATEGORY_IS_CLASS"    =>  VARS::post("CATEGORY_IS_CLASS"),    //表示区分
                             "GRADE_HR_CLASS"       =>  VARS::post("GRADE_HR_CLASS"),       //クラス
                             "PRINT_DATE"           =>  VARS::post("PRINT_DATE"),           //出力日
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function clean() {}
}
?>
