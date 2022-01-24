<?php

require_once('for_php7.php');

require_once('knjl511hModel.inc');
require_once('knjl511hQuery.inc');

class knjl511hController extends Controller {
    var $ModelClassName = "knjl511hModel";
    var $ProgramID      = "KNJL511H";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "changeTest":
                case "back1":
                case "next1":
                    $this->callView("knjl511hForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "back":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl511hForm1");
                    break 2;
                case "next":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl511hForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "reference":
                    $this->callView("knjl511hForm1");
                    break 2;
                case "execute":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "reset":
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
$knjl511hCtl = new knjl511hController;
?>
