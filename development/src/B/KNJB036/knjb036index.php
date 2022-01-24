<?php

require_once('for_php7.php');

require_once('knjb036Model.inc');
require_once('knjb036Query.inc');

class knjb036Controller extends Controller {
    var $ModelClassName = "knjb036Model";
    var $ProgramID        = "KNJB036";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {

                case "main":
                    $this->callView("knjb036Form1");
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
$knjb036Ctl = new knjb036Controller;
//var_dump($_REQUEST);
?>
