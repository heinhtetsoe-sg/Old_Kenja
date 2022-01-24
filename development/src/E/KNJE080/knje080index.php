<?php

require_once('for_php7.php');

require_once('knje080Model.inc');
require_once('knje080Query.inc');

class knje080Controller extends Controller {
    var $ModelClassName = "knje080Model";
    var $ProgramID      = "KNJE080";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje080":                          //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knje080Model();    //コントロールマスタの呼び出し
                    $this->callView("knje080Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje080Ctl = new knje080Controller;
?>
