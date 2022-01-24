<?php

require_once('for_php7.php');

require_once('knjd186aModel.inc');
require_once('knjd186aQuery.inc');

class knjd186aController extends Controller {
    var $ModelClassName = "knjd186aModel";
    var $ProgramID      = "KNJD186A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "semechg":
                    $sessionInstance->knjd186aModel();
                    $this->callView("knjd186aForm1");
                    exit;
                case "knjd186a":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd186aModel();
                    $this->callView("knjd186aForm1");
                    exit;
                case "gakki":
                    $sessionInstance->knjd186aModel();
                    $this->callView("knjd186aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd186aCtl = new knjd186aController;
var_dump($_REQUEST);
?>
