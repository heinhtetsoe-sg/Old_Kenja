<?php

require_once('for_php7.php');

require_once('knjl021eModel.inc');
require_once('knjl021eQuery.inc');

class knjl021eController extends Controller {
    var $ModelClassName = "knjl021eModel";
    var $ProgramID      = "KNJL021E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "back1":
                case "next1":
                case "reference":
                    $this->callView("knjl021eForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "back":
                case "next":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl021eForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "reset":
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
$knjl021eCtl = new knjl021eController;
?>
