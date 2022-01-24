<?php

require_once('for_php7.php');

require_once('knjl304dModel.inc');
require_once('knjl304dQuery.inc');

class knjl304dController extends Controller {
    var $ModelClassName = "knjl304dModel";
    var $ProgramID      = "KNJL304D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl304d":
                    $this->callView("knjl304dForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl304dCtl = new knjl304dController;
?>
