<?php

require_once('for_php7.php');

require_once('knji080Model.inc');
require_once('knji080Query.inc');

class knji080Controller extends Controller {
    var $ModelClassName = "knji080Model";
    var $ProgramID      = "KNJI080";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knji080":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knji080Model();		//コントロールマスタの呼び出し
                    $this->callView("knji080Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knji080Ctl = new knji080Controller;
var_dump($_REQUEST);
?>
