<?php

require_once('for_php7.php');

require_once('knjl041pModel.inc');
require_once('knjl041pQuery.inc');

class knjl041pController extends Controller {
    var $ModelClassName = "knjl041pModel";
    var $ProgramID      = "KNJL041P";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "app":
                case "main":
                case "read":
                case "back":
                case "next":
                case "reset":
                    $this->callView("knjl041pForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "":
                    $this->callView("knjl041pForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl041pCtl = new knjl041pController;
?>
