<?php

require_once('for_php7.php');

require_once('knjd186cModel.inc');
require_once('knjd186cQuery.inc');

class knjd186cController extends Controller {
    var $ModelClassName = "knjd186cModel";
    var $ProgramID      = "KNJD186C";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "semechg":
                    $sessionInstance->knjd186cModel();
                    $this->callView("knjd186cForm1");
                    exit;
                case "knjd186c":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd186cModel();
                    $this->callView("knjd186cForm1");
                    exit;
                case "gakki":
                    $sessionInstance->knjd186cModel();
                    $this->callView("knjd186cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd186cCtl = new knjd186cController;
var_dump($_REQUEST);
?>
