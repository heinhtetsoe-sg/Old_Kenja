<?php

require_once('for_php7.php');

require_once('knjl050qModel.inc');
require_once('knjl050qQuery.inc');

class knjl050qController extends Controller {
    var $ModelClassName = "knjl050qModel";
    var $ProgramID      = "KNJL050Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "end":
                case "main":
                case "read":
                case "back":
                case "next":
                    $this->callView("knjl050qForm1");
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
$knjl050qCtl = new knjl050qController;
?>
