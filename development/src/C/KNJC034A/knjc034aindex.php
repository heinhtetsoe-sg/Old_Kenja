<?php

require_once('for_php7.php');

require_once('knjc034aModel.inc');
require_once('knjc034aQuery.inc');

class knjc034aController extends Controller {
    var $ModelClassName = "knjc034aModel";
    var $ProgramID      = "KNJC034A";

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
                   $this->callView("knjc034aForm1");
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

$knjc034aCtl = new knjc034aController;
?>
