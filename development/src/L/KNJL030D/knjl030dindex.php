<?php

require_once('for_php7.php');

require_once('knjl030dModel.inc');
require_once('knjl030dQuery.inc');

class knjl030dController extends Controller {
    var $ModelClassName = "knjl030dModel";
    var $ProgramID      = "KNJL030D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl030dForm1");
                    break 2;
                case "edit":
                    $this->callView("knjl030dForm2");
                    break 2;
                case "insert":
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
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
$knjl030dCtl = new knjl030dController;
?>
