<?php

require_once('for_php7.php');


require_once('knjd210oModel.inc');
require_once('knjd210oQuery.inc');

class knjd210oController extends Controller {
    var $ModelClassName = "knjd210oModel";
    var $ProgramID      = "KNJD210O";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "execute":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                    $this->callView("knjd210oForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjd210oCtl = new knjd210oController;
?>
