<?php

require_once('for_php7.php');


require_once('knjd210sModel.inc');
require_once('knjd210sQuery.inc');

class knjd210sController extends Controller {
    var $ModelClassName = "knjd210sModel";
    var $ProgramID      = "KNJD210S";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "execute":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knjd210sForm1");
                    break 2;
                case "main":
                    $this->callView("knjd210sForm1");
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
$knjd210sCtl = new knjd210sController;
?>
