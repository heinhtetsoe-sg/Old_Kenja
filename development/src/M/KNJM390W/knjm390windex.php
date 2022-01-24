<?php

require_once('for_php7.php');

require_once('knjm390wModel.inc');
require_once('knjm390wQuery.inc');

class knjm390wController extends Controller {
    var $ModelClassName = "knjm390wModel";
    var $ProgramID      = "KNJM390W";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "add":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("addread");
                    break 1;
                case "alldel":
                case "chdel":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read2");
                    break 1;
                case "subform1":
                case "read2":
                case "reset":
                    $this->callView("knjm390wSubForm1");
                    break 2;
                case "":
                case "chg_course":
                case "change":
                case "read":
                case "addread":
                case "main":
                    $this->callView("knjm390wForm1");
                    break 2;
                case "dsub":
                    $this->callView("knjm390wForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm390wCtl = new knjm390wController;
?>
