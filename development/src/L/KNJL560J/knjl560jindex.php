<?php

require_once('for_php7.php');

require_once('knjl560jModel.inc');
require_once('knjl560jQuery.inc');

class knjl560jController extends Controller {
    var $ModelClassName = "knjl560jModel";
    var $ProgramID      = "KNJL560J";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl560jForm1");
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
$knjl560jCtl = new knjl560jController;
?>
