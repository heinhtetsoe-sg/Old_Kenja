<?php

require_once('for_php7.php');

class knjb0032Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ

    function knjb0032Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->field = array("DBNAME"   =>  VARS::post("DBNAME")
                            );

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
?>
