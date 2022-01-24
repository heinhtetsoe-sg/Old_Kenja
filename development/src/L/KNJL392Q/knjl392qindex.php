<?php

require_once('for_php7.php');

require_once('knjl392qModel.inc');
require_once('knjl392qQuery.inc');

class knjl392qController extends Controller {
    var $ModelClassName = "knjl392qModel";
    var $ProgramID      = "KNJL392Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                    $this->callView("knjl392qForm1");
                    break 2;
                case "exec":
                    $this->callView("knjl392qForm1");
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
$knjl392qCtl = new knjl392qController;
?>
