<?php

require_once('for_php7.php');

require_once('knjl130kModel.inc');
require_once('knjl130kQuery.inc');

class knjl130kController extends Controller {
    var $ModelClassName = "knjl130kModel";
    var $ProgramID      = "knjl130k";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "change":
                case "change_judge":
                case "back1":
                case "next1":
                    $this->callView("knjl130kForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl130kForm1");
                    break 2;
                case "back2":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl130kForm1");
                    break 2;
                case "next2":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl130kForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "reference":
                    $this->callView("knjl130kForm1");
                    break 2;
                case "open";
                    $this->callView("knjl130kForm2");
                    break 2;
                case "reset":
                case "change_testdiv":
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
$knjl130kCtl = new knjl130kController;
?>
