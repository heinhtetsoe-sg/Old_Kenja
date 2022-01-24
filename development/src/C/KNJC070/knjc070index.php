<?php

require_once('for_php7.php');

require_once('knjc070Model.inc');
require_once('knjc070Query.inc');

class knjc070Controller extends Controller {
    var $ModelClassName = "knjc070Model";
    var $ProgramID      = "KNJC070";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "semechg":
                    $sessionInstance->knjc070Model();
                    $this->callView("knjc070Form1");
                    exit;
                case "knjc070":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjc070Model();
                    $this->callView("knjc070Form1");
                    exit;
                case "gakki":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjc070Model();
                    $this->callView("knjc070Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc070Ctl = new knjc070Controller;
var_dump($_REQUEST);
?>
