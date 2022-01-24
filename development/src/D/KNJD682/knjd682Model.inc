<?php

require_once('for_php7.php');

class knjd682Model extends Model {
    var $field = array();
    var $cmd;

    function knjd682Model() {}

    function init() {
        $this->field = array("SEMESTER"     =>  VARS::post("SEMESTER"),
                             "GRADE"        =>  VARS::post("GRADE"),
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
    }

    function clean() {}
}
?>
