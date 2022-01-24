<?php

require_once('for_php7.php');

require_once('knjz406Model.inc');
require_once('knjz406Query.inc');

class knjz406Controller extends Controller {
    var $ModelClassName = "knjz406Model";
    var $ProgramID      = "KNJZ406";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "changeDataDiv":
                case "changeGradeHrClass":
                case "changeSubclasscd":
                case "def":
                case "reset":
                   $this->callView("knjz406Form1");
                   break 2;
                case "add":
                case "ins":
                case "del":
                case "extend":
                case "moveUp":
                case "moveDown":
                    $sessionInstance->setDataArray();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "update":
                    $sessionInstance->setDataArray();
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

$knjz406Ctl = new knjz406Controller;
?>
