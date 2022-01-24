<?php

require_once('for_php7.php');

require_once('knjb105Model.inc');
require_once('knjb105Query.inc');

class knjb105Controller extends Controller {
    var $ModelClassName = "knjb105Model";
    var $ProgramID      = "KNJB105";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "semechg":
                    $sessionInstance->knjb105Model();
                    $this->callView("knjb105Form1");
                    exit;
                case "knjb105":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjb105Model();
                    $this->callView("knjb105Form1");
                    exit;
                case "gakki":
                    $sessionInstance->knjb105Model();
                    $this->callView("knjb105Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb105Ctl = new knjb105Controller;
var_dump($_REQUEST);
?>
