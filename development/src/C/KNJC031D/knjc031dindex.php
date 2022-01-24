<?php

require_once('for_php7.php');

require_once('knjc031dModel.inc');
require_once('knjc031dQuery.inc');

class knjc031dController extends Controller {
    var $ModelClassName = "knjc031dModel";
    var $ProgramID      = "KNJC031D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "change":
                case "reset":
                   $this->callView("knjc031dForm1");
                   break 2;
                case "update":
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

$knjc031dCtl = new knjc031dController;
?>
