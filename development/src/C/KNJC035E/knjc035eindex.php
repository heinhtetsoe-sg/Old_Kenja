<?php

require_once('for_php7.php');

require_once('knjc035eModel.inc');
require_once('knjc035eQuery.inc');

class knjc035eController extends Controller {
    var $ModelClassName = "knjc035eModel";
    var $ProgramID      = "KNJC035E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                   $this->callView("knjc035eForm1");
                   break 2;
                case "change":
                case "subclasscd":
                case "chaircd":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knjc035eForm1");
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
$knjc035eCtl = new knjc035eController;
?>
