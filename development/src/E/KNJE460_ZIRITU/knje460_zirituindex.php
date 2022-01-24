<?php

require_once('for_php7.php');

require_once('knje460_zirituModel.inc');
require_once('knje460_zirituQuery.inc');

class knje460_zirituController extends Controller {
    var $ModelClassName = "knje460_zirituModel";
    var $ProgramID      = "KNJE460_ZIRITU";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "subform1":
                    $this->callView("knje460_zirituForm1");
                    break 2;
                case "subform1_update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knje460_zirituForm1", $sessionInstance->auth);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("subform1");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje460_zirituCtl = new knje460_zirituController;
?>
