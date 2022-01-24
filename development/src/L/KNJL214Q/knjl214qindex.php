<?php

require_once('for_php7.php');

require_once('knjl214qModel.inc');
require_once('knjl214qQuery.inc');

class knjl214qController extends Controller {
    var $ModelClassName = "knjl214qModel";
    var $ProgramID      = "KNJL214Q";

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
                    $this->callView("knjl214qForm1");
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
$knjl214qCtl = new knjl214qController;
?>
