<?php

require_once('for_php7.php');

require_once('knjc110aModel.inc');
require_once('knjc110aQuery.inc');

class knjc110aController extends Controller {
    var $ModelClassName = "knjc110aModel";
    var $ProgramID      = "KNJC110A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc110a":
                case "change":
                    $sessionInstance->knjc110aModel();
                    $this->callView("knjc110aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjc110aCtl = new knjc110aController;
//var_dump($_REQUEST);
?>
