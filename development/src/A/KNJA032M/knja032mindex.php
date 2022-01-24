<?php

require_once('for_php7.php');

require_once('knja032mModel.inc');
require_once('knja032mQuery.inc');

class knja032mController extends Controller {
    var $ModelClassName = "knja032mModel";
    var $ProgramID      = "KNJA032M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "sort":
                case "main":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->getMainModel();
                    $this->callView("knja032mForm1");
                   break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                case "read":
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
$knja032mCtl = new knja032mController;
//var_dump($_REQUEST);
?>
