<?php

require_once('for_php7.php');

require_once('knjl013yModel.inc');
require_once('knjl013yQuery.inc');

class knjl013yController extends Controller {
    var $ModelClassName = "knjl013yModel";
    var $ProgramID      = "KNJL013Y";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "knjl013y":
                    $this->callView("knjl013yForm1");
                    break 2;
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl013yForm1");
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
$knjl013yCtl = new knjl013yController;
?>
