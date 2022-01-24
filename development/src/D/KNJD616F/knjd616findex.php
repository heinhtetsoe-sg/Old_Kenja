<?php

require_once('for_php7.php');

require_once('knjd616fModel.inc');
require_once('knjd616fQuery.inc');

class knjd616fController extends Controller {
    var $ModelClassName = "knjd616fModel";
    var $ProgramID      = "KNJD616F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd616f":
                    $sessionInstance->knjd616fModel();
                    $this->callView("knjd616fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd616fCtl = new knjd616fController;
?>
