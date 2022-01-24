<?php

require_once('for_php7.php');

require_once('knjl075qModel.inc');
require_once('knjl075qQuery.inc');

class knjl075qController extends Controller {
    var $ModelClassName = "knjl075qModel";
    var $ProgramID      = "KNJL075Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "end":
                    $this->callView("knjl075qForm1");
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
$knjl075qCtl = new knjl075qController;
?>
