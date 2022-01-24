<?php

require_once('for_php7.php');

require_once('knjh121Model.inc');
require_once('knjh121Query.inc');

class knjh121Controller extends Controller {
    var $ModelClassName = "knjh121Model";
    var $ProgramID      = "KNJH121";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh121":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjh121Model();
                    $this->callView("knjh121Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh121Ctl = new knjh121Controller;
//var_dump($_REQUEST);
?>
