<?php

require_once('for_php7.php');

require_once('knjl090gModel.inc');
require_once('knjl090gQuery.inc');

class knjl090gController extends Controller {
    var $ModelClassName = "knjl090gModel";
    var $ProgramID      = "KNJL090G";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "change":
                case "change_testdiv2":
                case "back1":
                case "next1":
                    $this->callView("knjl090gForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl090gForm1");
                    break 2;
                case "back2":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl090gForm1");
                    break 2;
                case "next2":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl090gForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "reference":
                    $this->callView("knjl090gForm1");
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
$knjl090gCtl = new knjl090gController;
?>
