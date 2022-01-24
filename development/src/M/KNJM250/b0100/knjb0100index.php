<?php

require_once('for_php7.php');

require_once('knjb0100Model.inc');
require_once('knjb0100Query.inc');

class knjb0100Controller extends Controller {
    var $ModelClassName = "knjb0100Model";
    var $ProgramID      = "KNJB0100";     //プログラムID

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "reset":
                case "read":
                    $sessionInstance->getMainModel();
                    $this->callView("knjb0100Form1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb0100Ctl = new knjb0100Controller;
?>
