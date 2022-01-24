<?php

require_once('for_php7.php');


require_once('knjd210bModel.inc');
require_once('knjd210bQuery.inc');

class knjd210bController extends Controller {
    var $ModelClassName = "knjd210bModel";
    var $ProgramID      = "KNJD210B";

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
                    $this->callView("knjd210bForm1");
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
$knjd210bCtl = new knjd210bController;
?>
