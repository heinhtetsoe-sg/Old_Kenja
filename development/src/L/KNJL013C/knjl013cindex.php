<?php

require_once('for_php7.php');

require_once('knjl013cModel.inc');
require_once('knjl013cQuery.inc');

class knjl013cController extends Controller {
    var $ModelClassName = "knjl013cModel";
    var $ProgramID      = "KNJL013C";

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
                    $this->callView("knjl013cForm1");
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
$knjl013cCtl = new knjl013cController;
?>
