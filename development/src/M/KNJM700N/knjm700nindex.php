<?php

require_once('for_php7.php');

require_once('knjm700nModel.inc');
require_once('knjm700nQuery.inc');

class knjm700nController extends Controller {
    var $ModelClassName = "knjm700nModel";
    var $ProgramID      = "KNJM700N";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "add":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("addread");
                    break 1;
                case "":
                case "chg_hr_name";
                case "read":
                case "addread":
                case "main":
                case "reset":
                    $this->callView("knjm700nForm1");
                    break 2;
                case "dsub":
                    $this->callView("knjm700nForm1");
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
$knjm700nCtl = new knjm700nController;
?>
