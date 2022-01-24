<?php

require_once('for_php7.php');

require_once('knjb1257Model.inc');
require_once('knjb1257Query.inc');

class knjb1257Controller extends Controller {
    var $ModelClassName = "knjb1257Model";
    var $ProgramID      = "KNJB1257";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "change":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knjb1257Form1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
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

$knjb1257Ctl = new knjb1257Controller;
?>
