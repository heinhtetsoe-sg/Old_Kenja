<?php

require_once('for_php7.php');

require_once('knjh543bModel.inc');
require_once('knjh543bQuery.inc');

class knjh543bController extends Controller {
    var $ModelClassName = "knjh543bModel";
    var $ProgramID      = "KNJH543B";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "change":
                    $this->callView("knjh543bForm1");
                    break 2;
                case "update":
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
$knjh543bCtl = new knjh543bController;
//var_dump($_REQUEST);
?>
