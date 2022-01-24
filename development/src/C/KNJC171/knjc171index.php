<?php

require_once('for_php7.php');

require_once('knjc171Model.inc');
require_once('knjc171Query.inc');

class knjc171Controller extends Controller {
    var $ModelClassName = "knjc171Model";
    var $ProgramID      = "KNJC171";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjc171Model();
                    $this->callView("knjc171Form1");
                    exit;
                case "knjc171":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjc171Model();
                    $this->callView("knjc171Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc171Ctl = new knjc171Controller;
?>
