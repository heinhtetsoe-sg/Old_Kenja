<?php

require_once('for_php7.php');

require_once('knjl115kModel.inc');
require_once('knjl115kQuery.inc');

class knjl115kController extends Controller {
    var $ModelClassName = "knjl115kModel";
    var $ProgramID      = "KNJL115K";     //プログラムID

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl115kForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "clear":
                    $sessionInstance->getClearModel();
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
$knjl115kCtl = new knjl115kController;
?>
