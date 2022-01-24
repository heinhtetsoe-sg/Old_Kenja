<?php

require_once('for_php7.php');


require_once('knjd210fModel.inc');
require_once('knjd210fQuery.inc');

class knjd210fController extends Controller {
    var $ModelClassName = "knjd210fModel";
    var $ProgramID      = "KNJD210F";

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
                    $this->callView("knjd210fForm1");
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
$knjd210fCtl = new knjd210fController;
?>
