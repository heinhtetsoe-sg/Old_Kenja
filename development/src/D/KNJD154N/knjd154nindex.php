<?php

require_once('for_php7.php');

require_once('knjd154nModel.inc');
require_once('knjd154nQuery.inc');

class knjd154nController extends Controller {
    var $ModelClassName = "knjd154nModel";
    var $ProgramID      = "KNJD154N";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "clear";
                case "knjd154n";
                    $sessionInstance->knjd154nModel();
                    $this->callView("knjd154nForm1");
                    exit;
                case "main":
                case "changeDateDiv":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd154nModel();
                    $this->callView("knjd154nForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd154nCtl = new knjd154nController;
?>
