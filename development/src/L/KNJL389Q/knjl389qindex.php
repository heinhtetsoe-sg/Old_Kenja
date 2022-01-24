<?php

require_once('for_php7.php');

require_once('knjl389qModel.inc');
require_once('knjl389qQuery.inc');

class knjl389qController extends Controller {
    var $ModelClassName = "knjl389qModel";
    var $ProgramID      = "KNJL389Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                    $this->callView("knjl389qForm1");
                    break 2;
                case "exec":
                    $this->callView("knjl389qForm1");
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
$knjl389qCtl = new knjl389qController;
?>
