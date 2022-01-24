<?php

require_once('for_php7.php');

require_once('knjd234uModel.inc');
require_once('knjd234uQuery.inc');

class knjd234uController extends Controller {
    var $ModelClassName = "knjd234uModel";
    var $ProgramID      = "KNJD234U";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd234uModel();
                    $this->callView("knjd234uForm1");
                    exit;
                case "knjd234u";
                    $sessionInstance->knjd234uModel();
                    $this->callView("knjd234uForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd234uCtl = new knjd234uController;
?>
