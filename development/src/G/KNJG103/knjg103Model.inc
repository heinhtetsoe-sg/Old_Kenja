<?php

require_once('for_php7.php');

class knjg103Model extends Model {
    var $field = array();
    var $cmd;
    var $control;

    function knjg103Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),
                             "CHOICE"           =>  VARS::post("CHOICE"),
                             "HR_CLASS_TYPE"    =>  VARS::post("HR_CLASS_TYPE"),
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),
                             "NO_PRINT_FUKU"    =>  VARS::post("NO_PRINT_FUKU"),
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
