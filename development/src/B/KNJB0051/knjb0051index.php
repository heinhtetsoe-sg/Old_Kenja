<?php

require_once('for_php7.php');

require_once('knjb0051Model.inc');
require_once('knjb0051Query.inc');

class knjb0051Controller extends Controller {
    var $ModelClassName = "knjb0051Model";
    var $ProgramID      = "KNJB0051";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb0051":
                    $this->callView("knjb0051Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb0051Ctl = new knjb0051Controller;
?>
