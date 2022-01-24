<?php

require_once('for_php7.php');

require_once('knjc035bModel.inc');
require_once('knjc035bQuery.inc');

class knjc035bController extends Controller {
    var $ModelClassName = "knjc035bModel";
    var $ProgramID      = "KNJC035B";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc035b":
                    $this->callView("knjc035bForm1");
                    break 2;
                case "exec":
                    $sessionInstance->setAccessLogDetail("E", $ProgramID); 
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("knjc035b");
                    break 1;
                case "histdel":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID); 
                    $sessionInstance->getHistDeleteModel();
                    $sessionInstance->setCmd("knjc035b");
                    break 1;
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
$knjc035bCtl = new knjc035bController;
?>
