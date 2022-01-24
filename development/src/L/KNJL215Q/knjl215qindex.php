<?php

require_once('for_php7.php');

require_once('knjl215qModel.inc');
require_once('knjl215qQuery.inc');

class knjl215qController extends Controller {
    var $ModelClassName = "knjl215qModel";
    var $ProgramID      = "KNJL215Q";

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
                    $this->callView("knjl215qForm1");
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
$knjl215qCtl = new knjl215qController;
?>
