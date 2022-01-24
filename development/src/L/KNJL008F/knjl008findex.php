<?php

require_once('for_php7.php');

require_once('knjl008fModel.inc');
require_once('knjl008fQuery.inc');

class knjl008fController extends Controller {
    var $ModelClassName = "knjl008fModel";
    var $ProgramID      = "KNJM260M";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "cmdStart":
                case "main":
                    $this->callView("knjl008fForm1");
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
$knjl008fCtl = new knjl008fController;
//var_dump($_REQUEST);
?>
