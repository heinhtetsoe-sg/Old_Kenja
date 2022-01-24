<?php

require_once('for_php7.php');

require_once('knjl131fModel.inc');
require_once('knjl131fQuery.inc');

class knjl131fController extends Controller {
    var $ModelClassName = "knjl131fModel";
    var $ProgramID      = "KNJL131F";

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
                    $this->callView("knjl131fForm1");
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
                    $this->callView("knjl131fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl131fCtl = new knjl131fController;
?>
