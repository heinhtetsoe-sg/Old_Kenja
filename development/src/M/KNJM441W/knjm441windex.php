<?php

require_once('for_php7.php');

require_once('knjm441wModel.inc');
require_once('knjm441wQuery.inc');

class knjm441wController extends Controller {
    var $ModelClassName = "knjm441wModel";
    var $ProgramID      = "KNJM441W";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "print":
                case "main":
                case "knjm441w":
                    $sessionInstance->knjm441wModel();
                    $this->callView("knjm441wForm1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "updateprint":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("print");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm441wCtl = new knjm441wController;
?>
