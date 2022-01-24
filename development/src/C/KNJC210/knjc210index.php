<?php

require_once('for_php7.php');

require_once('knjc210Model.inc');
require_once('knjc210Query.inc');

class knjc210Controller extends Controller {
    var $ModelClassName = "knjc210Model";
    var $ProgramID      = "KNJC210";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc210":
                    $sessionInstance->knjc210Model();
                    $this->callView("knjc210Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc210Ctl = new knjc210Controller;
?>
