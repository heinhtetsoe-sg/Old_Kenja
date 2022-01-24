<?php

require_once('for_php7.php');

require_once('knjl395qModel.inc');
require_once('knjl395qQuery.inc');

class knjl395qController extends Controller {
    var $ModelClassName = "knjl395qModel";
    var $ProgramID      = "KNJL395Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                    $this->callView("knjl395qForm1");
                    break 2;
                case "exec":
                    $this->callView("knjl395qForm1");
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
$knjl395qCtl = new knjl395qController;
?>
