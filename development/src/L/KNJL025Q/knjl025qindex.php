<?php

require_once('for_php7.php');

require_once('knjl025qModel.inc');
require_once('knjl025qQuery.inc');

class knjl025qController extends Controller {
    var $ModelClassName = "knjl025qModel";
    var $ProgramID      = "KNJL025Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "end":
                    $this->callView("knjl025qForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
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
$knjl025qCtl = new knjl025qController;
?>
