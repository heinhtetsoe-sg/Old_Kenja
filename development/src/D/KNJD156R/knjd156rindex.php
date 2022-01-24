<?php

require_once('for_php7.php');

require_once('knjd156rModel.inc');
require_once('knjd156rQuery.inc');

class knjd156rController extends Controller {
    var $ModelClassName = "knjd156rModel";
    var $ProgramID      = "KNJD156R";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "clear";
                case "knjd156r";
                    $sessionInstance->knjd156rModel();
                    $this->callView("knjd156rForm1");
                    exit;
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd156rModel();
                    $this->callView("knjd156rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd156rCtl = new knjd156rController;
?>
