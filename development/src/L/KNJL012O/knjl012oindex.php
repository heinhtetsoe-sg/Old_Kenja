<?php

require_once('for_php7.php');

require_once('knjl012oModel.inc');
require_once('knjl012oQuery.inc');

class knjl012oController extends Controller {
    var $ModelClassName = "knjl012oModel";
    var $ProgramID      = "KNJL012O";

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
                    $this->callView("knjl012oForm1");
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
$knjl012oCtl = new knjl012oController;
?>
