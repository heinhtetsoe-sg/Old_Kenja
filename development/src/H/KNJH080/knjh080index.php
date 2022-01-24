<?php

require_once('for_php7.php');

require_once('knjh080Model.inc');
require_once('knjh080Query.inc');

class knjh080Controller extends Controller {
    var $ModelClassName = "knjh080Model";
    var $ProgramID      = "KNJH080";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh080":								//メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
					$sessionInstance->knjh080Model();		//コントロールマスタの呼び出し
                    $this->callView("knjh080Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjh080Ctl = new knjh080Controller;
var_dump($_REQUEST);
?>
