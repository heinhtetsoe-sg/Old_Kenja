<?php

require_once('for_php7.php');

require_once('knjz219Model.inc');
require_once('knjz219Query.inc');

class knjz219Controller extends Controller {
    var $ModelClassName = "knjz219Model";
    var $ProgramID      = "KNJZ219";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main";
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz219Form1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
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
$knjz219Ctl = new knjz219Controller;
//var_dump($_REQUEST);
?>
