<?php

require_once('for_php7.php');

require_once('knjl302dModel.inc');
require_once('knjl302dQuery.inc');

class knjl302dController extends Controller {
    var $ModelClassName = "knjl302dModel";
    var $ProgramID      = "KNJL302D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl302d":
                    $this->callView("knjl302dForm1");
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
$knjl302dCtl = new knjl302dController;
?>
