<?php

require_once('for_php7.php');

require_once('knjc030_1Model.inc');
require_once('knjc030_1Query.inc');

class knjc030_1Controller extends Controller {
    var $ModelClassName = "knjc030_1Model";
    var $ProgramID      = "KNJC030_1";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "jmp":
                    $this->callView("knjc030_1Form1");
                   break 2;
                case "":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc030_1Ctl = new knjc030_1Controller;
//var_dump($_REQUEST);
?>
