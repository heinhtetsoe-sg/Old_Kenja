<?php

require_once('for_php7.php');

require_once('knjl410_1Model.inc');
require_once('knjl410_1Query.inc');

class knjl410_1Controller extends Controller {
    var $ModelClassName = "knjl410_1Model";
    var $ProgramID      = "KNJL410_1";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl410_1":
                case "clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjl410_1Model();
                    $this->callView("knjl410_1Form1");
                    exit;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("knjl410_1");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjl410_1");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl410_1Ctl = new knjl410_1Controller;
?>
