<?php

require_once('for_php7.php');

require_once('knjc031d_2Model.inc');
require_once('knjc031d_2Query.inc');

class knjc031d_2Controller extends Controller {
    var $ModelClassName = "knjc031d_2Model";
    var $ProgramID      = "KNJC031D_2";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "change":
                case "reset":
                   $this->callView("knjc031d_2Form1");
                   break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
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

$knjc031d_2Ctl = new knjc031d_2Controller;
?>
