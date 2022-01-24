<?php

require_once('for_php7.php');

require_once('knjc030Model.inc');
require_once('knjc030Query.inc');

class knjc030Controller extends Controller {
    var $ModelClassName = "knjc030Model";
    var $ProgramID        = "KNJC030";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {

                case "main":
                    $this->callView("knjc030Form1");
                   break 2;

                case "error":
                    $this->callView("error");
                    break 2;

                case "read_before":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->key_Move_Model("before");
                    $sessionInstance->setCmd("main");
                    break 1;

                case "read_next":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->key_Move_Model("next");
                    $sessionInstance->setCmd("main");
                    break 1;
                    
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
$knjc030Ctl = new knjc030Controller;
//var_dump($_REQUEST);
?>
