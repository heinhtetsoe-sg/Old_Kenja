<?php

require_once('for_php7.php');

require_once('knjl393qModel.inc');
require_once('knjl393qQuery.inc');

class knjl393qController extends Controller {
    var $ModelClassName = "knjl393qModel";
    var $ProgramID      = "KNJL393Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                    $this->callView("knjl393qForm1");
                    break 2;
                case "exec":
                    $this->callView("knjl393qForm1");
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
$knjl393qCtl = new knjl393qController;
?>
