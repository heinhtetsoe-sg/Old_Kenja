<?php

require_once('for_php7.php');

require_once('knjl053rModel.inc');
require_once('knjl053rQuery.inc');

class knjl053rController extends Controller {
    var $ModelClassName = "knjl053rModel";
    var $ProgramID      = "KNJL053R";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "back":
                case "next":
                case "reset":
                    $this->callView("knjl053rForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
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
$knjl053rCtl = new knjl053rController;
?>
