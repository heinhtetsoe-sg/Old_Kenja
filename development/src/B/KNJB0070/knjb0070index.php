<?php

require_once('for_php7.php');

require_once('knjb0070Model.inc');
require_once('knjb0070Query.inc');

class knjb0070Controller extends Controller {
    var $ModelClassName = "knjb0070Model";
    var $ProgramID        = "KNJB0070";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {

                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knjb0070Form1");
                    break 2;

                case "error":
                    $this->callView("error");
                    break 2;

                case "":
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
$knjb0070Ctl = new knjb0070Controller;
//var_dump($_REQUEST);
?>
