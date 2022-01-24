<?php

require_once('for_php7.php');

require_once('knjz023Model.inc');
require_once('knjz023Query.inc');

class knjz023Controller extends Controller {
    var $ModelClassName = "knjz023Model";
    var $ProgramID      = "KNJZ023";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "reset":
                case "change":
                case "main";
                case "defAll";
                case "defMonth";
                case "copy";
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz023Form1");
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
$knjz023Ctl = new knjz023Controller;
//var_dump($_REQUEST);
?>
