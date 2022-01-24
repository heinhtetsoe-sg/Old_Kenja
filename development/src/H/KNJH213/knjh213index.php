<?php

require_once('for_php7.php');

require_once('knjh213Model.inc');
require_once('knjh213Query.inc');

class knjh213Controller extends Controller {
    var $ModelClassName = "knjh213Model";
    var $ProgramID      = "KNJH213";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "cmdStart":
                case "knjh213":
                case "reset":
                    $this->callView("knjh213Form1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjh213");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("cmdStart");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh213Ctl = new knjh213Controller;
?>
