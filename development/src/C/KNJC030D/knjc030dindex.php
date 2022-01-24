<?php

require_once('for_php7.php');

require_once('knjc030dModel.inc');
require_once('knjc030dQuery.inc');

class knjc030dController extends Controller {
    var $ModelClassName = "knjc030dModel";
    var $ProgramID      = "KNJC030D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                   $this->callView("knjc030dForm1");
                   break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "updateHrAte":
                case "cancelHrAte":
                    $sessionInstance->getUpdateHrAteModel();
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

$knjc030dCtl = new knjc030dController;
?>
