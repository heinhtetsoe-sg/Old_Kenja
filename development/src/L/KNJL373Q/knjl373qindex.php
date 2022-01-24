<?php

require_once('for_php7.php');

require_once('knjl373qModel.inc');
require_once('knjl373qQuery.inc');

class knjl373qController extends Controller {
    var $ModelClassName = "knjl373qModel";
    var $ProgramID      = "KNJL373Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "search":
                    //$sessionInstance->setAccessLogDetail("S", $ProgramID);
                    //$sessionInstance->getMainModel();
                    $this->callView("knjl373qForm1");
                    break 2;
                case "print":
                    $this->callView("knjl373qForm1");
                    break 2;
                case "create":
                    if(!$sessionInstance->getCsvFile()){
                        $this->callView("knjl373qForm1");
                    }
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
$knjl373qCtl = new knjl373qController;
?>
