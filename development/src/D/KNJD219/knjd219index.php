<?php

require_once('for_php7.php');

require_once('knjd219Model.inc');
require_once('knjd219Query.inc');

class knjd219Controller extends Controller {
    var $ModelClassName = "knjd219Model";
    var $ProgramID      = "KNJD219";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main";
                    $this->callView("knjd219Form1");
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
$knjd219Ctl = new knjd219Controller;
//var_dump($_REQUEST);
?>
