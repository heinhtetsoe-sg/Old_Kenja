<?php

require_once('for_php7.php');


require_once('knjd210iModel.inc');
require_once('knjd210iQuery.inc');

class knjd210iController extends Controller {
    var $ModelClassName = "knjd210iModel";
    var $ProgramID      = "KNJD210I";

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
                    $this->callView("knjd210iForm1");
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
$knjd210iCtl = new knjd210iController;
?>
