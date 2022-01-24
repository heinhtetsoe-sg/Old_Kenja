<?php

require_once('for_php7.php');

require_once('knjl540aModel.inc');
require_once('knjl540aQuery.inc');

class knjl540aController extends Controller {
    var $ModelClassName = "knjl540aModel";
    var $ProgramID      = "KNJL540A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "end":
                    $this->callView("knjl540aForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
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
$knjl540aCtl = new knjl540aController;
?>
