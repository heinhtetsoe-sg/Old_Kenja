<?php

require_once('for_php7.php');

require_once('knjc031eModel.inc');
require_once('knjc031eQuery.inc');

class knjc031eController extends Controller {
    var $ModelClassName = "knjc031eModel";
    var $ProgramID      = "KNJC031E";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                   $this->callView("knjc031eForm1");
                   break 2;
                case "change_group":
                case "change_hrclass":
                case "change_month":
                case "change_radio":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                   $this->callView("knjc031eForm1");
                   break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
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

$knjc031eCtl = new knjc031eController;
?>
