<?php

require_once('for_php7.php');

require_once('knje388Model.inc');
require_once('knje388Query.inc');

class knje388Controller extends Controller {
    var $ModelClassName = "knje388Model";
    var $ProgramID      = "KNJE388";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "semechg":
                    $sessionInstance->knje388Model();
                    $this->callView("knje388Form1");
                    exit;
                case "knje388":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knje388Model();
                    $this->callView("knje388Form1");
                    exit;
                case "gakki":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knje388Model();
                    $this->callView("knje388Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje388Ctl = new knje388Controller;
var_dump($_REQUEST);
?>
