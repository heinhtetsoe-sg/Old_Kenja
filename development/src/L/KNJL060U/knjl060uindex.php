<?php

require_once('for_php7.php');

require_once('knjl060uModel.inc');
require_once('knjl060uQuery.inc');

class knjl060uController extends Controller {
    var $ModelClassName = "knjl060uModel";
    var $ProgramID      = "KNJL060U";

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
                    $this->callView("knjl060uForm1");
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
$knjl060uCtl = new knjl060uController;
?>
