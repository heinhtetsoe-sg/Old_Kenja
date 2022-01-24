<?php

require_once('for_php7.php');

require_once('knjd234eModel.inc');
require_once('knjd234eQuery.inc');

class knjd234eController extends Controller {
    var $ModelClassName = "knjd234eModel";
    var $ProgramID      = "KNJD234E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "knjd234eSelectTest":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd234eModel();
                    $this->callView("knjd234eForm1");
                    exit;
                case "csvExe":    //CSV取込
                    $sessionInstance->setAccessLogDetail("EI", $ProgramID); 
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "knjd234e";
                    $sessionInstance->knjd234eModel();
                    $this->callView("knjd234eForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd234eCtl = new knjd234eController;
?>
