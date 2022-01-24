<?php

require_once('for_php7.php');

require_once('knjl075fModel.inc');
require_once('knjl075fQuery.inc');

class knjl075fController extends Controller {
    var $ModelClassName = "knjl075fModel";
    var $ProgramID      = "KNJL075F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "app":
                case "main":
                case "read":
                case "back":
                case "next":
                case "reset":
                case "search":
                    $this->callView("knjl075fForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "":
                    $this->callView("knjl075fForm1");
                    exit;
//                    $sessionInstance->setCmd("main");
//                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl075fCtl = new knjl075fController;
?>
