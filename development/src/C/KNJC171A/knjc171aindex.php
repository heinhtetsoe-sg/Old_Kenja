<?php

require_once('for_php7.php');

require_once('knjc171aModel.inc');
require_once('knjc171aQuery.inc');

class knjc171aController extends Controller {
    var $ModelClassName = "knjc171aModel";
    var $ProgramID      = "KNJC171A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc171a":
                    $sessionInstance->knjc171aModel();
                    $this->callView("knjc171aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjc171aCtl = new knjc171aController;
?>
