<?php

require_once('for_php7.php');

require_once('knjl111oModel.inc');
require_once('knjl111oQuery.inc');

class knjl111oController extends Controller {
    var $ModelClassName = "knjl111oModel";
    var $ProgramID      = "knjl111o";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "saikeisan":
                    $this->callView("knjl111oForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl111oForm1");
                    break 2;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $this->callView("knjl111oForm1");
                    break 2;
                case "back2":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl111oForm1");
                    break 2;
                case "next2":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl111oForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "reference":
                    $this->callView("knjl111oForm1");
                    break 2;
                case "reset":                
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                case "reference":
                    $this->callView("knjl111oForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl111oCtl = new knjl111oController;
?>
