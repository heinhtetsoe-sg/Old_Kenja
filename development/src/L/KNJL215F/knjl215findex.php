<?php

require_once('for_php7.php');

require_once('knjl215fModel.inc');
require_once('knjl215fQuery.inc');

class knjl215fController extends Controller {
    var $ModelClassName = "knjl215fModel";
    var $ProgramID      = "KNJL215F";

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
                    $this->callView("knjl215fForm1");
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
$knjl215fCtl = new knjl215fController;
?>
