<?php

require_once('for_php7.php');

class knje377Model extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;

    function knje377Model() {}

    function init() {

        $this->field = array("POROW"            =>  VARS::post("POROW"),            //行
                             "POCOL"            =>  VARS::post("POCOL")             //列
                     );

        $this->cmd = VARS::request("cmd");
    }
}
?>
