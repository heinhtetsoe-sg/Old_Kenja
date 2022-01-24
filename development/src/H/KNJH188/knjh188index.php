<?php

require_once('for_php7.php');

require_once('knjh188Model.inc');
require_once('knjh188Query.inc');

class knjh188Controller extends Controller {
    var $ModelClassName = "knjh188Model";
    var $ProgramID      = "KNJH188";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh188":
                    $sessionInstance->knjh188Model();
                    $this->callView("knjh188Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh188Ctl = new knjh188Controller;
?>
