<?php

require_once('for_php7.php');

require_once('knjz439Model.inc');
require_once('knjz439Query.inc');

class knjz439Controller extends Controller {
    var $ModelClassName = "knjz439Model";
    var $ProgramID      = "KNJZ439";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main";
                    $this->callView("knjz439Form1");
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
$knjz439Ctl = new knjz439Controller;
//var_dump($_REQUEST);
?>
