<?php

require_once('for_php7.php');

require_once('knjc166bModel.inc');
require_once('knjc166bQuery.inc');

class knjc166bController extends Controller {
    var $ModelClassName = "knjc166bModel";
    var $ProgramID      = "KNJC166B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "change":
                case "reset":
                case "changeYear":
                case "changeGrade":
                   $this->callView("knjc166bForm1");
                   break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
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

$knjc166bCtl = new knjc166bController;
?>
