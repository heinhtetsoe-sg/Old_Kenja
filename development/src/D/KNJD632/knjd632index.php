<?php

require_once('for_php7.php');

require_once('knjd632Model.inc');
require_once('knjd632Query.inc');

class knjd632Controller extends Controller {
    var $ModelClassName = "knjd632Model";
    var $ProgramID      = "KNJD632";

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
                    //$sessionInstance->getMainModel();
                    $this->callView("knjd632Form1");
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
$knjd632Ctl = new knjd632Controller;
?>
