<?php

require_once('for_php7.php');

require_once('knjl327aModel.inc');
require_once('knjl327aQuery.inc');

class knjl327aController extends Controller {
    var $ModelClassName = "knjl327aModel";
    var $ProgramID      = "KNJL327A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl327a":
                    $this->callView("knjl327aForm1");
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
$knjl327aCtl = new knjl327aController;
?>
