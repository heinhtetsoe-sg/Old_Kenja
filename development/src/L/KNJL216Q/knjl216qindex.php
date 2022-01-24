<?php

require_once('for_php7.php');

require_once('knjl216qModel.inc');
require_once('knjl216qQuery.inc');

class knjl216qController extends Controller {
    var $ModelClassName = "knjl216qModel";
    var $ProgramID      = "KNJL216Q";

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
                case "reset":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl216qForm1");
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
$knjl216qCtl = new knjl216qController;
?>
