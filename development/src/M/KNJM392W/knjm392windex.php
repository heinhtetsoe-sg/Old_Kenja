<?php

require_once('for_php7.php');

require_once('knjm392wModel.inc');
require_once('knjm392wQuery.inc');

class knjm392wController extends Controller {
    var $ModelClassName = "knjm392wModel";
    var $ProgramID      = "KNJM392W";

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
                    $this->callView("knjm392wSubForm1");
                    break 2;
                case "":
                case "chg_course":
                case "change":
                case "read":
                case "addread":
                case "main":
                    $this->callView("knjm392wForm1");
                    break 2;
                case "dsub":
                    $this->callView("knjm392wForm1");
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
$knjm392wCtl = new knjm392wController;
?>
