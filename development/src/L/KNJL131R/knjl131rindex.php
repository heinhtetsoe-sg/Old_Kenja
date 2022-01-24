<?php

require_once('for_php7.php');

require_once('knjl131rModel.inc');
require_once('knjl131rQuery.inc');

class knjl131rController extends Controller {
    var $ModelClassName = "knjl131rModel";
    var $ProgramID      = "KNJL131R";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "app":
                case "main":
                case "read":
                case "reset":
                case "print":
                    $this->callView("knjl131rForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "updatePrint":
                    $sessionInstance->getUpdatePrintModel();
                    $sessionInstance->setCmd("print");
                    break 1;
                case "":
                    $this->callView("knjl131rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl131rCtl = new knjl131rController;
?>
