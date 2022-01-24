<?php

require_once('for_php7.php');

require_once('knjd184bModel.inc');
require_once('knjd184bQuery.inc');

class knjd184bController extends Controller {
    var $ModelClassName = "knjd184bModel";
    var $ProgramID      = "KNJD184B";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd184b":
                    $sessionInstance->knjd184bModel();
                    $this->callView("knjd184bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd184bCtl = new knjd184bController;
?>
