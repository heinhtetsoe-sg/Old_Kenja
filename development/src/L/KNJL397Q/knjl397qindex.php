<?php

require_once('for_php7.php');

require_once('knjl397qModel.inc');
require_once('knjl397qQuery.inc');

class knjl397qController extends Controller {
    var $ModelClassName = "knjl397qModel";
    var $ProgramID      = "KNJL397Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                    $this->callView("knjl397qForm1");
                    break 2;
                case "exec":
                    $this->callView("knjl397qForm1");
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
$knjl397qCtl = new knjl397qController;
?>
