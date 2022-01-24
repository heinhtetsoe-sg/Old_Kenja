<?php

require_once('for_php7.php');

require_once('knjl073qModel.inc');
require_once('knjl073qQuery.inc');

class knjl073qController extends Controller {
    var $ModelClassName = "knjl073qModel";
    var $ProgramID      = "KNJL073Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                    $this->callView("knjl073qForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                    $this->callView("knjl073qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl073qCtl = new knjl073qController;
?>
