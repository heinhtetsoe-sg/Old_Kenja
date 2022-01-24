<?php

require_once('for_php7.php');

require_once('knjl321dModel.inc');
require_once('knjl321dQuery.inc');

class knjl321dController extends Controller {
    var $ModelClassName = "knjl321dModel";
    var $ProgramID      = "KNJL321D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl321d":
                case "changeTest":
                    $this->callView("knjl321dForm1");
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
$knjl321dCtl = new knjl321dController;
?>
