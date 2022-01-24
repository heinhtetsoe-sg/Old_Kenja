<?php

require_once('for_php7.php');

require_once('knjl388qModel.inc');
require_once('knjl388qQuery.inc');

class knjl388qController extends Controller {
    var $ModelClassName = "knjl388qModel";
    var $ProgramID      = "KNJL388Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $this->callView("knjl388qForm1");
                    break 2;
                case "csv":
                    if(!$sessionInstance->getCsvFile()){
                        $this->callView("knjl388qForm1");
                    }
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
$knjl388qCtl = new knjl388qController;
?>
