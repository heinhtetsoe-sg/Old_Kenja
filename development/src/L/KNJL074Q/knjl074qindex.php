<?php

require_once('for_php7.php');

require_once('knjl074qModel.inc');
require_once('knjl074qQuery.inc');

class knjl074qController extends Controller {
    var $ModelClassName = "knjl074qModel";
    var $ProgramID      = "KNJL074Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                    $this->callView("knjl074qForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                    $this->callView("knjl074qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl074qCtl = new knjl074qController;
?>
