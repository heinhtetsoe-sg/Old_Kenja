<?php

require_once('for_php7.php');

require_once('knjl327wModel.inc');
require_once('knjl327wQuery.inc');

class knjl327wController extends Controller {
    var $ModelClassName = "knjl327wModel";
    var $ProgramID      = "KNJL327W";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "main":
                case "clear";
                    $this->callView("knjl327wForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl327wCtl = new knjl327wController;
?>
