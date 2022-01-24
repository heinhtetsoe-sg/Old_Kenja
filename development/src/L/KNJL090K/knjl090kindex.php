<?php

require_once('for_php7.php');

require_once('knjl090kModel.inc');
require_once('knjl090kQuery.inc');

class knjl090kController extends Controller {
    var $ModelClassName = "knjl090kModel";
    var $ProgramID      = "KNJL060O";

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
                    $this->callView("knjl090kForm1");
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
$knjl090kCtl = new knjl090kController;
?>
