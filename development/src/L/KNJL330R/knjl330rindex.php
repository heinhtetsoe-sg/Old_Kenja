<?php

require_once('for_php7.php');

require_once('knjl330rModel.inc');
require_once('knjl330rQuery.inc');

class knjl330rController extends Controller {
    var $ModelClassName = "knjl330rModel";
    var $ProgramID      = "KNJL330R";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl330r":
                    $sessionInstance->knjl330rModel();
                    $this->callView("knjl330rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl330rCtl = new knjl330rController;
?>
