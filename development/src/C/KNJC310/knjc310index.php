<?php

require_once('for_php7.php');

require_once('knjc310Model.inc');
require_once('knjc310Query.inc');

class knjc310Controller extends Controller {
    var $ModelClassName = "knjc310Model";
    var $ProgramID      = "KNJC310";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "changeCombo":
                case "changeRadio":
                    $this->callView("knjc310Form1");
                    break 2;
                case "delete":
                case "deleteAll":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("edit");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc310Ctl = new knjc310Controller;
//var_dump($_REQUEST);
?>
