<?php

require_once('for_php7.php');

require_once('knjz406aModel.inc');
require_once('knjz406aQuery.inc');

class knjz406aController extends Controller {
    var $ModelClassName = "knjz406aModel";
    var $ProgramID      = "KNJZ406A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                   $this->callView("knjz406aForm1");
                   break 2;
                case "change_hrclass":
                case "change_month":
                    $this->callView("knjz406aForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "copy":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getCopyModel();
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
$knjz406aCtl = new knjz406aController;
?>
