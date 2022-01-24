<?php

require_once('for_php7.php');

require_once('knjc032aModel.inc');
require_once('knjc032aQuery.inc');

class knjc032aController extends Controller {
    var $ModelClassName = "knjc032aModel";
    var $ProgramID      = "KNJC032A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "change":
                case "change_class":
                case "reset":
                   $this->callView("knjc032aForm1");
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

$knjc032aCtl = new knjc032aController;
?>
