<?php

require_once('for_php7.php');

require_once('knjc161Model.inc');
require_once('knjc161Query.inc');

class knjc161Controller extends Controller {
    var $ModelClassName = "knjc161Model";
    var $ProgramID      = "KNJC161";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc161":
                    $sessionInstance->knjc161Model();
                    $this->callView("knjc161Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc161Ctl = new knjc161Controller;
?>
