<?php

require_once('for_php7.php');

require_once('knjc122Model.inc');
require_once('knjc122Query.inc');

class knjc122Controller extends Controller {
    var $ModelClassName = "knjc122Model";
    var $ProgramID      = "KNJC122";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjc122Model();
                    $this->callView("knjc122Form1");
                    exit;
                case "knjc122":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjc122Model();
                    $this->callView("knjc122Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc122Ctl = new knjc122Controller;
var_dump($_REQUEST);
?>
