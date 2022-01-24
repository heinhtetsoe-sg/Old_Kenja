<?php

require_once('for_php7.php');

require_once('knjd234wModel.inc');
require_once('knjd234wQuery.inc');

class knjd234wController extends Controller {
    var $ModelClassName = "knjd234wModel";
    var $ProgramID      = "KNJD234W";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "knjd234wSelectTest":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd234wModel();
                    $this->callView("knjd234wForm1");
                    exit;
                case "csvExe":    //CSV取込
                    $sessionInstance->setAccessLogDetail("EI", $ProgramID); 
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "knjd234w";
                    $sessionInstance->knjd234wModel();
                    $this->callView("knjd234wForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd234wCtl = new knjd234wController;
?>
