<?php

require_once('for_php7.php');

require_once('knjm490Model.inc');
require_once('knjm490Query.inc');

class knjm490Controller extends Controller {
    var $ModelClassName = "knjm490Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm490":
                    $sessionInstance->knjm490Model();
                    $this->callView("knjm490Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm490Ctl = new knjm490Controller;
var_dump($_REQUEST);
?>
