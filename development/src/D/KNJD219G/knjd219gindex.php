<?php

require_once('for_php7.php');


require_once('knjd219gModel.inc');
require_once('knjd219gQuery.inc');

class knjd219gController extends Controller {
    var $ModelClassName = "knjd219gModel";
    var $ProgramID      = "KNJD219G";

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
                    //$sessionInstance->getMainModel();
                    $this->callView("knjd219gForm1");
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
$knjd219gCtl = new knjd219gController;
?>
