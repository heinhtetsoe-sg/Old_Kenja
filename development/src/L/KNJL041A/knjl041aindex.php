<?php

require_once('for_php7.php');

require_once('knjl041aModel.inc');
require_once('knjl041aQuery.inc');

class knjl041aController extends Controller {
    var $ModelClassName = "knjl041aModel";
    var $ProgramID      = "KNJL041A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "end":
                    $this->callView("knjl041aForm1");
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
$knjl041aCtl = new knjl041aController;
?>
