<?php

require_once('for_php7.php');

require_once('knjc031cModel.inc');
require_once('knjc031cQuery.inc');

class knjc031cController extends Controller {
    var $ModelClassName = "knjc031cModel";
    var $ProgramID      = "KNJC031C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "change":
                case "reset":
                   $this->callView("knjc031cForm1");
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

$knjc031cCtl = new knjc031cController;
?>
