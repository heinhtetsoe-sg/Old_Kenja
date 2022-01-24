<?php

require_once('for_php7.php');

require_once('knjf152Model.inc');
require_once('knjf152Query.inc');

class knjf152Controller extends Controller {
    var $ModelClassName = "knjf152Model";
    var $ProgramID      = "KNJF152";
    
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
                    $this->callView("knjf152Form1");
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
$knjf152Ctl = new knjf152Controller;
?>
