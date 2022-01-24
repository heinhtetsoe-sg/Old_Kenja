<?php

require_once('for_php7.php');

require_once('knjh310Model.inc');
require_once('knjh310Query.inc');
require_once('graph.php');

class knjh310Controller extends Controller {
    var $ModelClassName = "knjh310Model";
    var $ProgramID      = "KNJH310";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "yearChange":
                    $this->callView("knjh310Form1");
                    exit;
                case "":
                case "change":
                case "bar":
                case "radar":
                case "graph":
                    $sessionInstance->knjh310Model();
                    $this->callView("knjh310Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh310Ctl = new knjh310Controller;
?>
