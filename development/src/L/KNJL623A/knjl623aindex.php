<?php

require_once('for_php7.php');

require_once('knjl623aModel.inc');
require_once('knjl623aQuery.inc');

class knjl623aController extends Controller {
    var $ModelClassName = "knjl623aModel";
    var $ProgramID      = "KNJL623A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl623a":
                    $this->callView("knjl623aForm1");
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
$knjl623aCtl = new knjl623aController;
?>
