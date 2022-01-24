<?php

require_once('for_php7.php');

require_once('knjl042qModel.inc');
require_once('knjl042qQuery.inc');

class knjl042qController extends Controller {
    var $ModelClassName = "knjl042qModel";
    var $ProgramID      = "KNJL042Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                    $this->callView("knjl042qForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                    $this->callView("knjl042qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl042qCtl = new knjl042qController;
?>
