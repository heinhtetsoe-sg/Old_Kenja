<?php

require_once('for_php7.php');

require_once('knjl075gModel.inc');
require_once('knjl075gQuery.inc');

class knjl075gController extends Controller {
    var $ModelClassName = "knjl075gModel";
    var $ProgramID      = "KNJL075G";

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
                    $this->callView("knjl075gForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "":
                    $this->callView("knjl075gForm1");
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
$knjl075gCtl = new knjl075gController;
?>
