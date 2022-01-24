<?php

require_once('for_php7.php');

require_once('knjb050Model.inc');
require_once('knjb050Query.inc');

class knjb050Controller extends Controller {
    var $ModelClassName = "knjb050Model";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb050":
                    $this->callView("knjb050Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb050Ctl = new knjb050Controller;
//var_dump($_REQUEST);
?>
