<?php

require_once('for_php7.php');

require_once('knjc032bModel.inc');
require_once('knjc032bQuery.inc');

class knjc032bController extends Controller {
    var $ModelClassName = "knjc032bModel";
    var $ProgramID      = "KNJC032B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "change":
                case "change_class":
                case "reset":
                   $this->callView("knjc032bForm1");
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

$knjc032bCtl = new knjc032bController;
?>
