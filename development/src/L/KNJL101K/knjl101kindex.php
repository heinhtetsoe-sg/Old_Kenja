<?php

require_once('for_php7.php');

require_once('knjl101kModel.inc');
require_once('knjl101kQuery.inc');

class knjl101kController extends Controller {
    var $ModelClassName = "knjl101kModel";
    var $ProgramID      = "KNJL101K";     //プログラムID

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "edit":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl101kForm1");
                    break 2;
                case "sim":
                    $sessionInstance->getSimModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "decision":
                    $sessionInstance->getDecisionModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "notice":
                    $sessionInstance->getNoticeModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "close":
                    $sessionInstance->getCloseModel();
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
$knjl101kCtl = new knjl101kController;
?>
