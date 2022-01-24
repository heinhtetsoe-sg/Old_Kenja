<?php

require_once('for_php7.php');

require_once('knjz242Model.inc');
require_once('knjz242Query.inc');

class knjz242Controller extends Controller {
    var $ModelClassName = "knjz242Model";
    var $ProgramID      = "KNJZ242";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "cmdStart":
                case "main":
                case "reset":
                    $this->callView("knjz242Form1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "copy":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("main");
                    break 1;
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
$knjz242Ctl = new knjz242Controller;
?>
