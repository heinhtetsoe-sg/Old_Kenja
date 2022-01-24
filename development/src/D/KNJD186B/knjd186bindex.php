<?php

require_once('for_php7.php');

require_once('knjd186bModel.inc');
require_once('knjd186bQuery.inc');

class knjd186bController extends Controller {
    var $ModelClassName = "knjd186bModel";
    var $ProgramID      = "KNJD186B";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "semechg":
                    $sessionInstance->knjd186bModel();
                    $this->callView("knjd186bForm1");
                    exit;
                case "knjd186b":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd186bModel();
                    $this->callView("knjd186bForm1");
                    exit;
                case "gakki":
                    $sessionInstance->knjd186bModel();
                    $this->callView("knjd186bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd186bCtl = new knjd186bController;
var_dump($_REQUEST);
?>
