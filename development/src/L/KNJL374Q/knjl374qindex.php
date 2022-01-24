<?php

require_once('for_php7.php');

require_once('knjl374qModel.inc');
require_once('knjl374qQuery.inc');

class knjl374qController extends Controller {
    var $ModelClassName = "knjl374qModel";
    var $ProgramID      = "KNJL374Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    //$sessionInstance->setAccessLogDetail("S", $ProgramID);
                    //$sessionInstance->getMainModel();
                    $this->callView("knjl374qForm1");
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
$knjl374qCtl = new knjl374qController;
?>
