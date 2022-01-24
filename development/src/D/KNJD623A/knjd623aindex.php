<?php

require_once('for_php7.php');

require_once('knjd623aModel.inc');
require_once('knjd623aQuery.inc');

class knjd623aController extends Controller {
    var $ModelClassName = "knjd623aModel";
    var $ProgramID      = "KNJD623A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd623a":
                    $sessionInstance->knjd623aModel();
                    $this->callView("knjd623aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd623aCtl = new knjd623aController;
?>
