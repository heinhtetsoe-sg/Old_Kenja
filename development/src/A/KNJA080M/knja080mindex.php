<?php

require_once('for_php7.php');

require_once('knja080mModel.inc');
require_once('knja080mQuery.inc');

class knja080mController extends Controller {
    var $ModelClassName = "knja080mModel";
    var $ProgramID      = "KNJA080M";
	
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $this->callView("knja080mForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "";
                case "clear";
                case "selectYear":
                case "selectclass":
                    $this->callView("knja080mForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knja080mCtl = new knja080mController;
?>
