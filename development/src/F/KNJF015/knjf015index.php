<?php

require_once('for_php7.php');

require_once('knjf015Model.inc');
require_once('knjf015Query.inc');

class knjf015Controller extends Controller {
    var $ModelClassName = "knjf015Model";
    var $ProgramID      = "KNJF015";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "semechg":
                    $sessionInstance->knjf015Model();
                    $this->callView("knjf015Form1");
                    exit;
                case "knjf015":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjf015Model();
                    $this->callView("knjf015Form1");
                    exit;
                case "gakki":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjf015Model();
                    $this->callView("knjf015Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf015Ctl = new knjf015Controller;
var_dump($_REQUEST);
?>
