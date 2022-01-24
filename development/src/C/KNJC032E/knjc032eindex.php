<?php

require_once('for_php7.php');

require_once('knjc032eModel.inc');
require_once('knjc032eQuery.inc');

class knjc032eController extends Controller {
    var $ModelClassName = "knjc032eModel";
    var $ProgramID      = "KNJC032E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "change":
                case "change_class":
                case "change_radio":
                case "reset":
                   $this->callView("knjc032eForm1");
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

$knjc032eCtl = new knjc032eController;
?>
