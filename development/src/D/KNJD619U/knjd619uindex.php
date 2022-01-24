<?php

require_once('for_php7.php');

require_once('knjd619uModel.inc');
require_once('knjd619uQuery.inc');

class knjd619uController extends Controller {
    var $ModelClassName = "knjd619uModel";
    var $ProgramID      = "KNJD619U";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd619u":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd619uModel();
                    $this->callView("knjd619uForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd619uCtl = new knjd619uController;
?>
