<?php

require_once('for_php7.php');

class knjd629Model extends Model {
    var $field = array();
    var $cmd;

    function knjd629Model() {}

    function init() {
        $this->field = array("GRADE"        =>  VARS::post("GRADE"),
                             "DBNAME"       =>  VARS::post("DBNAME")
                             );

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }
}
?>
