<?php

require_once('for_php7.php');


require_once('knjd210dModel.inc');
require_once('knjd210dQuery.inc');

class knjd210dController extends Controller {
    var $ModelClassName = "knjd210dModel";
    var $ProgramID      = "KNJD210D";

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
                    $this->callView("knjd210dForm1");
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
$knjd210dCtl = new knjd210dController;
?>
