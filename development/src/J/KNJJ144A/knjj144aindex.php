<?php

require_once('for_php7.php');

require_once('knjj144aModel.inc');
require_once('knjj144aQuery.inc');

class knjj144aController extends Controller {
    var $ModelClassName = "knjj144aModel";
    var $ProgramID      = "KNJJ144A";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                    $this->callView("knjj144aForm1");
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
$knjj144aCtl = new knjj144aController;
//var_dump($_REQUEST);
?>