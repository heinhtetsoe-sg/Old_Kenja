<?php

require_once('for_php7.php');

require_once('knjc155Model.inc');
require_once('knjc155Query.inc');

class knjc155Controller extends Controller {
    var $ModelClassName = "knjc155Model";
    var $ProgramID      = "KNJC155";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "semechg":
                    $sessionInstance->knjc155Model();
                    $this->callView("knjc155Form1");
                    exit;
                case "knjc155":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjc155Model();
                    $this->callView("knjc155Form1");
                    exit;
                case "gakki":
                    $sessionInstance->knjc155Model();
                    $this->callView("knjc155Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc155Ctl = new knjc155Controller;
var_dump($_REQUEST);
?>
