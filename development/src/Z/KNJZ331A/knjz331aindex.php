<?php

require_once('for_php7.php');

require_once('knjz331aModel.inc');
require_once('knjz331aQuery.inc');

class knjz331aController extends Controller {
    var $ModelClassName = "knjz331aModel";
    var $ProgramID      = "KNJZ331A";
	
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjz331aForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "selectclass":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->selectclass();
                    break 2;
                case "";
                case "clear";
                case "subMain":
                case "change_menu":
                case "main":
                    $this->callView("knjz331aForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjz331aCtl = new knjz331aController;
?>
