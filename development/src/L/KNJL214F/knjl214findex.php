<?php

require_once('for_php7.php');

require_once('knjl214fModel.inc');
require_once('knjl214fQuery.inc');

class knjl214fController extends Controller {
    var $ModelClassName = "knjl214fModel";
    var $ProgramID      = "KNJL214F";

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
                case "reset":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl214fForm1");
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
$knjl214fCtl = new knjl214fController;
?>
