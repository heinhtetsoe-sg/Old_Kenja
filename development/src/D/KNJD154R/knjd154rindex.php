<?php

require_once('for_php7.php');

require_once('knjd154rModel.inc');
require_once('knjd154rQuery.inc');

class knjd154rController extends Controller {
    var $ModelClassName = "knjd154rModel";
    var $ProgramID      = "KNJD154R";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "clear";
                case "knjd154r";
                    $sessionInstance->knjd154rModel();
                    $this->callView("knjd154rForm1");
                    exit;
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd154rModel();
                    $this->callView("knjd154rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd154rCtl = new knjd154rController;
?>
