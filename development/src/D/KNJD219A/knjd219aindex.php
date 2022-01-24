<?php

require_once('for_php7.php');

require_once('knjd219aModel.inc');
require_once('knjd219aQuery.inc');

class knjd219aController extends Controller {
    var $ModelClassName = "knjd219aModel";
    var $ProgramID      = "KNJD219A";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main";
                    $this->callView("knjd219aForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
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
$knjd219aCtl = new knjd219aController;
//var_dump($_REQUEST);
?>
