<?php

require_once('for_php7.php');

require_once('knjl060kModel.inc');
require_once('knjl060kQuery.inc');

class knjl060kController extends Controller {
    var $ModelClassName = "knjl060kModel";
    var $ProgramID      = "KNJL060K";     //プログラムID

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl060kForm1");
                    break 2;
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl060kCtl = new knjl060kController;
?>
