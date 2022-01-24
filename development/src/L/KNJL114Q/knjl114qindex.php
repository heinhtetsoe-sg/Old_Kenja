<?php

require_once('for_php7.php');

require_once('knjl114qModel.inc');
require_once('knjl114qQuery.inc');

class knjl114qController extends Controller {
    var $ModelClassName = "knjl114qModel";
    var $ProgramID      = "KNJL114Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "reset":
                case "end":
                    $this->callView("knjl114qForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
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
$knjl114qCtl = new knjl114qController;
?>
