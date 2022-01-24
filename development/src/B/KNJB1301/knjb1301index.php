<?php

require_once('for_php7.php');

require_once('knjb1301Model.inc');
require_once('knjb1301Query.inc');

class knjb1301Controller extends Controller {
    var $ModelClassName = "knjb1301Model";
    var $ProgramID      = "KNJB1301";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "clear";
                    $sessionInstance->knjb1301Model();
                    $this->callView("knjb1301Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb1301Ctl = new knjb1301Controller;
?>
