<?php

require_once('for_php7.php');

require_once('knjd615fModel.inc');
require_once('knjd615fQuery.inc');

class knjd615fController extends Controller {
    var $ModelClassName = "knjd615fModel";
    var $ProgramID      = "KNJD615F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd615f":
                    $sessionInstance->knjd615fModel();
                    $this->callView("knjd615fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd615fCtl = new knjd615fController;
?>
