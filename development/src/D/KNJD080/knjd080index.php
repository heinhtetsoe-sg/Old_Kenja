<?php

require_once('for_php7.php');

require_once('knjd080Model.inc');
require_once('knjd080Query.inc');

class knjd080Controller extends Controller {
    var $ModelClassName = "knjd080Model";
    var $ProgramID      = "KNJD080";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "toukei":
                case "":
                    $this->callView("knjd080Form1");
                   break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjd080Ctl = new knjd080Controller;
//var_dump($_REQUEST);
?>
