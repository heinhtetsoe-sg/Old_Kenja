<?php

require_once('for_php7.php');

require_once('knjm700mModel.inc');
require_once('knjm700mQuery.inc');

class knjm700mController extends Controller {
    var $ModelClassName = "knjm700mModel";
    var $ProgramID      = "KNJM700M";

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
                case "":
                case "read":
                case "addread":
                case "main":
                case "change":
                    $this->callView("knjm700mForm1");
                    break 2;
                case "dsub":
                    $this->callView("knjm700mForm1");
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
$knjm700mCtl = new knjm700mController;
?>
