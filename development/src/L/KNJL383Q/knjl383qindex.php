<?php

require_once('for_php7.php');

require_once('knjl383qModel.inc');
require_once('knjl383qQuery.inc');

class knjl383qController extends Controller {
    var $ModelClassName = "knjl383qModel";
    var $ProgramID      = "KNJL383Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    //$sessionInstance->setAccessLogDetail("S", $ProgramID);
                    //$sessionInstance->getMainModel();
                    $this->callView("knjl383qForm1");
                    break 2;
                case "update":
                    //$sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->UpdateModel();
                    $sessionInstance->cmd = "";
                    //$this->callView("knjl383qForm1");
                    break 1;
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
$knjl383qCtl = new knjl383qController;
?>
