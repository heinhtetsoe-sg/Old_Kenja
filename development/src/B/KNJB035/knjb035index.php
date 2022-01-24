<?php

require_once('for_php7.php');

require_once('knjb035Model.inc');
require_once('knjb035Query.inc');

class knjb035Controller extends Controller {
    var $ModelClassName = "knjb035Model";
    var $ProgramID        = "KNJB035";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {

                case "main":
                    $this->callView("knjb035Form1");
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
$knjb035Ctl = new knjb035Controller;
//var_dump($_REQUEST);
?>
