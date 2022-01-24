<?php

require_once('for_php7.php');

require_once('knjc040Model.inc');
require_once('knjc040Query.inc');

class knjc040Controller extends Controller {
    var $ModelClassName = "knjc040Model";
    var $ProgramID      = "KNJC040";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc040":
                case "semechg":
                    $sessionInstance->knjc040Model();
                    $this->callView("knjc040Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjc040Ctl = new knjc040Controller;
//var_dump($_REQUEST);
?>
