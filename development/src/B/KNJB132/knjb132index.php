<?php

require_once('for_php7.php');

require_once('knjb132Model.inc');
require_once('knjb132Query.inc');

class knjb132Controller extends Controller {
    var $ModelClassName = "knjb132Model";
    var $ProgramID      = "KNJB132";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb132":
                    $sessionInstance->knjb132Model();
                    $this->callView("knjb132Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb132Ctl = new knjb132Controller;
?>
