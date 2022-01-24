<?php
require_once('knjl142kModel.inc');
require_once('knjl142kQuery.inc');

class knjl142kController extends Controller {
    var $ModelClassName = "knjl142kModel";
    var $ProgramID      = "KNJL142K";     //プログラムID

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "back2":
                case "next2":
                    $this->callView("knjl142kForm1");
                    break 2;
                case "back":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("back2");
                    break 1;
                case "next":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("next2");
                    break 1;
                case "update":
                    $sessionInstance->getUpdateModel();
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
$knjl142kCtl = new knjl142kController;
?>
