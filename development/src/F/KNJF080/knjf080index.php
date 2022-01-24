<?php

require_once('for_php7.php');

require_once('knjf080Model.inc');
require_once('knjf080Query.inc');

class knjf080Controller extends Controller
{
    public $ModelClassName = "knjf080Model";
    public $ProgramID      = "KNJF080";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "init":
                case "list":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf080Form1");
                    break 2;
                case "edit":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf080Form2");
                    break 2;
                case "reset":
                    $this->callView("knjf080Form2");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjf080index.php?cmd=list";
                    $args["right_src"] = "knjf080index.php?cmd=edit";
                    $args["cols"] = "40%,*%";
                    View::frame($args);
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf080Ctl = new knjf080Controller();
