<?php

require_once('for_php7.php');

require_once('knjl230cModel.inc');
require_once('knjl230cQuery.inc');

class knjl230cController extends Controller {
    var $ModelClassName = "knjl230cModel";
    var $ProgramID      = "KNJL230C";

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
                    $this->callView("knjl230cForm1");
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
$knjl230cCtl = new knjl230cController;
?>
