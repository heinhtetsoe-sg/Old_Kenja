<?php

require_once('for_php7.php');

require_once('knjz333Model.inc');
require_once('knjz333Query.inc');

class knjz333Controller extends Controller {
    var $ModelClassName = "knjz333Model";
    var $ProgramID      = "KNJZ333";
	
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjz333Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "selectclass":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->selectclass();
                    break 2;
                case "format":
                    //$sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->setFormatData();
                    $sessionInstance->cmd = "subMain";
                    break 1;
                case "";
                case "clear";
                case "edit";    //あとでいじる！
                case "subMain":
                case "change_menu":
                case "main":
                    $this->callView("knjz333Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjz333Ctl = new knjz333Controller;
?>
