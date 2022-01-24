<?php

require_once('for_php7.php');

require_once('knjz620Model.inc');
require_once('knjz620Query.inc');
require_once('graph.php');

class knjz620Controller extends Controller {
    var $ModelClassName = "knjz620Model";
    var $ProgramID      = "KNJZ620";
	
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjz620Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "selectclass":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->selectclass();
                    break 2;
                case "";
                case "reappear";
                case "syubetu_change";
                case "sanka";
                    $this->callView("knjz620Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjz620Ctl = new knjz620Controller;
?>
