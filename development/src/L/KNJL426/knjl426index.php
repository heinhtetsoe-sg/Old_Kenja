<?php

require_once('for_php7.php');

require_once('knjl426Model.inc');
require_once('knjl426Query.inc');

class knjl426Controller extends Controller {
    var $ModelClassName = "knjl426Model";
    var $ProgramID      = "KNJL426";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "execute":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->makeYearData();
                    $this->callView("knjl426Form1");
                    break 2;
                case "":
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->getMainModel();
                    $this->callView("knjl426Form1");
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
$knjl426Ctl = new knjl426Controller;
?>
