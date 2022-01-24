<?php

require_once('for_php7.php');

class knji101cModel extends Model {
    var $cmd;

    function knji101cModel() {

    }

    function init() {

        $this->cmd = VARS::request("cmd");

    }
}
?>
