<?php

require_once('for_php7.php');

require_once('knjj080Model.inc');
require_once('knjj080Query.inc');

class knjj080Controller extends Controller {
    var $ModelClassName = "knjj080Model";
    var $ProgramID      = "KNJJ080";
	
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "sel";
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjj080Form1");
                    break 2;
// NO001 del //////////////////////////////////////////
//                 case "knjj080Form1";
//                    $this->callView("knjj080Form1");
//                    break 2;
///////////////////////////////////////////////////////
               default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjj080Ctl = new knjj080Controller;
//var_dump($_REQUEST);
?>
