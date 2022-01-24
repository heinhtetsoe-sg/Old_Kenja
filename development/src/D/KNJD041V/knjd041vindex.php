<?php

require_once('for_php7.php');

require_once('knjd041vModel.inc');
require_once('knjd041vQuery.inc');

class knjd041vController extends Controller {
    var $ModelClassName = "knjd041vModel";
    var $ProgramID      = "KNJD041V";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd041vModel();
                    $this->callView("knjd041vForm1");
                    exit;
                case "knjd041v":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd041vModel();
                    $this->callView("knjd041vForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjd041vCtl = new knjd041vController;
//var_dump($_REQUEST);
?>
