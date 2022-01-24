<?php

require_once('for_php7.php');

require_once('knjd128v_syukketuModel.inc');
require_once('knjd128v_syukketuQuery.inc');

class knjd128v_syukketuController extends Controller {
    var $ModelClassName = "knjd128v_syukketuModel";
    var $ProgramID      = "KNJD128V_SYUKKETU";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                   $this->callView("knjd128v_syukketuForm1");
                   break 2;
                case "change":
                case "subclasscd":
                case "chaircd":
                    //$sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knjd128v_syukketuForm1");
                    break 2;
                case "update":
                    //$sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd128v_syukketuCtl = new knjd128v_syukketuController;
?>
