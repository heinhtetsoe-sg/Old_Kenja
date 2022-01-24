<?php

require_once('for_php7.php');

require_once('knjb070Model.inc');
require_once('knjb070Query.inc');

class knjb070Controller extends Controller {
    var $ModelClassName = "knjb070Model";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {

                case "":
                case "knjb070":
                    $this->callView("knjb070Form1");
                    break 2;

                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjb070Ctl = new knjb070Controller;
//var_dump($_REQUEST);
?>
