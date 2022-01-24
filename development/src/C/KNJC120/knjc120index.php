<?php

require_once('for_php7.php');

require_once('knjc120Model.inc');
require_once('knjc120Query.inc');

class knjc120Controller extends Controller {
    var $ModelClassName = "knjc120Model";
    var $ProgramID      = "KNJC120";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjc120Model();
                    $this->callView("knjc120Form1");
                    exit;
                case "knjc120":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjc120Model();
                    $this->callView("knjc120Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjc120Ctl = new knjc120Controller;
var_dump($_REQUEST);
?>
