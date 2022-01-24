<?php

require_once('for_php7.php');

require_once('knjc110bModel.inc');
require_once('knjc110bQuery.inc');

class knjc110bController extends Controller {
    var $ModelClassName = "knjc110bModel";
    var $ProgramID      = "KNJC110B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc110b":
                case "semechg":
                    $sessionInstance->knjc110bModel();
                    $this->callView("knjc110bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjc110bCtl = new knjc110bController;
//var_dump($_REQUEST);
?>
