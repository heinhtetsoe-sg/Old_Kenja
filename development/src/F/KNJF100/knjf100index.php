<?php

require_once('for_php7.php');

require_once('knjf100Model.inc');
require_once('knjf100Query.inc');

class knjf100Controller extends Controller {
    var $ModelClassName = "knjf100Model";
    var $ProgramID      = "KNJF100";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf100":
                case "semechg":
                    $sessionInstance->knjf100Model();
                    $this->callView("knjf100Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("{滋ymu=q写r`rmrur&r2{$sessionInstance->cmd}q偲q七"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjf100Ctl = new knjf100Controller;
//var_dump($_REQUEST);
?>
