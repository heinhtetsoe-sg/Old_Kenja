<?php

require_once('for_php7.php');

require_once('knjl091rModel.inc');
require_once('knjl091rQuery.inc');

class knjl091rController extends Controller {
    var $ModelClassName = "knjl091rModel";
    var $ProgramID      = "KNJL091R";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "change":
                case "change_testdiv2":
                case "changeApp":
                case "back1":
                case "next1":
                    $this->callView("knjl091rForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl091rForm1");
                    break 2;
                case "back2":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl091rForm1");
                    break 2;
                case "next2":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl091rForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "reference":
                    $this->callView("knjl091rForm1");
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
$knjl091rCtl = new knjl091rController;
?>
