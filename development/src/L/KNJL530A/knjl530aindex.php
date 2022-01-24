<?php

require_once('for_php7.php');

require_once('knjl530aModel.inc');
require_once('knjl530aQuery.inc');

class knjl530aController extends Controller {
    var $ModelClassName = "knjl530aModel";
    var $ProgramID      = "KNJL530A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "end":
                    $this->callView("knjl530aForm1");
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
$knjl530aCtl = new knjl530aController;
?>
