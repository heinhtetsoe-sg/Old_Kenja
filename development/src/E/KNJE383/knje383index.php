<?php

require_once('for_php7.php');

require_once('knje383Model.inc');
require_once('knje383Query.inc');

class knje383Controller extends Controller {
    var $ModelClassName = "knje383Model";
    var $ProgramID      = "KNJE383";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "execute":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID); 
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                    $this->callView("knje383Form1");
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
$knje383Ctl = new knje383Controller;
?>
