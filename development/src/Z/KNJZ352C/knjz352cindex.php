<?php

require_once('for_php7.php');

require_once('knjz352cModel.inc');
require_once('knjz352cQuery.inc');

class knjz352cController extends Controller {
    var $ModelClassName = "knjz352cModel";
    var $ProgramID      = "KNJZ352C";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjz352c":
                case "reset":
                    $this->callView("knjz352cForm1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjz352c");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "copy":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("knjz352c");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz352cCtl = new knjz352cController;
?>
