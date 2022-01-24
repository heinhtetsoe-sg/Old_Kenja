<?php

require_once('for_php7.php');

require_once('knjf100aModel.inc');
require_once('knjf100aQuery.inc');

class knjf100aController extends Controller {
    var $ModelClassName = "knjf100aModel";
    var $ProgramID      = "KNJF100A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf100a":
                case "semechg":
                    $sessionInstance->knjf100aModel();
                    $this->callView("knjf100aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("{滋ymu=q写r`rmrur&r2{$sessionInstance->cmd}q偲q七"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf100aCtl = new knjf100aController;
//var_dump($_REQUEST);
?>
