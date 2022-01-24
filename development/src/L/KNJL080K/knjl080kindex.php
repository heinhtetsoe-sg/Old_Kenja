<?php

require_once('for_php7.php');

require_once('knjl080kModel.inc');
require_once('knjl080kQuery.inc');

class knjl080kController extends Controller {
    var $ModelClassName = "knjl080kModel";
    var $ProgramID      = "KNJL080K";     //プログラムID

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "reset":
                case "back2":
                case "next2":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl080kForm1");
                    break 2;
                case "back":
                case "next":
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
$knjl080kCtl = new knjl080kController;
?>
