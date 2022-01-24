<?php

require_once('for_php7.php');

require_once('knjz291_staff_workhistModel.inc');
require_once('knjz291_staff_workhistQuery.inc');

class knjz291_staff_workhistController extends Controller {
    var $ModelClassName = "knjz291_staff_workhistModel";
    var $ProgramID      = "KNJZ291_STAFF_WORKHIST";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main";
                case "click";
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz291_staff_workhistForm1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "insert":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
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
$knjz291_staff_workhistCtl = new knjz291_staff_workhistController;
?>
