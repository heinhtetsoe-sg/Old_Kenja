<?php

require_once('for_php7.php');

require_once('knjl260yModel.inc');
require_once('knjl260yQuery.inc');

class knjl260yController extends Controller {
    var $ModelClassName = "knjl260yModel";
    var $ProgramID      = "KNJL260Y";

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
                    $this->callView("knjl260yForm1");
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
$knjl260yCtl = new knjl260yController;
?>
