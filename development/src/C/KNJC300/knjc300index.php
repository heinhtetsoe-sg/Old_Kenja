<?php

require_once('for_php7.php');

require_once('knjc300Model.inc');
require_once('knjc300Query.inc');

class knjc300Controller extends Controller {
    var $ModelClassName = "knjc300Model";
    var $ProgramID      = "KNJC300";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "combo":
                case "change":
                case "changeRadio":
                    //$sessionInstance->knjc300Model();
                    $this->callView("knjc300Form1");
                    break 2;
                case "insert":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("reset");
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
$knjc300Ctl = new knjc300Controller;
?>
