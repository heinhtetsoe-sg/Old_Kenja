<?php

require_once('for_php7.php');

require_once('knjl217qModel.inc');
require_once('knjl217qQuery.inc');

class knjl217qController extends Controller {
    var $ModelClassName = "knjl217qModel";
    var $ProgramID      = "KNJL217Q";

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
                    $this->callView("knjl217qForm1");
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
$knjl217qCtl = new knjl217qController;
?>
