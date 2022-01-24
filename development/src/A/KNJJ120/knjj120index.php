<?php

require_once('for_php7.php');

require_once('knjj120Model.inc');
require_once('knjj120Query.inc');

class knjj120Controller extends Controller {
    var $ModelClassName = "knjj120Model";
    var $ProgramID      = "KNJJ120";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjj120":
                case "change":
                case "clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjj120Model();
                    $this->callView("knjj120Form1");
                    exit;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjj120");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjj120Ctl = new knjj120Controller;
?>
