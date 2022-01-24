<?php

require_once('for_php7.php');

class knja171eModel extends Model {
    var $field = array();
    var $cmd;
    var $control;

    function knja171eModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),
                             "HR_CLASS_TYPE"    =>  VARS::post("HR_CLASS_TYPE"),
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
?>
