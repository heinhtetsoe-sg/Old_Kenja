<?php

require_once('for_php7.php');

require_once('knjb033Model.inc');
require_once('knjb033Query.inc');

class knjb033Controller extends Controller {
    var $ModelClassName = "knjb033Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {

                case "":
                case "knjb033":
                    $this->callView("knjb033Form1");
                    break 2;

                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjb033Ctl = new knjb033Controller;
//var_dump($_REQUEST);
?>
