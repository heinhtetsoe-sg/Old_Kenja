<?php

require_once('for_php7.php');

require_once('knjl054dModel.inc');
require_once('knjl054dQuery.inc');

class knjl054dController extends Controller {
    var $ModelClassName = "knjl054dModel";
    var $ProgramID      = "KNJL054D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                    $this->callView("knjl054dForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                    $this->callView("knjl054dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl054dCtl = new knjl054dController;
?>
