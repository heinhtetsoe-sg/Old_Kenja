<?php

require_once('for_php7.php');

require_once('knjl011oModel.inc');
require_once('knjl011oQuery.inc');

class knjl011oController extends Controller {
    var $ModelClassName = "knjl011oModel";
    var $ProgramID      = "KNJL011O";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "back1":
                case "next1":
                    $this->callView("knjl011oForm1");
                    break 2;
                case "brother":
                    $this->callView("knjl011oForm2");
                    break 2;
                case "brother_update":
                    $sessionInstance->getBrotherUpdateModel();
                    $sessionInstance->setCmd("brother");
                    break 1;
                case "add":
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "back":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl011oForm1");
                    break 2;
                case "next":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl011oForm1");
                    break 2;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "reference":
                    $this->callView("knjl011oForm1");
                    break 2;
                case "reset":                
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
$knjl011oCtl = new knjl011oController;
?>
