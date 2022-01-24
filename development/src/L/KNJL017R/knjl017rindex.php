<?php

require_once('for_php7.php');

require_once('knjl017rModel.inc');
require_once('knjl017rQuery.inc');

class knjl017rController extends Controller {
    var $ModelClassName = "knjl017rModel";
    var $ProgramID      = "KNJL017R";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "changeApp":
                case "changeTest":
                case "changeSh":
                case "main":
                case "read":
                case "reset":
                    $this->callView("knjl017rForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "":
                    $this->callView("knjl017rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl017rCtl = new knjl017rController;
?>
