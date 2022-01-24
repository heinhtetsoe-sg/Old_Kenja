<?php

require_once('for_php7.php');

require_once('knjl305oModel.inc');
require_once('knjl305oQuery.inc');

class knjl305oController extends Controller {
    var $ModelClassName = "knjl305oModel";
    var $ProgramID      = "knjl305o";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "back1":
                case "next1":
                    $this->callView("knjl305oForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl305oForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "reference":
                    $this->callView("knjl305oForm1");
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
$knjl305oCtl = new knjl305oController;
?>
