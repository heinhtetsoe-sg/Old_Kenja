<?php

require_once('for_php7.php');

require_once('knjc031fModel.inc');
require_once('knjc031fQuery.inc');

class knjc031fController extends Controller {
    var $ModelClassName = "knjc031fModel";
    var $ProgramID      = "KNJC031F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                   $this->callView("knjc031fForm1");
                   break 2;
                case "change_course":
                case "change_group":
                case "change_hrclass":
                case "change_month":
                case "change_radio":
                   $this->callView("knjc031fForm1");
                   break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
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
$knjc031fCtl = new knjc031fController;
?>
