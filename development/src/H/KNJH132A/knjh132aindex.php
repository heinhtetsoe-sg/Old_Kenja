<?php

require_once('for_php7.php');

require_once('knjh132aModel.inc');
require_once('knjh132aQuery.inc');

class knjh132aController extends Controller {
    var $ModelClassName = "knjh132aModel";
    var $ProgramID      = "KNJH132A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh132a":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjh132aModel();
                    $this->callView("knjh132aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjh132aCtl = new knjh132aController;
?>
