<?php

require_once('for_php7.php');

require_once('knjc152Model.inc');
require_once('knjc152Query.inc');

class knjc152Controller extends Controller {
    var $ModelClassName = "knjc152Model";
    var $ProgramID      = "KNJC152";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc152":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjc152Model();
                    $this->callView("knjc152Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc152Ctl = new knjc152Controller;
var_dump($_REQUEST);
?>
