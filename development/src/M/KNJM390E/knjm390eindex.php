<?php

require_once('for_php7.php');

require_once('knjm390eModel.inc');
require_once('knjm390eQuery.inc');

class knjm390eController extends Controller {
    var $ModelClassName = "knjm390eModel";
    var $ProgramID      = "KNJM390E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "add":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("addread");
                    break 1;
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
                    $this->callView("knjm390eSubForm1");
                    break 2;
                case "":
                case "chg_chair":
                case "chg_course":
                case "change":
                case "read":
                case "addread":
                case "main":
                    $this->callView("knjm390eForm1");
                    break 2;
                case "dsub":
                    $this->callView("knjm390eForm1");
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
$knjm390eCtl = new knjm390eController;
?>
