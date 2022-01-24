<?php

require_once('for_php7.php');

require_once('knjg080Model.inc');
require_once('knjg080Query.inc');

class knjg080Controller extends Controller {
    var $ModelClassName = "knjg080Model";
    var $ProgramID      = "KNJG080";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("div");
                    break 1;
                //職員用
                case "update_staff":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel_staff();
                    $sessionInstance->setCmd("div");
                    break 1;
                //保護者用
                case "update_guardian":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel_guardian();
                    $sessionInstance->setCmd("div");
                    break 1;
                case "":
                case "main":
                case "div":
                case "clear";
                case "back";
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjg080Model();
                    $this->callView("knjg080Form1");
                    exit;
                case "form2":
                case "form2_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjg080Form2");
                    break 2;
                case "form2_update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel2();
                    $sessionInstance->setCmd("form2");
                    break 1;
                //職員用
                case "form3":
                case "form3_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjg080Form3");
                    break 2;
                case "form3_update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel2_staff();
                    $sessionInstance->setCmd("form3");
                    break 1;
                //保護者用
                case "form4":
                case "form4_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjg080Form4");
                    break 2;
                case "form4_update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel2_guardian();
                    $sessionInstance->setCmd("form4");
                    break 1;
                //生徒、職員切り換え用
                case "back_change";
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->changeView();
                    $sessionInstance->knjg080Model();
                    $this->callView("knjg080Form1");
                    exit;
                //保護者切り換え用
                case "back_change2";
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->changeView2();
                    $sessionInstance->knjg080Model();
                    $this->callView("knjg080Form1");
                    exit;
                case "knjg080":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjg080Model();       //コントロールマスタの呼び出し
                    $this->callView("knjg080Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjg080Ctl = new knjg080Controller;
//var_dump($_REQUEST);
?>
