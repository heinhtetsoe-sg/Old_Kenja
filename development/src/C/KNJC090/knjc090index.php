<?php

require_once('for_php7.php');

require_once('knjc090Model.inc');
require_once('knjc090Query.inc');

class knjc090Controller extends Controller {
    var $ModelClassName = "knjc090Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc090":
                    $sessionInstance->knjc090Model();
                    $this->callView("knjc090Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjc090Ctl = new knjc090Controller;
//var_dump($_REQUEST);
?>
