<?php

require_once('for_php7.php');


require_once('knjd212dModel.inc');
require_once('knjd212dQuery.inc');

class knjd212dController extends Controller {
    var $ModelClassName = "knjd212dModel";
    var $ProgramID      = "KNJD212D";

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
                    $this->callView("knjd212dForm1");
                    break 2;
                case "main":
                    $this->callView("knjd212dForm1");
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
$knjd212dCtl = new knjd212dController;
?>
