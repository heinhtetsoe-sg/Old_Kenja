<?php

require_once('for_php7.php');

require_once('knjd183hModel.inc');
require_once('knjd183hQuery.inc');

class knjd183hController extends Controller {
    var $ModelClassName = "knjd183hModel";
    var $ProgramID      = "KNJD183H";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "clear";
                case "knjd183h";
                    $sessionInstance->knjd183hModel();
                    $this->callView("knjd183hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd183hCtl = new knjd183hController;
?>
