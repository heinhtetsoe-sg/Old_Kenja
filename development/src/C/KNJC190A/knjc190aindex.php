<?php

require_once('for_php7.php');

require_once('knjc190aModel.inc');
require_once('knjc190aQuery.inc');

class knjc190aController extends Controller {
    var $ModelClassName = "knjc190aModel";
    var $ProgramID      = "KNJC190A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc190a":
                    $sessionInstance->knjc190aModel();
                    $this->callView("knjc190aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc190aCtl = new knjc190aController;
?>
