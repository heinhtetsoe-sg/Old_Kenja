<?php

require_once('for_php7.php');

require_once('knjh189Model.inc');
require_once('knjh189Query.inc');

class knjh189Controller extends Controller {
    var $ModelClassName = "knjh189Model";
    var $ProgramID      = "KNJH189";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh189":
                    $sessionInstance->knjh189Model();
                    $this->callView("knjh189Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh189Ctl = new knjh189Controller;
?>
