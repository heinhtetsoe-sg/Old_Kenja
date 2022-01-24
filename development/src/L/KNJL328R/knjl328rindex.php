<?php

require_once('for_php7.php');

require_once('knjl328rModel.inc');
require_once('knjl328rQuery.inc');

class knjl328rController extends Controller {
    var $ModelClassName = "knjl328rModel";
    var $ProgramID      = "KNJL328R";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl328r":
                    $sessionInstance->knjl328rModel();
                    $this->callView("knjl328rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl328rCtl = new knjl328rController;
?>
