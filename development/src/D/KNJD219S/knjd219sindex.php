<?php

require_once('for_php7.php');


require_once('knjd219sModel.inc');
require_once('knjd219sQuery.inc');

class knjd219sController extends Controller {
    var $ModelClassName = "knjd219sModel";
    var $ProgramID      = "KNJD219S";

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
                    $this->callView("knjd219sForm1");
                    break 2;
                case "main":
                    $this->callView("knjd219sForm1");
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
$knjd219sCtl = new knjd219sController;
?>
