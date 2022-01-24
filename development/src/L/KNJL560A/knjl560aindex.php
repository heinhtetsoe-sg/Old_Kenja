<?php

require_once('for_php7.php');

require_once('knjl560aModel.inc');
require_once('knjl560aQuery.inc');

class knjl560aController extends Controller {
    var $ModelClassName = "knjl560aModel";
    var $ProgramID      = "KNJL560A";

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
                    $this->callView("knjl560aForm1");
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
$knjl560aCtl = new knjl560aController;
?>
