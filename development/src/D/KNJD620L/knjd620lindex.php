<?php

require_once('for_php7.php');

require_once('knjd620lModel.inc');
require_once('knjd620lQuery.inc');

class knjd620lController extends Controller {
    var $ModelClassName = "knjd620lModel";
    var $ProgramID      = "KNJD620L";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd620lModel();
                    $this->callView("knjd620lForm1");
                    exit;
                case "knjd620l":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd620lModel();
                    $this->callView("knjd620lForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd620lCtl = new knjd620lController;
?>
