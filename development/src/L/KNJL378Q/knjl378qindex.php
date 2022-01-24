<?php

require_once('for_php7.php');

require_once('knjl378qModel.inc');
require_once('knjl378qQuery.inc');

class knjl378qController extends Controller {
    var $ModelClassName = "knjl378qModel";
    var $ProgramID      = "KNJl378q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $this->callView("knjl378qForm1");
                    break 2;
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->cmd = "";
                    break 1;
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
$knjl378qCtl = new knjl378qController;
?>
