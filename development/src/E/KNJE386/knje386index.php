<?php

require_once('for_php7.php');

require_once('knje386Model.inc');
require_once('knje386Query.inc');

class knje386Controller extends Controller {
    var $ModelClassName = "knje386Model";
    var $ProgramID      = "KNJE386";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->setAccessLogDetail("E", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->getMainModel();
                    $this->callView("knje386Form1");
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
$knje386Ctl = new knje386Controller;
?>
