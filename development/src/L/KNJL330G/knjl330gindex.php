<?php

require_once('for_php7.php');

require_once('knjl330gModel.inc');
require_once('knjl330gQuery.inc');

class knjl330gController extends Controller {
    var $ModelClassName = "knjl330gModel";
    var $ProgramID      = "KNJL330G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl330g":
                    $sessionInstance->knjl330gModel();
                    $this->callView("knjl330gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl330gCtl = new knjl330gController;
?>
