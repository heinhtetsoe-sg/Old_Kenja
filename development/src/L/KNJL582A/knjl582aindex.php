<?php

require_once('for_php7.php');

require_once('knjl582aModel.inc');
require_once('knjl582aQuery.inc');

class knjl582aController extends Controller {
    var $ModelClassName = "knjl582aModel";
    var $ProgramID      = "KNJL582A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "end":
                case "search":
                case "reload":
                case "back":
                case "next":
                    $this->callView("knjl582aForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("search");
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
$knjl582aCtl = new knjl582aController;
?>
