<?php

require_once('for_php7.php');

require_once('knjz219aModel.inc');
require_once('knjz219aQuery.inc');

class knjz219aController extends Controller {
    var $ModelClassName = "knjz219aModel";
    var $ProgramID      = "KNJZ219A";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main";
                    $this->callView("knjz219aForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "updateAttend":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModelAttend();
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
$knjz219aCtl = new knjz219aController;
//var_dump($_REQUEST);
?>
