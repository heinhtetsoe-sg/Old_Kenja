<?php

require_once('for_php7.php');

require_once('knjm510Model.inc');
require_once('knjm510Query.inc');

class knjm510Controller extends Controller {
    var $ModelClassName = "knjm510Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm510":
                    $sessionInstance->knjm510Model();
                    $this->callView("knjm510Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm510Ctl = new knjm510Controller;
var_dump($_REQUEST);
?>
