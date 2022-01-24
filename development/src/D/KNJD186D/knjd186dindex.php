<?php

require_once('for_php7.php');

require_once('knjd186dModel.inc');
require_once('knjd186dQuery.inc');

class knjd186dController extends Controller {
    var $ModelClassName = "knjd186dModel";
    var $ProgramID      = "KNJD186D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd186d":
                    $sessionInstance->knjd186dModel();
                    $this->callView("knjd186dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjd186dCtl = new knjd186dController;
?>
