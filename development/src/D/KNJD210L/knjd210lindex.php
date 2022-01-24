<?php

require_once('for_php7.php');


require_once('knjd210lModel.inc');
require_once('knjd210lQuery.inc');

class knjd210lController extends Controller {
    var $ModelClassName = "knjd210lModel";
    var $ProgramID      = "KNJD210L";

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
                    $this->callView("knjd210lForm1");
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
$knjd210lCtl = new knjd210lController;
?>
