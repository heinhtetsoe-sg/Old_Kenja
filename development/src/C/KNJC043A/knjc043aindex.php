<?php

require_once('for_php7.php');

require_once('knjc043aModel.inc');
require_once('knjc043aQuery.inc');

class knjc043aController extends Controller {
    var $ModelClassName = "knjc043aModel";
    var $ProgramID      = "KNJC043A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc043a":
                    $sessionInstance->knjc043aModel();
                    $this->callView("knjc043aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjc043aCtl = new knjc043aController;
?>
