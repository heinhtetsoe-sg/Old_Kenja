<?php

require_once('for_php7.php');

require_once('knjl396qModel.inc');
require_once('knjl396qQuery.inc');

class knjl396qController extends Controller {
    var $ModelClassName = "knjl396qModel";
    var $ProgramID      = "KNJL396Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                    $this->callView("knjl396qForm1");
                    break 2;
                case "exec":
                    $this->callView("knjl396qForm1");
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
$knjl396qCtl = new knjl396qController;
?>
