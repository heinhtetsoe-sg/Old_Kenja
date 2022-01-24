<?php

require_once('for_php7.php');

require_once('knjz177Model.inc');
require_once('knjz177Query.inc');

class knjz177Controller extends Controller {
    var $ModelClassName = "knjz177Model";
    var $ProgramID      = "KNJZ177";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main";
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz177Form1");
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
$knjz177Ctl = new knjz177Controller;
//var_dump($_REQUEST);
?>
