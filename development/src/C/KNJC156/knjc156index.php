<?php

require_once('for_php7.php');

require_once('knjc156Model.inc');
require_once('knjc156Query.inc');

class knjc156Controller extends Controller {
    var $ModelClassName = "knjc156Model";
    var $ProgramID      = "KNJC156";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "semechg":
                    $sessionInstance->knjc156Model();
                    $this->callView("knjc156Form1");
                    exit;
                case "knjc156":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjc156Model();
                    $this->callView("knjc156Form1");
                    exit;
                case "gakki":
                    $sessionInstance->knjc156Model();
                    $this->callView("knjc156Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc156Ctl = new knjc156Controller;
var_dump($_REQUEST);
?>
