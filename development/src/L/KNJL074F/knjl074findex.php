<?php

require_once('for_php7.php');

require_once('knjl074fModel.inc');
require_once('knjl074fQuery.inc');

class knjl074fController extends Controller {
    var $ModelClassName = "knjl074fModel";
    var $ProgramID      = "KNJL074F";

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
                    $this->callView("knjl074fForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "":
                    $this->callView("knjl074fForm1");
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
$knjl074fCtl = new knjl074fController;
?>
