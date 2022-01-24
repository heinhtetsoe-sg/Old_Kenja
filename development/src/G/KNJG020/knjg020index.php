<?php

require_once('for_php7.php');

require_once('knjg020Model.inc');
require_once('knjg020Query.inc');

class knjg020Controller extends Controller {
    var $ModelClassName = "knjg020Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjg020":
                    $sessionInstance->knjg020Model();
                    $this->callView("knjg020Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjg020Ctl = new knjg020Controller;
//var_dump($_REQUEST);
?>
