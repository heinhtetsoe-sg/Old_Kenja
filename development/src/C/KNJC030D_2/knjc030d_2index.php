<?php

require_once('for_php7.php');

require_once('knjc030d_2Model.inc');
require_once('knjc030d_2Query.inc');

class knjc030d_2Controller extends Controller {
    var $ModelClassName = "knjc030d_2Model";
    var $ProgramID      = "KNJC030D_2";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "change":
                case "reset":
                   $this->callView("knjc030d_2Form1");
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

$knjc030d_2Ctl = new knjc030d_2Controller;
?>
