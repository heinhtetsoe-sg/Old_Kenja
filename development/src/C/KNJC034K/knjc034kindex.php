<?php

require_once('for_php7.php');

require_once('knjc034kModel.inc');
require_once('knjc034kQuery.inc');

class knjc034kController extends Controller {
    var $ModelClassName = "knjc034kModel";
    var $ProgramID      = "KNJC034K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "change":
                case "subclasscd":
                case "chaircd":
                case "change_class":
                case "reset":
                   $this->callView("knjc034kForm1");
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

$knjc034kCtl = new knjc034kController;
?>
