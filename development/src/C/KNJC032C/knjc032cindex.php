<?php

require_once('for_php7.php');

require_once('knjc032cModel.inc');
require_once('knjc032cQuery.inc');

class knjc032cController extends Controller {
    var $ModelClassName = "knjc032cModel";
    var $ProgramID      = "KNJC032C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "change":
                case "change_class":
                case "reset":
                   $this->callView("knjc032cForm1");
                   break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
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

$knjc032cCtl = new knjc032cController;
?>
