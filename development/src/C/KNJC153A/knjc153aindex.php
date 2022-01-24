<?php

require_once('for_php7.php');

require_once('knjc153aModel.inc');
require_once('knjc153aQuery.inc');

class knjc153aController extends Controller {
    var $ModelClassName = "knjc153aModel";
    var $ProgramID      = "KNJC153A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjc153aModel();
                    $this->callView("knjc153aForm1");
                    exit;
                case "knjc153a":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjc153aModel();
                    $this->callView("knjc153aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc153aCtl = new knjc153aController;
?>
