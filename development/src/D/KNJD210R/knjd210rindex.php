<?php

require_once('for_php7.php');


require_once('knjd210rModel.inc');
require_once('knjd210rQuery.inc');

class knjd210rController extends Controller {
    var $ModelClassName = "knjd210rModel";
    var $ProgramID      = "KNJD210R";

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
                    $this->callView("knjd210rForm1");
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
$knjd210rCtl = new knjd210rController;
?>
