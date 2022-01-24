<?php

require_once('for_php7.php');

require_once('knjl390qModel.inc');
require_once('knjl390qQuery.inc');

class knjl390qController extends Controller {
    var $ModelClassName = "knjl390qModel";
    var $ProgramID      = "KNJL390Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                    $this->callView("knjl390qForm1");
                    break 2;
                case "exec":
                    $this->callView("knjl390qForm1");
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
$knjl390qCtl = new knjl390qController;
?>
