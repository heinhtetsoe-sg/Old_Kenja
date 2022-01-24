<?php

require_once('for_php7.php');

require_once('knjl213yModel.inc');
require_once('knjl213yQuery.inc');

class knjl213yController extends Controller {
    var $ModelClassName = "knjl213yModel";
    var $ProgramID      = "KNJL213Y";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "knjl213y":
                    $this->callView("knjl213yForm1");
                    break 2;
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl213yForm1");
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
$knjl213yCtl = new knjl213yController;
?>
