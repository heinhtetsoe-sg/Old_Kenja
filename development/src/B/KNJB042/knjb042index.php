<?php

require_once('for_php7.php');

require_once('knjb042Model.inc');
require_once('knjb042Query.inc');

class knjb042Controller extends Controller {
    var $ModelClassName = "knjb042Model";
    var $ProgramID      = "KNJB042";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb042":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knjb042Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb042Ctl = new knjb042Controller;
?>
