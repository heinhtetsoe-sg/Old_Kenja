<?php

require_once('for_php7.php');

require_once('knjl416Model.inc');
require_once('knjl416Query.inc');

class knjl416Controller extends Controller {
    var $ModelClassName = "knjl416Model";
    var $ProgramID      = "KNJL416";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "execute":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->makeYearData();
                    $this->callView("knjl416Form1");
                    break 2;
                case "":
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->getMainModel();
                    $this->callView("knjl416Form1");
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
$knjl416Ctl = new knjl416Controller;
?>
