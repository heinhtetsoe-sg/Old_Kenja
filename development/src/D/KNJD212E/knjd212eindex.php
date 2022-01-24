<?php

require_once('for_php7.php');


require_once('knjd212eModel.inc');
require_once('knjd212eQuery.inc');

class knjd212eController extends Controller {
    var $ModelClassName = "knjd212eModel";
    var $ProgramID      = "KNJD212E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "execute":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knjd212eForm1");
                    break 2;
                case "main":
                    $this->callView("knjd212eForm1");
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
$knjd212eCtl = new knjd212eController;
?>
