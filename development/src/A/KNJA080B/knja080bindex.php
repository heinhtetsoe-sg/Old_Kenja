<?php

require_once('for_php7.php');

require_once('knja080bModel.inc');
require_once('knja080bQuery.inc');

class knja080bController extends Controller {
    var $ModelClassName = "knja080bModel";
    var $ProgramID      = "KNJA080B";
	
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $this->callView("knja080bForm1");
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
                    $this->callView("knja080bForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knja080bCtl = new knja080bController;
?>
