<?php

require_once('for_php7.php');

class knja172Model extends Model {
    var $semester;
    var $hrClass;
    var $field = array();
    var $cmd;
    var $control;

    function knja172Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->field = array("HR_CLASS_TYPE"    =>  VARS::post("HR_CLASS_TYPE"),
                             "PRINT_RADIO"      =>  VARS::post("PRINT_RADIO"),
                             "GRADE"            =>  VARS::post("GRADE"),
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
