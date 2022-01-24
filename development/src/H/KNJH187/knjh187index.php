<?php

require_once('for_php7.php');

require_once('knjh187Model.inc');
require_once('knjh187Query.inc');

class knjh187Controller extends Controller {
    var $ModelClassName = "knjh187Model";
    var $ProgramID      = "KNJH187";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh187":
                case "reset":
                    $this->callView("knjh187Form1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjh187");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh187Ctl = new knjh187Controller;
?>
