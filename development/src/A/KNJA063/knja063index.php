<?php

require_once('for_php7.php');

require_once('knja063Model.inc');
require_once('knja063Query.inc');

class knja063Controller extends Controller {
    var $ModelClassName = "knja063Model";
    var $ProgramID      = "KNJA063";
	
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $this->callView("knja063Form1");
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
                    $this->callView("knja063Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knja063Ctl = new knja063Controller;
?>
