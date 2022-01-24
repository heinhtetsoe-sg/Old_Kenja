<?php

require_once('for_php7.php');

require_once('knjb3060Model.inc');
require_once('knjb3060Query.inc');

class knjb3060Controller extends Controller {
    var $ModelClassName = "knjb3060Model";
    var $ProgramID      = "KNJB3060";
	
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjb3060Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "";
                case "main":
                    $this->callView("knjb3060Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjb3060Ctl = new knjb3060Controller;
?>
