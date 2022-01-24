<?php

require_once('for_php7.php');

require_once('knjd219fModel.inc');
require_once('knjd219fQuery.inc');

class knjd219fController extends Controller {
    var $ModelClassName = "knjd219fModel";
    var $ProgramID      = "KNJD219F";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main";
                    $this->callView("knjd219fForm1");
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
$knjd219fCtl = new knjd219fController;
//var_dump($_REQUEST);
?>
