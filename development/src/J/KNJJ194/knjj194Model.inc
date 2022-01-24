<?php

require_once('for_php7.php');

class knjj194Model extends Model {
    var $field      = array();
    var $selectdata = array();
    var $cmd;

    function knjj194Model() {}

    function init() {
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("DBNAME"   =>  VARS::post("DBNAME"));      //DB変更に備えて
                            
        $this->cmd = VARS::request("cmd");
        $this->selectdata = explode(",", VARS::post("selectdata"));
    }

    function clean() {}
}
?>