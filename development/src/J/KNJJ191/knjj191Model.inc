<?php

require_once('for_php7.php');

class knjj191Model extends Model {
    var $field      = array();
    var $cmd;

    function knjj191Model() {
    }

    function init() {
        $this->getPropertiesAll(); //プロパティファイル
        $this->year = CTRL_YEAR;
        $this->field = array("SCHOOL_KIND"  =>  VARS::post("SCHOOL_KIND"),
                             "DBNAME"       =>  VARS::post("DBNAME")
                     );
        $this->cmd = VARS::request("cmd");
        $this->selectdata = explode(",", VARS::post("selectdata"));
    }

    function clean() {}
}
?>