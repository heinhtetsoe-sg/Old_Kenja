<?php

require_once('for_php7.php');

require_once('knjd154vModel.inc');
require_once('knjd154vQuery.inc');

class knjd154vController extends Controller {
    var $ModelClassName = "knjd154vModel";
    var $ProgramID      = "KNJD154V";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "clear";
                case "knjd154v";
                    $sessionInstance->knjd154vModel();
                    $this->callView("knjd154vForm1");
                    exit;
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd154vModel();
                    $this->callView("knjd154vForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd154vCtl = new knjd154vController;
?>
