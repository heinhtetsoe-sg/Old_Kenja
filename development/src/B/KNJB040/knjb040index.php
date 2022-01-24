<?php

require_once('for_php7.php');

require_once('knjb040Model.inc');
require_once('knjb040Query.inc');

class knjb040Controller extends Controller {
    var $ModelClassName = "knjb040Model";
    var $ProgramID      = "KNJB040";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {

                case "":
                case "knjb040":
                    $this->callView("knjb040Form1");
                    break 2;

                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjb040Ctl = new knjb040Controller;
//var_dump($_REQUEST);
?>
