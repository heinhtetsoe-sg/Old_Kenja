<?php

require_once('for_php7.php');

require_once('knjc080Model.inc');
require_once('knjc080Query.inc');

class knjc080Controller extends Controller {
    var $ModelClassName = "knjc080Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc080":
                    $sessionInstance->knjc080Model();
                    $this->callView("knjc080Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjc080Ctl = new knjc080Controller;
//var_dump($_REQUEST);
?>
