<?php

require_once('for_php7.php');

require_once('knjl241gModel.inc');
require_once('knjl241gQuery.inc');

class knjl241gController extends Controller {
    var $ModelClassName = "knjl241gModel";
    var $ProgramID      = "KNJL241G";

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
                    $this->callView("knjl241gForm1");
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
                    $this->callView("knjl241gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl241gCtl = new knjl241gController;
?>
