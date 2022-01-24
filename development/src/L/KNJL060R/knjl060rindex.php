<?php

require_once('for_php7.php');

require_once('knjl060rModel.inc');
require_once('knjl060rQuery.inc');

class knjl060rController extends Controller {
    var $ModelClassName = "knjl060rModel";
    var $ProgramID      = "KNJL060R";

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
                    $this->callView("knjl060rForm1");
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
$knjl060rCtl = new knjl060rController;
?>
