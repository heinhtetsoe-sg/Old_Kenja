<?php

require_once('for_php7.php');

require_once('knjm390mModel.inc');
require_once('knjm390mQuery.inc');

class knjm390mController extends Controller {
    var $ModelClassName = "knjm390mModel";
    var $ProgramID      = "KNJM390M";

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
                    $this->callView("knjm390mSubForm1");
                    break 2;
                case "":
                case "chg_course":
                case "change":
                case "read":
                case "addread":
                case "main":
                    $this->callView("knjm390mForm1");
                    break 2;
                case "dsub":
                    $this->callView("knjm390mForm1");
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
$knjm390mCtl = new knjm390mController;
?>
