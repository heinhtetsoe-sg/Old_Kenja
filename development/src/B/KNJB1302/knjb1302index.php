<?php

require_once('for_php7.php');

require_once('knjb1302Model.inc');
require_once('knjb1302Query.inc');

class knjb1302Controller extends Controller {
    var $ModelClassName = "knjb1302Model";
    var $ProgramID      = "KNJB1302";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "clear";
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjb1302Model();
                    $this->callView("knjb1302Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb1302Ctl = new knjb1302Controller;
?>
