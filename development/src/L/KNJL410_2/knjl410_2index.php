<?php

require_once('for_php7.php');

require_once('knjl410_2Model.inc');
require_once('knjl410_2Query.inc');

class knjl410_2Controller extends Controller {
    var $ModelClassName = "knjl410_2Model";
    var $ProgramID      = "KNJL410_2";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl410_2":
                case "clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjl410_2Model();
                    $this->callView("knjl410_2Form1");
                    exit;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("knjl410_2");
                    break 1;
                case "insert":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjl410_2");
                    break 1;
                case "update":
                case "update2":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjl410_2");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl410_2Ctl = new knjl410_2Controller;
?>
