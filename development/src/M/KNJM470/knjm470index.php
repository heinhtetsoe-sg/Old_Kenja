<?php

require_once('for_php7.php');

require_once('knjm470Model.inc');
require_once('knjm470Query.inc');

class knjm470Controller extends Controller {
    var $ModelClassName = "knjm470Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm470":
                    $sessionInstance->knjm470Model();
                    $this->callView("knjm470Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm470Ctl = new knjm470Controller;
var_dump($_REQUEST);
?>
