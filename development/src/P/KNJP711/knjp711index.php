<?php

require_once('for_php7.php');

require_once('knjp711Model.inc');
require_once('knjp711Query.inc');

class knjp711Controller extends Controller {
    var $ModelClassName = "knjp711Model";
    var $ProgramID      = "KNJP711";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp711":
                case "reset":
                    $this->callView("knjp711Form1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjp711");
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
$knjp711Ctl = new knjp711Controller;
?>
