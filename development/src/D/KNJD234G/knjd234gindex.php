<?php

require_once('for_php7.php');

require_once('knjd234gModel.inc');
require_once('knjd234gQuery.inc');

class knjd234gController extends Controller {
    var $ModelClassName = "knjd234gModel";
    var $ProgramID      = "KNJD234G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd234gModel();
                    $this->callView("knjd234gForm1");
                    exit;
                case "knjd234g";
                    $sessionInstance->knjd234gModel();
                    $this->callView("knjd234gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd234gCtl = new knjd234gController;
?>
