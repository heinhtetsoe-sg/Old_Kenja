<?php

require_once('for_php7.php');

require_once('knjb0011Model.inc');
require_once('knjb0011Query.inc');

class knjb0011Controller extends Controller {
    var $ModelClassName = "knjb0011Model";
    var $ProgramID      = "KNJB0011";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "form1":
                case "main":
                case "change":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knjb0011Form1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("subForm1A");
                    break 1;
                case "subForm1":
                case "subForm1A":
                    $this->callView("knjb0011SubForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}

$knjb0011Ctl = new knjb0011Controller;
?>
