<?php

require_once('for_php7.php');

require_once('knjl379qModel.inc');
require_once('knjl379qQuery.inc');

class knjl379qController extends Controller {
    var $ModelClassName = "knjl379qModel";
    var $ProgramID      = "KNJL379Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    //$sessionInstance->setAccessLogDetail("S", $ProgramID);
                    //$sessionInstance->getMainModel();
                    $this->callView("knjl379qForm1");
                    break 2;
                case "update":
                    //$sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->UpdateModel();
                    $sessionInstance->cmd = "";
                    //$this->callView("knjl379qForm1");
                    break 1;
                case "copy":
                    $sessionInstance->getCopyModel();
                    $this->callView("knjl379qForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjl379qCtl = new knjl379qController;
?>
