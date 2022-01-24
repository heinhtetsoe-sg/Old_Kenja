<?php

require_once('for_php7.php');

require_once('knjh320Model.inc');
require_once('knjh320Query.inc');
require_once('graph.php');

class knjh320Controller extends Controller {
    var $ModelClassName = "knjh320Model";
    var $ProgramID      = "KNJH320";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "yearChange":
                    $this->callView("knjh320Form1");
                    exit;
                case "":
                case "change":
                case "bar":
                case "radar":
                    $sessionInstance->knjh320Model();
                    $this->callView("knjh320Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh320Ctl = new knjh320Controller;
?>
