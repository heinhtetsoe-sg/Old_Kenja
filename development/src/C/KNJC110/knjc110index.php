<?php

require_once('for_php7.php');

require_once('knjc110Model.inc');
require_once('knjc110Query.inc');

class knjc110Controller extends Controller {
    var $ModelClassName = "knjc110Model";
    var $ProgramID      = "KNJC110";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "semechg":
                    $sessionInstance->knjc110Model();
                    $this->callView("knjc110Form1");
                    exit;
                case "knjc110":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjc110Model();
                    $this->callView("knjc110Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc110Ctl = new knjc110Controller;
//var_dump($_REQUEST);
?>
