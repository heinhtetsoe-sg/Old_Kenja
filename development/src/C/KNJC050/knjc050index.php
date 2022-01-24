<?php

require_once('for_php7.php');

require_once('knjc050Model.inc');
require_once('knjc050Query.inc');

class knjc050Controller extends Controller {
    var $ModelClassName = "knjc050Model";
    var $ProgramID      = "KNJC050";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "toukei":
                    $sessionInstance->knjc050Model();
                    $this->callView("knjc050Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjc050cCtl = new knjc050Controller;
//var_dump($_REQUEST);
?>
