<?php

require_once('for_php7.php');

require_once('knjc034eModel.inc');
require_once('knjc034eQuery.inc');

class knjc034eController extends Controller {
    var $ModelClassName = "knjc034eModel";
    var $ProgramID      = "KNJC034E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "change":
                case "subclasscd":
                case "chaircd":
                case "reset":
                   $this->callView("knjc034eForm1");
                   break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
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
$knjc034eCtl = new knjc034eController;
?>
