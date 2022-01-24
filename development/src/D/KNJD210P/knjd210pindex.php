<?php

require_once('for_php7.php');


require_once('knjd210pModel.inc');
require_once('knjd210pQuery.inc');

class knjd210pController extends Controller {
    var $ModelClassName = "knjd210pModel";
    var $ProgramID      = "KNJD210P";

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
                    $this->callView("knjd210pForm1");
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
$knjd210pCtl = new knjd210pController;
?>
