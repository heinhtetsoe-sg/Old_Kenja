<?php

require_once('for_php7.php');

require_once('knjb1100Model.inc');
require_once('knjb1100Query.inc');

class knjb1100Controller extends Controller {
    var $ModelClassName = "knjb1100Model";
    var $ProgramID      = "KNJB1100";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main";
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knjb1100Form1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                      $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb1100Ctl = new knjb1100Controller;
//var_dump($_REQUEST);
?>
