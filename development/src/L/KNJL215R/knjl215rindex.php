<?php

require_once('for_php7.php');

require_once('knjl215rModel.inc');
require_once('knjl215rQuery.inc');

class knjl215rController extends Controller {
    var $ModelClassName = "knjl215rModel";
    var $ProgramID      = "KNJL215R";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl215rForm1");
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
$knjl215rCtl = new knjl215rController;
?>
