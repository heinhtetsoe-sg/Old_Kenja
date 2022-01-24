<?php

require_once('for_php7.php');

class knja171fModel extends Model {
    var $semester;
    var $hrClass;
    var $field = array();
    var $cmd;
    var $control;

    function knja171fModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->field = array("HR_CLASS_TYPE"    =>  VARS::post("HR_CLASS_TYPE"),
                             "PRINT_RADIO"      =>  VARS::post("PRINT_RADIO"),
                             "JOSEKI"           =>  VARS::post("JOSEKI"),
                             "NOT_PRINT_HOGOSHA_NAME"      =>  VARS::post("NOT_PRINT_HOGOSHA_NAME"),
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
