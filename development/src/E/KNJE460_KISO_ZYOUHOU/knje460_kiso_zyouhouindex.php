<?php

require_once('for_php7.php');

require_once('knje460_kiso_zyouhouModel.inc');
require_once('knje460_kiso_zyouhouQuery.inc');

class knje460_kiso_zyouhouController extends Controller {
    var $ModelClassName = "knje460_kiso_zyouhouModel";
    var $ProgramID      = "KNJE460_KISO_ZYOUHOU";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "subform1":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje460_kiso_zyouhouForm1");
                    break 2;
                case "subform1_update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knje460_kiso_zyouhouForm1", $sessionInstance->auth);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "kiso_zyouhou_select":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje460_kiso_zyouhouSubForm1");
                    break 2;
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
$knje460_kiso_zyouhouCtl = new knje460_kiso_zyouhouController;
?>
