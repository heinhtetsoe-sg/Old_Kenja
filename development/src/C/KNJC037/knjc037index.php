<?php

require_once('for_php7.php');

require_once('knjc037Model.inc');
require_once('knjc037Query.inc');

class knjc037Controller extends Controller {
    var $ModelClassName = "knjc037Model";
    var $ProgramID      = "KNJC037";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "knjc037":
                    $sessionInstance->knjc037Model();
                    $this->callView("knjc037Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc037Ctl = new knjc037Controller;
//var_dump($_REQUEST);
?>
