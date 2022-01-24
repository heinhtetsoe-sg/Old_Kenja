<?php

require_once('for_php7.php');

require_once('knjh538dModel.inc');
require_once('knjh538dQuery.inc');

class knjh538dController extends Controller {
    var $ModelClassName = "knjh538dModel";
    var $ProgramID      = "KNJH538D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                   $this->callView("knjh538dForm1");
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

$knjh538dCtl = new knjh538dController;
?>
