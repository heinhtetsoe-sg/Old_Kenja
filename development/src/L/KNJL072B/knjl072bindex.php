<?php

require_once('for_php7.php');

require_once('knjl072bModel.inc');
require_once('knjl072bQuery.inc');

class knjl072bController extends Controller {
    var $ModelClassName = "knjl072bModel";
    var $ProgramID      = "KNJL072B";

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
                    $this->callView("knjl072bForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
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
$knjl072bCtl = new knjl072bController;
?>
