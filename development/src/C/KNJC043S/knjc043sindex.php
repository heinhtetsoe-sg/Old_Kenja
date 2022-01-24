<?php

require_once('for_php7.php');

require_once('knjc043sModel.inc');
require_once('knjc043sQuery.inc');

class knjc043sController extends Controller {
    var $ModelClassName = "knjc043sModel";
    var $ProgramID      = "KNJC043S";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc043s":
                case "semechg":
                    $sessionInstance->knjc043sModel();
                    $this->callView("knjc043sForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjc043sCtl = new knjc043sController;
//var_dump($_REQUEST);
?>
