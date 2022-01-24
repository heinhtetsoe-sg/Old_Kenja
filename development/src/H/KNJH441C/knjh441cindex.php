<?php

require_once('for_php7.php');

require_once('knjh441cModel.inc');
require_once('knjh441cQuery.inc');

class knjh441cController extends Controller {
    var $ModelClassName = "knjh441cModel";
    var $ProgramID      = "knjh441c";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjh441cModel();
                    $this->callView("knjh441cForm1");
                    exit;
                case "knjh441c";
                    $sessionInstance->knjh441cModel();
                    $this->callView("knjh441cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh441cCtl = new knjh441cController;
?>
