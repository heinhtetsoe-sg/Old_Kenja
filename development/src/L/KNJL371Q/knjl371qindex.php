<?php

require_once('for_php7.php');

require_once('knjl371qModel.inc');
require_once('knjl371qQuery.inc');

class knjl371qController extends Controller {
    var $ModelClassName = "knjl371qModel";
    var $ProgramID      = "KNJL371Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    //$sessionInstance->setAccessLogDetail("S", $ProgramID);
                    //$sessionInstance->getMainModel();
                    $this->callView("knjl371qForm1");
                    break 2;
                case "import":
                    $sessionInstance->getExecModel();
                    $this->callView("knjl371qForm1");
                    break 2;
                case "export":
                    if(!$sessionInstance->getExportModel()){
                        $this->callView("knjl371qForm1");
                    }
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
$knjl371qCtl = new knjl371qController;
?>
