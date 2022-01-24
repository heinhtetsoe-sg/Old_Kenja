<?php

require_once('for_php7.php');

require_once('knjm433Model.inc');
require_once('knjm433Query.inc');

class knjm433Controller extends Controller {
    var $ModelClassName = "knjm433Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm433":
                    $sessionInstance->knjm433Model();
                    $this->callView("knjm433Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm433Ctl = new knjm433Controller;
var_dump($_REQUEST);
?>
