<?php

require_once('for_php7.php');

require_once('knjl009fModel.inc');
require_once('knjl009fQuery.inc');

class knjl009fController extends Controller {
    var $ModelClassName = "knjl009fModel";
    var $ProgramID      = "KNJM260M";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "cmdStart":
                case "main":
                    $this->callView("knjl009fForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("cmdStart");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl009fCtl = new knjl009fController;
//var_dump($_REQUEST);
?>
