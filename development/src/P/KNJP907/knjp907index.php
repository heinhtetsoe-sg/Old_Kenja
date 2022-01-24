<?php

require_once('for_php7.php');

require_once('knjp907Model.inc');
require_once('knjp907Query.inc');

class knjp907Controller extends Controller {
    var $ModelClassName = "knjp907Model";
    var $ProgramID      = "KNJP907";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "changeSeme":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjp907Model();
                    $this->callView("knjp907Form1");
                    exit;
                case "knjp907":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjp907Model();       //コントロールマスタの呼び出し
                    $this->callView("knjp907Form1");
                    exit;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->updateExe();
                    $sessionInstance->cmd = 'main';
                    break 1;
                case "cancel_update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->cancelExe();
                    $sessionInstance->cmd = 'main';
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjp907Ctl = new knjp907Controller;
//var_dump($_REQUEST);
?>
