<?php

require_once('for_php7.php');

require_once('knjc170aModel.inc');
require_once('knjc170aQuery.inc');

class knjc170aController extends Controller {
    var $ModelClassName = "knjc170aModel";
    var $ProgramID      = "KNJC170A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "semechg":
                    $sessionInstance->knjc170aModel();
                    $this->callView("knjc170aForm1");
                    exit;
                case "knjc170a":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjc170aModel();
                    $this->callView("knjc170aForm1");
                    exit;
                case "gakki":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjc170aModel();
                    $this->callView("knjc170aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc170aCtl = new knjc170aController;
var_dump($_REQUEST);
?>
