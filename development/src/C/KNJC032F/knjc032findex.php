<?php

require_once('for_php7.php');

require_once('knjc032fModel.inc');
require_once('knjc032fQuery.inc');

class knjc032fController extends Controller {
    var $ModelClassName = "knjc032fModel";
    var $ProgramID      = "KNJC032F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "change":
                case "change_course":
                case "change_class":
                case "change_radio":
                case "reset":
                   $this->callView("knjc032fForm1");
                   break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
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

$knjc032fCtl = new knjc032fController;
?>
