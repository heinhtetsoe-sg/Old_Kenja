<?php

require_once('for_php7.php');

require_once('knjl330uModel.inc');
require_once('knjl330uQuery.inc');

class knjl330uController extends Controller {
    var $ModelClassName = "knjl330uModel";
    var $ProgramID      = "KNJL330U";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl330u":
                    $this->callView("knjl330uForm1");
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
$knjl330uCtl = new knjl330uController;
?>
