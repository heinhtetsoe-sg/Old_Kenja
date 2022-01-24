<?php

require_once('for_php7.php');

require_once('knjb102Model.inc');
require_once('knjb102Query.inc');

class knjb102Controller extends Controller {
    var $ModelClassName = "knjb102Model";
    var $ProgramID      = "KNJB102";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "clear";
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjb102Model();
                    $this->callView("knjb102Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb102Ctl = new knjb102Controller;
?>
