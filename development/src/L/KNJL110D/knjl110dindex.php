<?php

require_once('for_php7.php');

require_once('knjl110dModel.inc');
require_once('knjl110dQuery.inc');

class knjl110dController extends Controller {
    var $ModelClassName = "knjl110dModel";
    var $ProgramID      = "KNJL110D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "changeTest":
                case "back1":
                case "next1":
                    $this->callView("knjl110dForm1");
                    break 2;
                case "add":
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "reference":
                    $this->callView("knjl110dForm1");
                    break 2;
                case "execute":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
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
$knjl110dCtl = new knjl110dController;
?>
