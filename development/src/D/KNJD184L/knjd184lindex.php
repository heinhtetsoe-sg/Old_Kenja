<?php

require_once('for_php7.php');

require_once('knjd184lModel.inc');
require_once('knjd184lQuery.inc');

class knjd184lController extends Controller {
    var $ModelClassName = "knjd184lModel";
    var $ProgramID      = "KNJD184L";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd184l":
                    $sessionInstance->knjd184lModel();
                    $this->callView("knjd184lForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd184lCtl = new knjd184lController;
?>
