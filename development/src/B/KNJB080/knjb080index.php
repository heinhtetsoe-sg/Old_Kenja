<?php

require_once('for_php7.php');

require_once('knjb080Model.inc');
require_once('knjb080Query.inc');

class knjb080Controller extends Controller {
    var $ModelClassName = "knjb080Model";
    var $ProgramID      = "KNJB080";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {

                case "":
                case "knjb080":
                    $this->callView("knjb080Form1");
                    break 2;

                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjb080Ctl = new knjb080Controller;
//var_dump($_REQUEST);
?>
