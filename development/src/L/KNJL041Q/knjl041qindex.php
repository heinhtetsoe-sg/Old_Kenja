<?php

require_once('for_php7.php');

require_once('knjl041qModel.inc');
require_once('knjl041qQuery.inc');

class knjl041qController extends Controller {
    var $ModelClassName = "knjl041qModel";
    var $ProgramID      = "KNJL041Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "app":
                case "main":
                case "read":
                case "back":
                case "next":
                    $this->callView("knjl041qForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "":
                    $this->callView("knjl041qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl041qCtl = new knjl041qController;
?>
