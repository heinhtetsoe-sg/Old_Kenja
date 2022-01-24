<?php

require_once('for_php7.php');

require_once('knjc161aModel.inc');
require_once('knjc161aQuery.inc');

class knjc161aController extends Controller {
    var $ModelClassName = "knjc161aModel";
    var $ProgramID      = "KNJC161A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc161a":
                    $sessionInstance->knjc161aModel();
                    $this->callView("knjc161aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc161aCtl = new knjc161aController;
?>
