<?php

require_once('for_php7.php');

require_once('knjd124gModel.inc');
require_once('knjd124gQuery.inc');

class knjd124gController extends Controller {
    var $ModelClassName = "knjd124gModel";
    var $ProgramID      = "KNJD124G";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                   $this->callView("knjd124gForm1");
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

$knjd124gCtl = new knjd124gController;
?>
