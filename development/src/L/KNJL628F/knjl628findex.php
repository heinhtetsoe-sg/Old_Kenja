<?php

require_once('for_php7.php');

require_once('knjl628fModel.inc');
require_once('knjl628fQuery.inc');

class knjl628fController extends Controller {
    var $ModelClassName = "knjl628fModel";
    var $ProgramID      = "KNJL628F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl628f":
                    $sessionInstance->knjl628fModel();
                    $this->callView("knjl628fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl628fCtl = new knjl628fController;
?>
