<?php

require_once('for_php7.php');

require_once('knjl421Model.inc');
require_once('knjl421Query.inc');

class knjl421Controller extends Controller {
    var $ModelClassName = "knjl421Model";
    var $ProgramID      = "KNJL421";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl421":
                case "clear":
                case "change":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjl421Model();
                    $this->callView("knjl421Form1");
                    exit;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("knjl421");
                    break 1;
                case "insert":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjl421");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjl421");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl421Ctl = new knjl421Controller;
?>
