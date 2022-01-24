<?php

require_once('for_php7.php');

require_once('knjl387qModel.inc');
require_once('knjl387qQuery.inc');

class knjl387qController extends Controller {
    var $ModelClassName = "knjl387qModel";
    var $ProgramID      = "KNJL387Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $this->callView("knjl387qForm1");
                    break 2;
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->cmd = "";
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
$knjl387qCtl = new knjl387qController;
?>
