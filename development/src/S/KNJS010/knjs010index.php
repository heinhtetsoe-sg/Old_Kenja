<?php

require_once('for_php7.php');

require_once('knjs010Model.inc');
require_once('knjs010Query.inc');

class knjs010Controller extends Controller {
    var $ModelClassName = "knjs010Model";
    var $ProgramID      = "KNJS010";

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
                   $this->callView("knjs010Form1");
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

$knjs010Ctl = new knjs010Controller;
?>
