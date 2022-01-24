<?php

require_once('for_php7.php');

require_once('knjl075pModel.inc');
require_once('knjl075pQuery.inc');

class knjl075pController extends Controller {
    var $ModelClassName = "knjl075pModel";
    var $ProgramID      = "KNJL075P";

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
                    $this->callView("knjl075pForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "":
                    $this->callView("knjl075pForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl075pCtl = new knjl075pController;
?>
