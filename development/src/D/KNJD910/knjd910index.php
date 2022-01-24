<?php

require_once('for_php7.php');


require_once('knjd910Model.inc');
require_once('knjd910Query.inc');

class knjd910Controller extends Controller {
    var $ModelClassName = "knjd910Model";
    var $ProgramID      = "KNJD910";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "execute":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                    $this->callView("knjd910Form1");
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
$knjd910Ctl = new knjd910Controller;
?>
