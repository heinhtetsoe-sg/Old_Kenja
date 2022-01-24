<?php

require_once('for_php7.php');

require_once('knjd452Model.inc');
require_once('knjd452Query.inc');

class knjd452Controller extends Controller {
    var $ModelClassName = "knjd452Model";
    var $ProgramID      = "KNJD452";
	
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjd452Form1");
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
                case "change_hr_class":
                case "main":
                    $this->callView("knjd452Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjd452Ctl = new knjd452Controller;
?>
