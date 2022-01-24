<?php

require_once('for_php7.php');

require_once('knjd187bModel.inc');
require_once('knjd187bQuery.inc');

class knjd187bController extends Controller {
    var $ModelClassName = "knjd187bModel";
    var $ProgramID      = "KNJD187B";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd187b":
                    $sessionInstance->knjd187bModel();
                    $this->callView("knjd187bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd187bCtl = new knjd187bController;
?>
