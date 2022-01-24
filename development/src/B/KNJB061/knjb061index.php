<?php

require_once('for_php7.php');

require_once('knjb061Model.inc');
require_once('knjb061Query.inc');

class knjb061Controller extends Controller {
    var $ModelClassName = "knjb061Model";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb061":
                    $this->callView("knjb061Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb061Ctl = new knjb061Controller;
//var_dump($_REQUEST);
?>
