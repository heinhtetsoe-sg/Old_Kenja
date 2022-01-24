<?php

require_once('for_php7.php');

require_once('knjc165Model.inc');
require_once('knjc165Query.inc');

class knjc165Controller extends Controller {
    var $ModelClassName = "knjc165Model";
    var $ProgramID      = "KNJC165";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc165":
                    $sessionInstance->knjc165Model();
                    $this->callView("knjc165Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc165Ctl = new knjc165Controller;
?>
